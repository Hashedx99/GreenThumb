package com.greenthumb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GreenThumb Application Entry Point.
 * <p>
 * Houseplant management REST API built with Spring Boot.
 * Supports JWT authentication, plant care scheduling,
 * Cloudinary image uploads, and Perenual API integration.
 * </p>
 *
 * @author Hamza Ali
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class GreenThumbApplication {

    /**
     * Main method — bootstraps the Spring Boot application.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(GreenThumbApplication.class, args);
    }
}
