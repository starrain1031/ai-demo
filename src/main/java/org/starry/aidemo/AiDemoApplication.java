package org.starry.aidemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the AI demo backend.
 */
@MapperScan("org.starry.aidemo.mapper")
@SpringBootApplication
public class AiDemoApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(AiDemoApplication.class, args);
    }

}
