Feature: Trainer workload component
  The workload component aggregates valid events and reports missing summaries.

  Scenario: Aggregate a valid workload event
    Given no workload exists for trainer "Nika.Beridze"
    When an ADD workload event of 60 minutes is applied in August 2026
    Then the trainer's August 2026 workload is 60 minutes

  Scenario: Request a summary for an unknown trainer
    Given no workload exists for trainer "missing"
    When the summary for "missing" is requested
    Then the workload component reports that the trainer was not found
