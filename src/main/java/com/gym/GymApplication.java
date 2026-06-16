package com.gym;

import com.gym.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GymApplication {

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {

        }
    }
}