package com.example.springboot.docker;

import java.text.ParseException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class BackendApplication {

	public static void main(String[] args) throws ParseException {
		SpringApplication.run(BackendApplication.class, args);
	}
}
