package br.com.farabolini.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ChallengeItauApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChallengeItauApplication.class, args);
	}

}
