package com.utetra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class UtetraApplication extends SpringBootServletInitializer {

    // Cho phep trien khai file WAR len Tomcat ngoai
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(UtetraApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(UtetraApplication.class, args);
    }
}
