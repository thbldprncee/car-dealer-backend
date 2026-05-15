package org.example;

import org.example.presentation.AvailableCarController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvailableCarSecurityAnnotationTest {

    @Test
    void shouldAllowClientSalesManagerAndAdminToListAvailableCars() throws Exception {
        PreAuthorize preAuthorize = AvailableCarController.class
                .getMethod("getAvailableCars")
                .getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('CLIENT', 'SALES_MANAGER', 'ADMIN')");
    }

    @Test
    void shouldAllowClientSalesManagerAndAdminToGetAvailableCarById() throws Exception {
        PreAuthorize preAuthorize = AvailableCarController.class
                .getMethod("getAvailableCarById", UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('CLIENT', 'SALES_MANAGER', 'ADMIN')");
    }
}
