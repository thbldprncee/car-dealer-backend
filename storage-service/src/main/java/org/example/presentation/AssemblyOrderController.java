package org.example.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.CreateAssemblyOrderRequest;
import org.example.dto.request.UpdateAssemblyOrderRequest;
import org.example.dto.response.AssemblyOrderResponse;
import org.example.service.AssemblyOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assembly-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
public class AssemblyOrderController {

    private final AssemblyOrderService assemblyOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssemblyOrderResponse createAssemblyOrder(@Valid @RequestBody CreateAssemblyOrderRequest request) {
        return assemblyOrderService.createAssemblyOrder(request);
    }

    @GetMapping
    public List<AssemblyOrderResponse> getAllAssemblyOrders() {
        return assemblyOrderService.getAllAssemblyOrders();
    }

    @GetMapping("/{id}")
    public AssemblyOrderResponse getAssemblyOrderById(@PathVariable UUID id) {
        return assemblyOrderService.getAssemblyOrderById(id);
    }

    @PutMapping("/{id}")
    public AssemblyOrderResponse updateAssemblyOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssemblyOrderRequest request
    ) {
        return assemblyOrderService.updateAssemblyOrder(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssemblyOrder(@PathVariable UUID id) {
        assemblyOrderService.deleteAssemblyOrder(id);
    }
}
