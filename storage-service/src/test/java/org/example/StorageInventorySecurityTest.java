package org.example;

import org.example.presentation.InventoryController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StorageInventorySecurityTest {

    @Test
    void internalInventoryEndpointsDoNotExposeCarsOrSparePartsToManagerOrUser() throws NoSuchMethodException {
        assertWarehouseOrAdmin("getCarById");
        assertWarehouseOrAdmin("getAllCars");
        assertWarehouseOrAdmin("getSparePartById");
        assertWarehouseOrAdmin("getAllSpareParts");
    }

    private void assertWarehouseOrAdmin(String methodName) throws NoSuchMethodException {
        Class<?>[] parameterTypes = methodName.endsWith("ById")
                ? new Class<?>[] {UUID.class}
                : new Class<?>[] {};
        PreAuthorize preAuthorize = InventoryController.class
                .getMethod(methodName, parameterTypes)
                .getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')");
        assertThat(preAuthorize.value()).doesNotContain("USER", "MANAGER");
    }
}
