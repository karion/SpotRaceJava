package pl.net.karion.SpotRacer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SpotRacerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpotRacerApplication.class, args);
	}

}
