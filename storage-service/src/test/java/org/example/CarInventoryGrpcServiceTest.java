package org.example;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.domain.enums.CarColor;
import org.example.domain.model.Car;
import org.example.dto.response.CarResponse;
import org.example.grpc.CarInventoryGrpcService;
import org.example.grpc.AvailableCarResponse;
import org.example.grpc.GetAvailableCarByIdRequest;
import org.example.grpc.GetAvailableCarsRequest;
import org.example.mappers.CarMapper;
import org.example.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CarInventoryGrpcServiceTest {

    private final CarRepository carRepository = mock(CarRepository.class);
    private final CarMapper carMapper = mock(CarMapper.class);

    private CarInventoryGrpcService carInventoryGrpcService;

    @BeforeEach
    void setUp() {
        carInventoryGrpcService = new CarInventoryGrpcService(carRepository, carMapper);
    }

    @Test
    void shouldReturnOnlyAvailableCars() {
        Car car = mock(Car.class);
        UUID carId = UUID.randomUUID();
        UUID carModelId = UUID.randomUUID();

        when(carRepository.findAllByAvailableTrueAndRemovedFalse()).thenReturn(List.of(car));
        when(carMapper.toResponse(car)).thenReturn(carResponse(carId, carModelId, true));

        CapturingObserver<org.example.grpc.GetAvailableCarsResponse> observer = new CapturingObserver<>();

        carInventoryGrpcService.getAvailableCars(GetAvailableCarsRequest.newBuilder().build(), observer);

        assertThat(observer.value.getCarsList()).hasSize(1);
        assertThat(observer.value.getCars(0).getId()).isEqualTo(carId.toString());
        assertThat(observer.value.getCars(0).getAvailable()).isTrue();
        assertThat(observer.completed).isTrue();
        verify(carRepository).findAllByAvailableTrueAndRemovedFalse();
    }

    @Test
    void shouldReturnEmptyListWhenNoAvailableCars() {
        when(carRepository.findAllByAvailableTrueAndRemovedFalse()).thenReturn(List.of());

        CapturingObserver<org.example.grpc.GetAvailableCarsResponse> observer = new CapturingObserver<>();

        carInventoryGrpcService.getAvailableCars(GetAvailableCarsRequest.newBuilder().build(), observer);

        assertThat(observer.value.getCarsList()).isEmpty();
        assertThat(observer.completed).isTrue();
    }

    @Test
    void shouldReturnAvailableCarById() {
        UUID carId = UUID.randomUUID();
        UUID carModelId = UUID.randomUUID();
        Car car = mock(Car.class);

        when(car.isAvailable()).thenReturn(true);
        when(carRepository.findByIdAndRemovedFalse(carId)).thenReturn(Optional.of(car));
        when(carMapper.toResponse(car)).thenReturn(carResponse(carId, carModelId, true));

        CapturingObserver<AvailableCarResponse> observer = new CapturingObserver<>();

        carInventoryGrpcService.getAvailableCarById(
                GetAvailableCarByIdRequest.newBuilder().setId(carId.toString()).build(),
                observer
        );

        assertThat(observer.value.getId()).isEqualTo(carId.toString());
        assertThat(observer.value.getCarModelDisplayName()).isEqualTo("BMW 320i");
        assertThat(observer.completed).isTrue();
    }

    @Test
    void shouldReturnNotFoundForMissingCar() {
        UUID carId = UUID.randomUUID();
        when(carRepository.findByIdAndRemovedFalse(carId)).thenReturn(Optional.empty());

        CapturingObserver<AvailableCarResponse> observer = new CapturingObserver<>();

        carInventoryGrpcService.getAvailableCarById(
                GetAvailableCarByIdRequest.newBuilder().setId(carId.toString()).build(),
                observer
        );

        assertThat(observer.error)
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(error -> ((StatusRuntimeException) error).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForUnavailableCar() {
        UUID carId = UUID.randomUUID();
        Car car = mock(Car.class);

        when(car.isAvailable()).thenReturn(false);
        when(carRepository.findByIdAndRemovedFalse(carId)).thenReturn(Optional.of(car));

        CapturingObserver<AvailableCarResponse> observer = new CapturingObserver<>();

        carInventoryGrpcService.getAvailableCarById(
                GetAvailableCarByIdRequest.newBuilder().setId(carId.toString()).build(),
                observer
        );

        assertThat(observer.error)
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(error -> ((StatusRuntimeException) error).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }

    private CarResponse carResponse(UUID carId, UUID carModelId, boolean available) {
        return new CarResponse(
                carId,
                carModelId,
                "BMW 320i",
                "VIN-BMW-320I-0001",
                CarColor.BLACK,
                new BigDecimal("3500000.00"),
                available,
                true
        );
    }

    private static class CapturingObserver<T> implements StreamObserver<T> {
        private T value;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable throwable) {
            this.error = throwable;
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }
}
