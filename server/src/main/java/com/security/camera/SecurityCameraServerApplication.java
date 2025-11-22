package com.security.camera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SecurityCameraServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityCameraServerApplication.class, args);
        System.out.println("===========================================");
        System.out.println("Security Camera Server Started Successfully");
        System.out.println("API Documentation: http://localhost:8080");
        System.out.println("===========================================");
    }
}
