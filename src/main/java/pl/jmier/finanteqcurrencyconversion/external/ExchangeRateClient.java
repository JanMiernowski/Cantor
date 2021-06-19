package pl.jmier.finanteqcurrencyconversion.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.jmier.finanteqcurrencyconversion.config.NbpProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ExchangeRateClient {
  private final RestTemplate restTemplate;
  private final NbpProperties nbpProperties;
  private final ObjectMapper objectMapper;

  public CurrencyRateResponse findCurrencyRate(String currency, LocalDate forDate)
      throws JsonProcessingException {
    String address =
        String.format("%s/exchangerates/rates/c/%s/%s", nbpProperties.getUrl(), currency, forDate);

    ResponseEntity<String> rateResponse;
    try {
      rateResponse = restTemplate.exchange(address, HttpMethod.GET, HttpEntity.EMPTY, String.class);
    } catch (HttpClientErrorException ex) {
      rateResponse = new ResponseEntity<String>(ex.getStatusCode());
    }

    if (rateResponse.getStatusCode().is2xxSuccessful()) {
      return objectMapper.readValue(rateResponse.getBody(), CurrencyRateResponse.class);
    }

    if (rateResponse.getStatusCode().is4xxClientError()) {
      if (LocalDate.now().isAfter(forDate.plusDays(10))) {
        throw new RuntimeException("Currency " + currency + " not found.");
      }
      return findCurrencyRate(currency, forDate.minusDays(1));
    }

    throw new IllegalStateException(
        "It was impossible to fetch currency rate " + rateResponse.getStatusCodeValue());
  }
}
