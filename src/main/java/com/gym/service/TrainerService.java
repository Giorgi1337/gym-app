package com.gym.service;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TrainerMapper;
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

    public TrainerService(TrainerRepository trainerRepository,
                          TrainingTypeRepository trainingTypeRepository,
                          UsernameGenerator usernameGenerator,
                          GymMetrics gymMetrics, PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.usernameGenerator = usernameGenerator;
        this.gymMetrics = gymMetrics;
        this.passwordEncoder = passwordEncoder;
    }

    public RegistrationResponse save(TrainerRegistrationRequest request) {
        Trainer trainer = TrainerMapper.toEntity(request);

        User user = trainer.getUser();

        user.setFirstName(normalize(user.getFirstName()));
        user.setLastName(normalize(user.getLastName()));

        String username = usernameGenerator.generate(user.getFirstName(), user.getLastName());
        String rawPassword = PasswordGenerator.generate();

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

        return TrainerMapper.toProfileResponse(trainer);
    }


    @Transactional
    public TrainerUpdateResponse update(String username, TrainerUpdateRequest request) {
        Trainer trainer = trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        TrainerMapper.applyUpdate(trainer, request);
        trainerRepository.save(trainer);

        log.info("Updated trainer: {}", trainer.getUser().getUsername());
        return TrainerMapper.toUpdateResponse(trainer);
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
