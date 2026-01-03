package com.padmasiniAdmin.padmasiniAdmin_1;
import java.time.LocalTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PadmasiniAdmin1Application {

    public static void main(String[] args) {
        SpringApplication.run(PadmasiniAdmin1Application.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                            "https://d2i6kp2bub3ddu.cloudfront.net",
                            "https://padmasini.com/Learnforward/",
                            "http://localhost:5173",
                            "http://localhost:5173/Learnforward/",
                            "https://padmasini.com",
                            "http://localhost:5174",
                            "https://studentfrontendpage.netlify.app",
                            "https://padmasini7-frontend.netlify.app",
                            "https://d2kr3vc90ue6me.cloudfront.net",
                            "https://trilokinnovations.com",
                            "https://www.trilokinnovations.com",
                            "https://majestic-frangollo-031fed.netlify.app"
                            
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
