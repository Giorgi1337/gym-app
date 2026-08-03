package com.gym.service;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TrainingMapper;
import com.gym.exception.BusinessValidationException;
import com.gym.exception.ErrorResponse;
import com.gym.integration.workload.TrainerWorkloadRequest;
import com.gym.integration.workload.WorkloadGateway;
import org.springframework.data.domain.PageRequest;
import com.gym.mapper.TrainingTypeMapper;
import com.gym.metrics.GymMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final GymMetrics gymMetrics;
    private final WorkloadGateway workloadGateway;

    public TrainingService(
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            TrainingTypeRepository trainingTypeRepository,
            GymMetrics gymMetrics,
            WorkloadGateway workloadGateway) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.gymMetrics = gymMetrics;
        this.workloadGateway = workloadGateway;
    }

    @Transactional(readOnly = true)
    public TraineeTrainingPageResponse getTraineeTrainings(
            String username, @Nullable OffsetDateTime fromDate, @Nullable OffsetDateTime toDate,
            @Nullable String trainerName, @Nullable String trainingType,
            Pageable pageable) {

        traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        Specification<Training> spec = combine(Arrays.asList(
                TrainingSpecifications.forTrainee(username),
                TrainingSpecifications.dateFrom(fromDate),
                TrainingSpecifications.dateTo(toDate),
                TrainingSpecifications.trainerUsernameEquals(trainerName),
                TrainingSpecifications.trainingTypeEquals(trainingType)));

        Pageable resolvedPageable = withDefaultSort(pageable);

        Page<Training> result = gymMetrics.trainingQueryTimer()
                .record(() -> trainingRepository.findAll(spec, resolvedPageable));

        return TrainingMapper.toTraineePage(result);
    }

    @Transactional(readOnly = true)
    public TrainerTrainingPageResponse getTrainerTrainings(
            String username, @Nullable OffsetDateTime fromDate,
            @Nullable OffsetDateTime toDate, @Nullable String traineeName,
            Pageable pageable) {

        trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        Specification<Training> spec = combine(Arrays.asList(
                TrainingSpecifications.forTrainer(username),
                TrainingSpecifications.dateFrom(fromDate),
                TrainingSpecifications.dateTo(toDate),
                TrainingSpecifications.traineeUsernameEquals(traineeName)));

        Pageable resolvedPageable = withDefaultSort(pageable);

        Page<Training> result = gymMetrics.trainingQueryTimer()
                .record(() -> trainingRepository.findAll(spec, resolvedPageable));

        return TrainingMapper.toTrainerPage(result);
    }

    private static Pageable withDefaultSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "trainingDate"));
    }

    public void addTraining(String trainerUsername, String traineeUsername, AddTrainingRequest request) {
        Trainer trainer = trainerRepository.findByUser_Username(trainerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + trainerUsername));

        Trainee trainee = traineeRepository.findByUser_Username(traineeUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + traineeUsername));

//        validateTrainingDate(request.getTrainingDate());

        Training training = TrainingMapper.toEntity(request, trainee, trainer);
        trainingRepository.save(training);
        workloadGateway.send(training, TrainerWorkloadRequest.ActionType.ADD);

        gymMetrics.incrementTrainingAdded();

        log.info("Added training '{}' for trainer={} trainee={}",
                request.getTrainingName(), trainerUsername, traineeUsername);
    }

    public void deleteTraining(String trainerUsername, String traineeUsername, AddTrainingRequest request) {
        Training training = trainingRepository
                .findMatchingTrainings(
                        trainerUsername, traineeUsername, request.getTrainingName().trim(),
                        request.getTrainingDate().toInstant(), request.getTrainingDuration())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Matching training not found"));
        if (training.getTrainingDate().isBefore(Instant.now())) {
            throw new BusinessValidationException(List.of(
                    new ErrorResponse.FieldError("trainingDate", "A past training cannot be deleted")));
        }
        workloadGateway.send(training, TrainerWorkloadRequest.ActionType.DELETE);
        trainingRepository.delete(training);
        log.info("Cancelled training '{}' for trainer={} trainee={}",
                request.getTrainingName(), trainerUsername, traineeUsername);
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getTrainingTypes() {
        return trainingTypeRepository.findAll().stream()
                .map(TrainingTypeMapper::toResponse)
                .toList();
    }

    private static Specification<Training> combine(List<@Nullable Specification<Training>> specs) {
        List<Specification<Training>> present = specs.stream()
                .filter(Objects::nonNull)
                .toList();

        return (root, query, cb) -> {
            List<Predicate> predicates = present.stream()
                    .map(spec -> spec.toPredicate(root, query, cb))
                    .filter(Objects::nonNull)
                    .toList();
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void validateTrainingDate(OffsetDateTime trainingDate) {
        final int MAX_SCHEDULING_WINDOW_MONTHS = 1;
        Instant now = Instant.now();
        Instant latestAllowed = now.atOffset(ZoneOffset.UTC)
                .plusMonths(MAX_SCHEDULING_WINDOW_MONTHS)
                .toInstant();
        Instant requestedDate = trainingDate.toInstant();

        if (!requestedDate.isAfter(now)) {
            throw new BusinessValidationException(List.of(
                    new ErrorResponse.FieldError(
                            "trainingDate",
                            "Training date must be in the future")));
        }

        if (requestedDate.isAfter(latestAllowed)) {
            throw new BusinessValidationException(List.of(
                    new ErrorResponse.FieldError(
                            "trainingDate",
                            "Training date must be within the next month")));
        }
    }
}
