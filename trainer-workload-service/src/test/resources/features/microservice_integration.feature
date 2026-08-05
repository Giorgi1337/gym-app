Feature: CRM and workload service integration
  CRM workload events must be consumable by the workload microservice.

  Scenario: A CRM training event updates the trainer workload
    Given a CRM training for trainer "Nika.Beridze" on 2026-08-20 lasting 45 minutes
    When the CRM publishes an ADD event and the workload service consumes it
    Then the integrated workload for August 2026 is 45 minutes

  Scenario: An invalid CRM event is rejected at the workload boundary
    Given a CRM training event with a blank trainer username
    When the workload service consumes the invalid event
    Then the event is sent to the workload dead letter queue
    And no integrated workload is persisted
