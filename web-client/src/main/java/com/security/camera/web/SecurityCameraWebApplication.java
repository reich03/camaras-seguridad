package com.security.camera.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecurityCameraWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityCameraWebApplication.class, args);
        System.out.println("===========================================");
        System.out.println("Security Camera Web Client Started");
        System.out.println("Access at: http://localhost:8081");
        System.out.println("===========================================");
    }
}
