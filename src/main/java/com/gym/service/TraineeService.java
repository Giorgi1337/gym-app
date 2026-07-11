package com.gym.service;

import com.gym.dto.*;
import com.gym.mapper.TraineeMapper;
import com.gym.mapper.TrainerMapper;
import com.gym.metrics.GymMetrics;
import com.gym.model.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.TraineeRepository;
import com.gym.dto.TrainerSummary;
import com.gym.repository.TrainerRepository;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import com.gym.dto.TrainerRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gym.utils.NameUtils.normalize;

@Service
@Transactional
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    private final UsernameGenerator usernameGenerator;
    private final GymMetrics gymMetrics;

    public TraineeService(TraineeRepository traineeRepository,
                          TrainerRepository trainerRepository,
                          UsernameGenerator usernameGenerator,
                          GymMetrics gymMetrics) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.usernameGenerator = usernameGenerator;
        this.gymMetrics = gymMetrics;
    }

    public RegistrationResponse save(TraineeRegistrationRequest request) {

        Trainee trainee = TraineeMapper.toEntity(request);

        User user = trainee.getUser();

        user.setFirstName(normalize(user.getFirstName()));
        user.setLastName(normalize(user.getLastName()));

        String username = usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName()
        );

        user.setUsername(username);
        user.setPassword(PasswordGenerator.generate());

        traineeRepository.save(trainee);

        gymMetrics.incrementTraineeRegistration();

        log.info("Registered trainee: {}", username);

        return TraineeMapper.toRegistrationResponse(trainee);
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponse findByUsername(String username) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        return TraineeMapper.toProfileResponse(trainee);
    }

    @Transactional
    public TraineeUpdateResponse update(String username, TraineeUpdateRequest request) {
        Trainee trainee = traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        TraineeMapper.applyUpdate(trainee, request);
        traineeRepository.save(trainee);

        log.info("Updated trainee: {}", trainee.getUser().getUsername());
        return TraineeMapper.toUpdateResponse(trainee);
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
                .map(TrainerMapper::toSummary)
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
                .map(TrainerMapper::toSummary)
                .toList();
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
