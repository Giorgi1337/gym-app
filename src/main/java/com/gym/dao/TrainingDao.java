package com.gym.dao;

import com.gym.model.Training;
import org.hibernate.SessionFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainingDao {

    private final SessionFactory sessionFactory;

    public TrainingDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Training training) {
        sessionFactory.getCurrentSession().persist(training);
    }

    public List<Training> findByTraineeUsername(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType) {

        StringBuilder hql = new StringBuilder("""
                SELECT t FROM Training t
                JOIN FETCH t.trainee tr
                JOIN FETCH tr.user tru
                JOIN FETCH t.trainer trn
                JOIN FETCH trn.user trnu
                JOIN FETCH t.trainingType tt
                WHERE tru.username = :username
                """
        );

        if (fromDate != null) hql.append(" AND t.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" AND t.trainingDate <= :toDate");
        if (trainerName != null && !trainerName.isBlank()) hql.append(" AND trnu.username = :trainerName");
        if (trainingType != null && !trainingType.isBlank())
            hql.append(" AND LOWER(tt.trainingTypeName) = LOWER(:trainingType)");

        var query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isBlank()) query.setParameter("trainerName", trainerName);
        if (trainingType != null && !trainingType.isBlank()) query.setParameter("trainingType", trainingType);

        return query.getResultList();
    }

    public List<Training> findByTrainerUsername(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        StringBuilder hql = getStringBuilder(fromDate, toDate, traineeName);

        var query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.isBlank()) query.setParameter("traineeName", traineeName);

        return query.getResultList();
    }

    private static @NonNull StringBuilder getStringBuilder(LocalDate fromDate, LocalDate toDate, String traineeName) {
        StringBuilder hql = new StringBuilder("""
                SELECT t FROM Training t
                JOIN FETCH t.trainee tr
                JOIN FETCH tr.user tru
                JOIN FETCH t.trainer trn
                JOIN FETCH trn.user trnu
                JOIN FETCH t.trainingType tt
                WHERE trnu.username = :username
                """
        );

        if (fromDate != null) hql.append(" AND t.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" AND t.trainingDate <= :toDate");
        if (traineeName != null && !traineeName.isBlank()) hql.append(" AND tru.username = :traineeName");
        return hql;
    }

}
