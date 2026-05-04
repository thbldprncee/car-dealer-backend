package org.example;

import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.ComponentOptionRepository;
import org.example.repository.SparePartRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private ComponentOptionRepository componentOptionRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private SparePartRepository sparePartRepository;

    @Test
    void shouldLoadSeedUsersFromDatabase() {
        assertThat(userRepository.findById(CLIENT_ID)).isPresent();
        assertThat(userRepository.findById(SALES_MANAGER_ID)).isPresent();
        assertThat(userRepository.findById(WAREHOUSE_ADMIN_ID)).isPresent();
        assertThat(userRepository.findById(ADMIN_ID)).isPresent();

        assertThat(userRepository.count()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void shouldLoadSeedInventoryFromDatabase() {
        assertThat(carModelRepository.findById(BMW_320I_MODEL_ID)).isPresent();

        assertThat(componentOptionRepository.findById(WHEELS_STANDARD_ID)).isPresent();
        assertThat(componentOptionRepository.findById(TRANSMISSION_ID)).isPresent();
        assertThat(componentOptionRepository.findById(STEERING_STANDARD_ID)).isPresent();
        assertThat(componentOptionRepository.findById(INTERIOR_FABRIC_ID)).isPresent();

        assertThat(carRepository.findById(SEED_CAR_1_ID)).isPresent();
        assertThat(carRepository.findById(SEED_CAR_2_ID)).isPresent();

        assertThat(sparePartRepository.findById(SPARE_PART_ID)).isPresent();
    }
}
