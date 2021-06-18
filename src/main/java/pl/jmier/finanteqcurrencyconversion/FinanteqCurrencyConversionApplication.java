package pl.jmier.finanteqcurrencyconversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.jmier.finanteqcurrencyconversion.config.NbpProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {NbpProperties.class})
public class FinanteqCurrencyConversionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanteqCurrencyConversionApplication.class, args);
	}

}
