package com.azure.acme.assist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX, pattern = "com.azure.acme.assist.tools.*"))
public class FitAssistApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitAssistApplication.class, args);
    }
}
