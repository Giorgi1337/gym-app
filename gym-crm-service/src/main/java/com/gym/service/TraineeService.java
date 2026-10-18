package com.gym.service;

import com.gym.dto.RegistrationResponse;
import com.gym.dto.TraineeProfileResponse;
import com.gym.dto.TraineeRegistrationRequest;
import com.gym.dto.TraineeUpdateRequest;
import com.gym.dto.TraineeUpdateResponse;
import com.gym.dto.TrainerRef;
import com.gym.dto.TrainerSummary;
import com.gym.dto.UpdateTraineeTrainersRequest;
import com.gym.exception.ResourceNotFoundException;
import com.gym.metrics.GymMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gym.utils.NameUtils.normalize;

@Service
@Transactional
@RequiredArgsConstructor
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final GymMetrics gymMetrics;

    public RegistrationResponse save(TraineeRegistrationRequest request) {

        Trainee trainee = Trainee.builder()
                .user(User.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .isActive(true)
                        .build())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();
        User user = trainee.getUser();

        user.setFirstName(normalize(user.getFirstName()));
        user.setLastName(normalize(user.getLastName()));

        String username = usernameGenerator.generate(user.getFirstName(), user.getLastName());
        String rawPassword = passwordGenerator.generate();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));

        traineeRepository.save(trainee);
        gymMetrics.incrementTraineeRegistration();
        log.info("Registered trainee: {}", username);

        return new RegistrationResponse(username, rawPassword);
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponse findByUsername(String username) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        User user = trainee.getUser();
        return new TraineeProfileResponse()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(trainee.getDateOfBirth())
                .address(trainee.getAddress())
                .isActive(user.getIsActive())
                .trainers(toTrainerSummaries(trainee));
    }

    @Transactional
    public TraineeUpdateResponse update(String username, TraineeUpdateRequest request) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        User user = trainee.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(request.getIsActive());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());
        traineeRepository.save(trainee);

        log.info("Updated trainee: {}", trainee.getUser().getUsername());
        return new TraineeUpdateResponse()
                .username(trainee.getUser().getUsername())
                .firstName(trainee.getUser().getFirstName())
                .lastName(trainee.getUser().getLastName())
                .dateOfBirth(trainee.getDateOfBirth())
                .address(trainee.getAddress())
                .isActive(trainee.getUser().getIsActive())
                .trainers(toTrainerSummaries(trainee));
    }

    @Transactional
    public void delete(String username) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        traineeRepository.delete(trainee);
        log.info("Deleted trainee: {}", username);
    }

    @Transactional(readOnly = true)
    public List<TrainerSummary> getUnassignedTrainers(String username) {
        traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        return traineeRepository.findUnassignedTrainers(username).stream()
                .filter(trainer -> Boolean.TRUE.equals(trainer.getUser().getIsActive()))
                .map(this::toTrainerSummary)
                .toList();
    }

    @Transactional
    public List<TrainerSummary> updateTrainers(String username, UpdateTraineeTrainersRequest request) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        Set<String> requestedUsernames = request.getTrainers().stream()
                .map(TrainerRef::getTrainerUsername)
                .collect(Collectors.toSet());

        List<Trainer> trainers = trainerRepository.findByUsernames(requestedUsernames);

        if (trainers.size() != requestedUsernames.size()) {
            Set<String> foundUsernames = trainers.stream()
                    .map(t -> t.getUser().getUsername())
                    .collect(Collectors.toSet());

            Set<String> missing = requestedUsernames.stream()
                    .filter(u -> !foundUsernames.contains(u))
                    .collect(Collectors.toSet());

            throw new ResourceNotFoundException("Trainer(s) not found: " + missing);
        }

        trainee.setTrainers(new HashSet<>(trainers));
        traineeRepository.save(trainee);

        log.info("Updated trainer list for trainee: {}", username);

        return trainers.stream()
                .map(this::toTrainerSummary)
                .toList();
    }

    private List<TrainerSummary> toTrainerSummaries(Trainee trainee) {
        if (trainee.getTrainers() == null || trainee.getTrainers().isEmpty()) {
            return Collections.emptyList();
        }
        return trainee.getTrainers().stream()
                .filter(Objects::nonNull)
                .map(this::toTrainerSummary)
                .toList();
    }

    private TrainerSummary toTrainerSummary(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerSummary()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .specialization(trainer.getSpecialization().getTrainingTypeName());
    }

    @Transactional
    public void setActive(String username, boolean active) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        trainee.getUser().setIsActive(active);
        traineeRepository.save(trainee);

        log.info("Set active = {} for trainee: {}", active, username);
    }

}
