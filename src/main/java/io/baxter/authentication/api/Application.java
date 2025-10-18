package io.baxter.authentication.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(scanBasePackages = {
        "io.baxter.authentication.api",
        "io.baxter.authentication.infrastructure",
        "io.baxter.authentication.data"
})
@EnableR2dbcRepositories(basePackages = "io.baxter.authentication.data.repository")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
