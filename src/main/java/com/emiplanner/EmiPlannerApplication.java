package com.emiplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EmiPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmiPlannerApplication.class, args);
	}

}
