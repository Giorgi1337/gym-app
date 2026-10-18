package com.gym.service;

import com.gym.dto.RegistrationResponse;
import com.gym.dto.TraineeSummary;
import com.gym.dto.TrainerProfileResponse;
import com.gym.dto.TrainerRegistrationRequest;
import com.gym.dto.TrainerUpdateRequest;
import com.gym.dto.TrainerUpdateResponse;
import com.gym.exception.ResourceNotFoundException;
import com.gym.metrics.GymMetrics;
import com.gym.model.*;
import com.gym.repository.TrainerRepository;
import com.gym.repository.TrainingTypeRepository;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.gym.utils.NameUtils.normalize;

@Service
@Transactional
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernameGenerator usernameGenerator;
    private final GymMetrics gymMetrics;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;

    public TrainerService(TrainerRepository trainerRepository,
                          TrainingTypeRepository trainingTypeRepository,
                          UsernameGenerator usernameGenerator,
                          GymMetrics gymMetrics, PasswordEncoder passwordEncoder,
                          PasswordGenerator passwordGenerator) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.usernameGenerator = usernameGenerator;
        this.gymMetrics = gymMetrics;
        this.passwordEncoder = passwordEncoder;
        this.passwordGenerator = passwordGenerator;
    }

    public RegistrationResponse save(TrainerRegistrationRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .build();
        TrainingType requestedSpecialization = TrainingType.builder()
                .trainingTypeName(request.getSpecialization())
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(requestedSpecialization)
                .build();

        user.setFirstName(normalize(user.getFirstName()));
        user.setLastName(normalize(user.getLastName()));

        String username = usernameGenerator.generate(user.getFirstName(), user.getLastName());
        String rawPassword = passwordGenerator.generate();

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));

        String specializationName = StringUtils.normalizeSpace(trainer.getSpecialization().getTrainingTypeName());
        TrainingType specialization = trainingTypeRepository
                .findByTrainingTypeNameEqualsIgnoreCase(specializationName)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found: " + specializationName));
        trainer.setSpecialization(specialization);

        trainerRepository.save(trainer);
        gymMetrics.incrementTrainerRegistration();
        log.info("Registered trainer: {}", username);

        return new RegistrationResponse(username, rawPassword);   // return raw password once, here only
    }

    @Transactional(readOnly = true)
    public TrainerProfileResponse findByUsername(String username) {
        Trainer trainer = trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        return new TrainerProfileResponse()
                .firstName(trainer.getUser().getFirstName())
                .lastName(trainer.getUser().getLastName())
                .specialization(trainer.getSpecialization().getTrainingTypeName())
                .isActive(trainer.getUser().getIsActive())
                .trainees(toTraineeSummaries(trainer));
    }


    @Transactional
    public TrainerUpdateResponse update(String username, TrainerUpdateRequest request) {
        Trainer trainer = trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        User user = trainer.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(request.getIsActive());
        trainerRepository.save(trainer);

        log.info("Updated trainer: {}", trainer.getUser().getUsername());
        return new TrainerUpdateResponse()
                .username(trainer.getUser().getUsername())
                .firstName(trainer.getUser().getFirstName())
                .lastName(trainer.getUser().getLastName())
                .specialization(trainer.getSpecialization().getTrainingTypeName())
                .isActive(trainer.getUser().getIsActive())
                .trainees(toTraineeSummaries(trainer));
    }

    private List<TraineeSummary> toTraineeSummaries(Trainer trainer) {
        if (trainer.getTrainees() == null) {
            return List.of();
        }
        return trainer.getTrainees().stream()
                .map(trainee -> new TraineeSummary()
                        .username(trainee.getUser().getUsername())
                        .firstName(trainee.getUser().getFirstName())
                        .lastName(trainee.getUser().getLastName()))
                .toList();
    }

    @Transactional
    public void setActive(String username, boolean active) {
        Trainer trainer = trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        trainer.getUser().setIsActive(active);
        trainerRepository.save(trainer);

        log.info("Set active = {} for trainer: {}", active, username);
    }

}
