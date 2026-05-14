package org.example.config.security;

import lombok.RequiredArgsConstructor;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.example.repository.TestDriveRequestRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("orderSecurityService")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final CurrentUserService currentUserService;
    private final InStockCarOrderRepository inStockCarOrderRepository;
    private final CustomCarOrderRepository customCarOrderRepository;
    private final TestDriveRequestRepository testDriveRequestRepository;

    public boolean isInStockOrderOwner(UUID orderId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return inStockCarOrderRepository.findByIdAndRemovedFalse(orderId)
                .map(order -> order.getClient().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isCustomOrderOwner(UUID orderId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return customCarOrderRepository.findByIdAndRemovedFalse(orderId)
                .map(order -> order.getClient().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isTestDriveRequestOwner(UUID requestId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return testDriveRequestRepository.findByIdAndRemovedFalse(requestId)
                .map(order -> order.getClient().getId().equals(currentUserId))
                .orElse(false);
    }
}
