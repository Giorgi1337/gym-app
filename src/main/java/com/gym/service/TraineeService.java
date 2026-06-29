package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.dao.TrainerDao;
import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainee.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TraineeMapper;
import com.gym.mapper.TrainerMapper;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
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

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UsernameGenerator usernameGenerator;

    public TraineeService(TraineeDao traineeDao, TrainerDao trainerDao, UsernameGenerator usernameGenerator) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
    }

    public RegistrationResponse register(TraineeRegistrationRequest request) {
        Trainee trainee = TraineeMapper.toEntity(request);

        User user = trainee.getUser();

        user.setFirstName(normalize(user.getFirstName()));
        user.setLastName(normalize(user.getLastName()));

        user.setUsername(usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName()
        ));

        user.setPassword(PasswordGenerator.generate());

        traineeDao.save(trainee);

        log.info("Registered trainee: {}", user.getUsername());
        return TraineeMapper.toRegistrationResponse(trainee);
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponse getProfile(String username) {
        Trainee trainee = traineeDao.getProfile(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        return TraineeMapper.toProfileResponse(trainee);
    }

    @Transactional
    public TraineeUpdateResponse update(String username, TraineeUpdateRequest request) {
        Trainee trainee = traineeDao.getProfile(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        TraineeMapper.applyUpdate(trainee, request);
        traineeDao.update(trainee);

        log.info("Updated trainee: {}", trainee.getUser().getUsername());
        return TraineeMapper.toUpdateResponse(trainee);
    }

    @Transactional
    public void delete(String username) {
        Trainee trainee = traineeDao.getProfile(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        traineeDao.delete(trainee);
        log.info("Deleted trainee: {}", username);
    }

    @Transactional(readOnly = true)
    public List<TraineeProfileResponse.TrainerSummary> getUnassignedTrainers(String username) {
        traineeDao.getProfile(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        return traineeDao.findUnassignedTrainers(username).stream()
                .filter(trainer -> Boolean.TRUE.equals(trainer.getUser().getIsActive()))
                .map(TrainerMapper::toSummary)
                .toList();
    }

    @Transactional
    public List<TraineeProfileResponse.TrainerSummary> updateTrainers(String username, UpdateTraineeTrainersRequest request) {
        Trainee trainee = traineeDao.getProfile(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        Set<String> requestedUsernames = request.trainers().stream()
                .map(UpdateTraineeTrainersRequest.TrainerRef::trainerUsername)
                .collect(Collectors.toSet());

        List<Trainer> trainers = trainerDao.findByUsernames(requestedUsernames);

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
        traineeDao.update(trainee);

        log.info("Updated trainer list for trainee: {}", username);

        return trainers.stream()
                .map(TrainerMapper::toSummary)
                .toList();
    }

    @Transactional
    public TraineeUpdateResponse setActive(String username, boolean active) {
        Trainee trainee = traineeDao.getProfile(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        trainee.getUser().setIsActive(active);
        traineeDao.update(trainee);

        log.info("Set active = {} for trainee: {}", active, username);
        return TraineeMapper.toUpdateResponse(trainee);
    }

}
