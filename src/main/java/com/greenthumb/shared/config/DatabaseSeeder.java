package com.greenthumb.shared.config;

import com.greenthumb.feature.care.CareSchedule;
import com.greenthumb.feature.care.CareScheduleRepository;
import com.greenthumb.feature.care.CareType;
import com.greenthumb.feature.plant.PlantStatus;
import com.greenthumb.feature.plant.UserPlant;
import com.greenthumb.feature.plant.UserPlantRepository;
import com.greenthumb.feature.species.PlantSpecies;
import com.greenthumb.feature.species.PlantSpeciesRepository;
import com.greenthumb.feature.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with initial data on first application startup.
 * <p>
 * Creates 1 admin, 3 users, 6 plant species, and sample plants with
 * care schedules. Only runs if no users exist in the database.
 * </p>
 *
 * @author Hamza Ali
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PlantSpeciesRepository speciesRepository;
    private final UserPlantRepository plantRepository;
    private final CareScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Entry point called by Spring Boot after application context loads.
     * Skips seeding if data already exists.
     *
     * @param args command-line arguments (unused)
     */
    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database...");
        seedUsers();
        seedSpecies();
        seedPlantsAndSchedules();
        log.info("Database seeding complete.");
    }

    /**
     * Creates 1 admin and 3 regular users with pre-verified accounts.
     */
    private void seedUsers() {
        // Admin account
        userRepository.save(User.builder()
                .name("GreenThumb Admin")
                .email("admin@greenthumb.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Platform administrator")
                .build());

        // Regular users
        userRepository.save(User.builder()
                .name("Hamza Ali")
                .email("hamza@greenthumb.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Plant enthusiast with a passion for tropical species")
                .location("Bahrain")
                .build());

        userRepository.save(User.builder()
                .name("Jasim Ahmed")
                .email("jasim@greenthumb.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Succulent collector")
                .location("Manama")
                .build());

        userRepository.save(User.builder()
                .name("Ali Hassan")
                .email("ali@greenthumb.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Beginner plant parent")
                .location("Riffa")
                .build());

        log.info("Users seeded: 1 admin + 3 users");
    }

    /**
     * Creates 6 plant species covering a range of care difficulties and toxicity.
     */
    private void seedSpecies() {
        User admin = userRepository.findByEmail("admin@greenthumb.com").orElseThrow();

        List<PlantSpecies> species = List.of(
                PlantSpecies.builder()
                        .commonName("Peace Lily")
                        .scientificName("Spathiphyllum wallisii")
                        .wateringFrequencyDays(7)
                        .lightRequirement("Low to indirect light")
                        .toxicToCats(true)
                        .createdByAdmin(admin)
                        .build(),
                PlantSpecies.builder()
                        .commonName("Snake Plant")
                        .scientificName("Sansevieria trifasciata")
                        .wateringFrequencyDays(14)
                        .lightRequirement("Low to bright indirect light")
                        .toxicToCats(true)
                        .createdByAdmin(admin)
                        .build(),
                PlantSpecies.builder()
                        .commonName("Spider Plant")
                        .scientificName("Chlorophytum comosum")
                        .wateringFrequencyDays(7)
                        .lightRequirement("Bright indirect light")
                        .toxicToCats(false)
                        .createdByAdmin(admin)
                        .build(),
                PlantSpecies.builder()
                        .commonName("Phalaenopsis Orchid")
                        .scientificName("Phalaenopsis amabilis")
                        .wateringFrequencyDays(10)
                        .lightRequirement("Bright indirect light")
                        .toxicToCats(false)
                        .createdByAdmin(admin)
                        .build(),
                PlantSpecies.builder()
                        .commonName("Pothos")
                        .scientificName("Epipremnum aureum")
                        .wateringFrequencyDays(7)
                        .lightRequirement("Low to medium indirect light")
                        .toxicToCats(true)
                        .createdByAdmin(admin)
                        .build(),
                PlantSpecies.builder()
                        .commonName("Aloe Vera")
                        .scientificName("Aloe barbadensis")
                        .wateringFrequencyDays(21)
                        .lightRequirement("Bright direct light")
                        .toxicToCats(true)
                        .createdByAdmin(admin)
                        .build()
        );

        speciesRepository.saveAll(species);
        log.info("Species seeded: 6 entries");
    }

    /**
     * Creates sample plants for users with linked care schedules.
     */
    private void seedPlantsAndSchedules() {
        User hamza = userRepository.findByEmail("hamza@greenthumb.com").orElseThrow();
        User jasim = userRepository.findByEmail("jasim@greenthumb.com").orElseThrow();

        PlantSpecies orchid = speciesRepository.findAll().stream()
                .filter(s -> s.getCommonName().equals("Phalaenopsis Orchid"))
                .findFirst().orElseThrow();
        PlantSpecies snakePlant = speciesRepository.findAll().stream()
                .filter(s -> s.getCommonName().equals("Snake Plant"))
                .findFirst().orElseThrow();
        PlantSpecies spiderPlant = speciesRepository.findAll().stream()
                .filter(s -> s.getCommonName().equals("Spider Plant"))
                .findFirst().orElseThrow();

        // Hamza's orchid
        UserPlant hamzaOrchid = plantRepository.save(UserPlant.builder()
                .user(hamza)
                .species(orchid)
                .nickname("Orchie")
                .acquiredDate(LocalDate.now().minusMonths(2))
                .location("Living Room")
                .notes("Got this as a gift. Blooming beautifully.")
                .status(PlantStatus.HEALTHY)
                .build());

        // jasim's snake plant
        UserPlant jasimSnake = plantRepository.save(UserPlant.builder()
                .user(jasim)
                .species(snakePlant)
                .nickname("Snakey")
                .acquiredDate(LocalDate.now().minusMonths(6))
                .location("Bedroom")
                .notes("Very low maintenance, thriving!")
                .status(PlantStatus.HEALTHY)
                .build());

        // jasim's spider plant
        UserPlant jasimSpider = plantRepository.save(UserPlant.builder()
                .user(jasim)
                .species(spiderPlant)
                .nickname("Webby")
                .acquiredDate(LocalDate.now().minusMonths(1))
                .location("Kitchen")
                .status(PlantStatus.HEALTHY)
                .build());

        // Care schedules for seeded plants
        scheduleRepository.saveAll(List.of(
                CareSchedule.builder()
                        .userPlant(hamzaOrchid)
                        .careType(CareType.WATER)
                        .intervalDays(10)
                        .nextDueDate(LocalDate.now().plusDays(3))
                        .isActive(true)
                        .build(),
                CareSchedule.builder()
                        .userPlant(jasimSnake)
                        .careType(CareType.WATER)
                        .intervalDays(14)
                        .nextDueDate(LocalDate.now())  // Due today — for demo
                        .isActive(true)
                        .build(),
                CareSchedule.builder()
                        .userPlant(jasimSpider)
                        .careType(CareType.WATER)
                        .intervalDays(7)
                        .nextDueDate(LocalDate.now().minusDays(1))  // Overdue — for demo
                        .isActive(true)
                        .build(),
                CareSchedule.builder()
                        .userPlant(jasimSpider)
                        .careType(CareType.FERTILISE)
                        .intervalDays(30)
                        .nextDueDate(LocalDate.now().plusDays(15))
                        .isActive(true)
                        .build()
        ));

        log.info("Plants and care schedules seeded");
    }
}
