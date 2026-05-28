package pl.net.karion.SpotRacer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpotRacerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpotRacerApplication.class, args);
	}

}
