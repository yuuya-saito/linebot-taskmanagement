package com.ys.linebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories("com.ys.linebot")
@EnableScheduling
public class LinebotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinebotApplication.class, args);
	}

}
