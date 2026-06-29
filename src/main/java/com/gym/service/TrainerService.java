package com.gym.service;

import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainer.TrainerProfileResponse;
import com.gym.dto.trainer.TrainerRegistrationRequest;
import com.gym.dto.trainer.TrainerUpdateRequest;
import com.gym.dto.trainer.TrainerUpdateResponse;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TrainerMapper;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gym.utils.NameUtils.normalize;

@Service
@Transactional
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final UsernameGenerator usernameGenerator;

    public TrainerService(TrainerDao trainerDao, TrainingTypeDao trainingTypeDao, UsernameGenerator usernameGenerator) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.usernameGenerator = usernameGenerator;
    }

    public RegistrationResponse register(TrainerRegistrationRequest request) {
        Trainer trainer = TrainerMapper.toEntity(request);

        User user = trainer.getUser();

        user.setFirstName(normalize(user.getFirstName()));
        user.setLastName(normalize(user.getLastName()));

        user.setUsername(usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName()
        ));

        user.setPassword(PasswordGenerator.generate());

        String specializationName = StringUtils.normalizeSpace(trainer.getSpecialization().getTrainingTypeName());

        TrainingType specialization = trainingTypeDao.findByName(specializationName);

        if (specialization == null) {
            throw new ResourceNotFoundException("Specialization not found: " + specializationName);
        }

        trainer.setSpecialization(specialization);

        trainerDao.save(trainer);

        log.info("Registered trainer: {}", user.getUsername());
        return TrainerMapper.toRegistrationResponse(trainer);
    }

    @Transactional(readOnly = true)
    public TrainerProfileResponse getProfile(String username) {
        Trainer trainer = trainerDao.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        return TrainerMapper.toProfileResponse(trainer);
    }

    @Transactional
    public TrainerUpdateResponse update(String username, TrainerUpdateRequest request) {
        Trainer trainer = trainerDao.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        TrainerMapper.applyUpdate(trainer, request);
        trainerDao.update(trainer);

        log.info("Updated trainer: {}", trainer.getUser().getUsername());
        return TrainerMapper.toUpdateResponse(trainer);
    }

    @Transactional
    public TrainerUpdateResponse setActive(String username, boolean active) {
        Trainer trainer = trainerDao.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        trainer.getUser().setIsActive(active);
        trainerDao.update(trainer);

        log.info("Set active = {} for trainer: {}", active, username);
        return TrainerMapper.toUpdateResponse(trainer);
    }

}
