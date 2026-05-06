package org.example.config.security;

import lombok.RequiredArgsConstructor;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("orderSecurityService")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final CurrentUserService currentUserService;
    private final InStockCarOrderRepository inStockCarOrderRepository;
    private final CustomCarOrderRepository customCarOrderRepository;

    public boolean isInStockOrderOwner(UUID orderId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return inStockCarOrderRepository.findById(orderId)
                .filter(order -> !order.isRemoved())
                .map(order -> order.getClient().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isCustomOrderOwner(UUID orderId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return customCarOrderRepository.findById(orderId)
                .filter(order -> !order.isRemoved())
                .map(order -> order.getClient().getId().equals(currentUserId))
                .orElse(false);
    }
}