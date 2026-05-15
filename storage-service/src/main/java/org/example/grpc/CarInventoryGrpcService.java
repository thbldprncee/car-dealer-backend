package org.example.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.model.Car;
import org.example.mappers.CarMapper;
import org.example.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarInventoryGrpcService extends CarInventoryServiceGrpc.CarInventoryServiceImplBase {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional(readOnly = true)
    public void getAvailableCars(
            GetAvailableCarsRequest request,
            StreamObserver<GetAvailableCarsResponse> responseObserver
    ) {
        log.info("Received gRPC GetAvailableCars request");

        GetAvailableCarsResponse response = GetAvailableCarsResponse.newBuilder()
                .addAllCars(carRepository.findAllByAvailableTrueAndRemovedFalse().stream()
                        .map(carMapper::toResponse)
                        .map(this::toGrpcResponse)
                        .toList())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void getAvailableCarById(
            GetAvailableCarByIdRequest request,
            StreamObserver<AvailableCarResponse> responseObserver
    ) {
        log.info("Received gRPC GetAvailableCarById request id={}", request.getId());

        UUID id = parseId(request.getId(), responseObserver);
        if (id == null) {
            return;
        }

        carRepository.findByIdAndRemovedFalse(id)
                .filter(Car::isAvailable)
                .map(carMapper::toResponse)
                .map(this::toGrpcResponse)
                .ifPresentOrElse(response -> {
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Available car with id=%s was not found".formatted(id))
                        .asRuntimeException()));
    }

    private UUID parseId(String id, StreamObserver<AvailableCarResponse> responseObserver) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid car id: " + id)
                    .asRuntimeException());
            return null;
        }
    }

    private AvailableCarResponse toGrpcResponse(org.example.dto.response.CarResponse car) {
        return AvailableCarResponse.newBuilder()
                .setId(car.id().toString())
                .setCarModelId(car.carModelId().toString())
                .setCarModelDisplayName(car.carModelDisplayName())
                .setVin(car.vin())
                .setColor(car.color().name())
                .setPrice(car.price().toPlainString())
                .setAvailable(car.available())
                .setAvailableForTestDrive(car.availableForTestDrive())
                .build();
    }
}
