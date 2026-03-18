package org.example.domain.model;

import org.example.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SparePartTest {

    @Test
    void shouldCreateSparePart() {
        UUID modelId = UUID.randomUUID();

        SparePart sparePart = new SparePart(
                UUID.randomUUID(),
                "Brake Pads",
                "Front brake pads",
                new BigDecimal("15000"),
                Set.of(modelId)
        );

        assertEquals("Brake Pads", sparePart.name());
        assertEquals("Front brake pads", sparePart.description());
        assertEquals(new BigDecimal("15000"), sparePart.price());
        assertTrue(sparePart.isCompatibleWith(modelId));
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThrows(DomainValidationException.class, () ->
                new SparePart(
                        null,
                        "Brake Pads",
                        "Front brake pads",
                        new BigDecimal("15000"),
                        Set.of(UUID.randomUUID())
                )
        );
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThrows(DomainValidationException.class, () ->
                new SparePart(
                        UUID.randomUUID(),
                        "   ",
                        "Front brake pads",
                        new BigDecimal("15000"),
                        Set.of(UUID.randomUUID())
                )
        );
    }

    @Test
    void shouldThrowWhenDescriptionIsBlank() {
        assertThrows(DomainValidationException.class, () ->
                new SparePart(
                        UUID.randomUUID(),
                        "Brake Pads",
                        "   ",
                        new BigDecimal("15000"),
                        Set.of(UUID.randomUUID())
                )
        );
    }

    @Test
    void shouldThrowWhenPriceIsNegative() {
        assertThrows(DomainValidationException.class, () ->
                new SparePart(
                        UUID.randomUUID(),
                        "Brake Pads",
                        "Front brake pads",
                        new BigDecimal("-1"),
                        Set.of(UUID.randomUUID())
                )
        );
    }

    @Test
    void shouldThrowWhenCompatibleCarModelIdsAreEmpty() {
        assertThrows(DomainValidationException.class, () ->
                new SparePart(
                        UUID.randomUUID(),
                        "Brake Pads",
                        "Front brake pads",
                        new BigDecimal("15000"),
                        Set.of()
                )
        );
    }
}