package com.gym.service;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TrainingMapper;
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

    public TrainingService(
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            TrainingTypeRepository trainingTypeRepository,
            GymMetrics gymMetrics) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.gymMetrics = gymMetrics;
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

    @Transactional
    public void addTraining(String trainerUsername, String traineeUsername, AddTrainingRequest request) {
        Trainer trainer = trainerRepository.findByUser_Username(trainerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + trainerUsername));

        Trainee trainee = traineeRepository.findByUser_Username(traineeUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + traineeUsername));

//        validateTrainingDate(request.getTrainingDate());

        Training training = TrainingMapper.toEntity(request, trainee, trainer);
        trainingRepository.save(training);

        gymMetrics.incrementTrainingAdded();

        log.info("Added training '{}' for trainer={} trainee={}",
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

//    private void validateTrainingDate(LocalDate trainingDate) {
//        final int MAX_SCHEDULING_WINDOW_DAYS = 14;
//
//        LocalDate today = LocalDate.now();
//        LocalDate latestAllowed = today.plusDays(MAX_SCHEDULING_WINDOW_DAYS);
//
//        if (trainingDate.isBefore(today)) {
//            throw new BusinessValidationException(List.of(
//                    new ErrorResponse.FieldError("trainingDate", "Training date cannot be in the past")));
//        }
//
//        if (trainingDate.isAfter(latestAllowed)) {
//            throw new BusinessValidationException(List.of(
//                    new ErrorResponse.FieldError(
//                            "trainingDate",
//                            "Training date must be within the next %d days".formatted(MAX_SCHEDULING_WINDOW_DAYS))));
//        }
//    }
}