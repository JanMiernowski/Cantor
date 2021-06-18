package pl.jmier.finanteqcurrencyconversion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "nbp")
public class NbpProperties {
    private String url;
}
