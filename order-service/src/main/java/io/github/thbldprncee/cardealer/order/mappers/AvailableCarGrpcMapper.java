package io.github.thbldprncee.cardealer.order.mappers;

import io.github.thbldprncee.cardealer.order.dto.response.AvailableCarResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AvailableCarGrpcMapper {

    public AvailableCarResponse toDto(io.github.thbldprncee.cardealer.grpc.AvailableCarResponse car) {
        return new AvailableCarResponse(
                UUID.fromString(car.getId()),
                UUID.fromString(car.getCarModelId()),
                car.getCarModelDisplayName(),
                car.getVin(),
                car.getColor(),
                new BigDecimal(car.getPrice()),
                car.getAvailable(),
                car.getAvailableForTestDrive()
        );
    }
}
