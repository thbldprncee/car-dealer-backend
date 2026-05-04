package org.example.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.domain.enums.TestDriveStatus;
import org.example.domain.exceptions.DomainValidationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@Table(name = "test_drive_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class TestDriveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    @ToString.Exclude
    private Car car;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TestDriveStatus status;

    public TestDriveRequest(
            UUID id,
            Client client,
            Car car,
            LocalDateTime startDateTime,
            TestDriveStatus status
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Test drive request id must not be null"));
        this.client = Optional.ofNullable(client)
                .orElseThrow(() -> new DomainValidationException("Client must not be null"));
        this.car = Optional.ofNullable(car)
                .orElseThrow(() -> new DomainValidationException("Car must not be null"));
        this.startDateTime = Optional.ofNullable(startDateTime)
                .orElseThrow(() -> new DomainValidationException("Test drive start date time must not be null"));
        this.status = Optional.ofNullable(status)
                .orElseThrow(() -> new DomainValidationException("Test drive status must not be null"));

        validateCarAvailabilityForTestDrive();
    }

    public void approve() {
        ensureStatus(
                TestDriveStatus.CREATED,
                "Only a test drive request in CREATED status can be approved"
        );
        this.status = TestDriveStatus.APPROVED;
    }

    public void complete() {
        ensureStatus(
                TestDriveStatus.APPROVED,
                "Only an approved test drive request can be completed"
        );
        this.status = TestDriveStatus.COMPLETED;
    }

    public void cancel() {
        if (status == TestDriveStatus.COMPLETED) {
            throw new DomainValidationException("A completed test drive request cannot be canceled");
        }

        if (status == TestDriveStatus.CANCELED) {
            throw new DomainValidationException("Test drive request is already canceled");
        }

        this.status = TestDriveStatus.CANCELED;
    }

    private void validateCarAvailabilityForTestDrive() {
        if (!car.isAvailable()) {
            throw new DomainValidationException("Selected car is not available");
        }

        if (!car.isAvailableForTestDrive()) {
            throw new DomainValidationException("Selected car is not available for test drive");
        }
    }

    private void ensureStatus(TestDriveStatus expected, String message) {
        if (status != expected) {
            throw new DomainValidationException(message);
        }
    }
}