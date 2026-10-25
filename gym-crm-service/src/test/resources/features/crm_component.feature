Feature: Gym CRM training component
  The CRM component persists valid trainings and publishes workload changes.

  Scenario: Add a training for known users
    Given trainer "Nika.Beridze" and trainee "Giorgi.Kapanadze" exist
    When a 60 minute training is added for them
    Then the training is persisted
    And an ADD workload event is published

  Scenario: Reject a training for an unknown trainer
    Given trainer "missing" does not exist
    When a 60 minute training is added for trainer "missing"
    Then the CRM reports that the trainer was not found
    And no training or workload event is produced
