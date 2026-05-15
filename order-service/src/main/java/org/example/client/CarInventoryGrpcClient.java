package org.example.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import org.example.config.properties.StorageGrpcProperties;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.exceptions.GrpcServiceUnavailableException;
import org.example.dto.response.AvailableCarResponse;
import org.example.grpc.CarInventoryServiceGrpc;
import org.example.grpc.GetAvailableCarByIdRequest;
import org.example.grpc.GetAvailableCarsRequest;
import org.example.mappers.AvailableCarGrpcMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CarInventoryGrpcClient implements CarInventoryClient {

    private final ManagedChannel channel;
    private final CarInventoryServiceGrpc.CarInventoryServiceBlockingStub stub;
    private final AvailableCarGrpcMapper availableCarGrpcMapper;
    private final long timeoutMs;

    public CarInventoryGrpcClient(
            StorageGrpcProperties properties,
            AvailableCarGrpcMapper availableCarGrpcMapper
    ) {
        this.channel = ManagedChannelBuilder.forAddress(properties.host(), properties.port())
                .usePlaintext()
                .build();
        this.stub = CarInventoryServiceGrpc.newBlockingStub(channel);
        this.availableCarGrpcMapper = availableCarGrpcMapper;
        this.timeoutMs = properties.timeoutMs();
    }

    @Override
    public List<AvailableCarResponse> getAvailableCars() {
        try {
            return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                    .getAvailableCars(GetAvailableCarsRequest.newBuilder().build())
                    .getCarsList()
                    .stream()
                    .map(availableCarGrpcMapper::toDto)
                    .toList();
        } catch (StatusRuntimeException exception) {
            throw mapGrpcException(exception, null);
        } catch (RuntimeException exception) {
            throw new GrpcServiceUnavailableException("StorageService gRPC call failed", exception);
        }
    }

    @Override
    public AvailableCarResponse getAvailableCarById(UUID id) {
        try {
            return availableCarGrpcMapper.toDto(stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                    .getAvailableCarById(GetAvailableCarByIdRequest.newBuilder()
                            .setId(id.toString())
                            .build()));
        } catch (StatusRuntimeException exception) {
            throw mapGrpcException(exception, id);
        } catch (RuntimeException exception) {
            throw new GrpcServiceUnavailableException("StorageService gRPC call failed", exception);
        }
    }

    @PreDestroy
    void shutdown() {
        channel.shutdown();
    }

    private RuntimeException mapGrpcException(StatusRuntimeException exception, UUID carId) {
        Status.Code code = exception.getStatus().getCode();

        if (code == Status.Code.NOT_FOUND && carId != null) {
            return new EntityNotFoundException("Car", carId);
        }

        if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
            return new GrpcServiceUnavailableException("StorageService is unavailable", exception);
        }

        return new GrpcServiceUnavailableException("StorageService gRPC call failed", exception);
    }
}
