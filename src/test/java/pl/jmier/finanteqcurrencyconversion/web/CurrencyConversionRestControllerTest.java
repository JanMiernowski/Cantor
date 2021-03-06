package pl.jmier.finanteqcurrencyconversion.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pl.jmier.finanteqcurrencyconversion.config.RestTemplateConfig;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionRequest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
    classes = RestTemplateConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CurrencyConversionRestControllerTest {

  @LocalServerPort private Integer port;

  @Autowired private RestTemplate restTemplate;

  @Autowired private CurrencyConversionRestController currencyConversionRestController;

  @Test
  void shouldReturnStatus200ForCorrectValues() {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("eur", "pln", BigDecimal.valueOf(100));
    HttpEntity<CurrencyConversionRequest> entity = new HttpEntity<>(currencyConversionRequest);
    // when
    ResponseEntity<CurrencyConversionResponse> responsePost =
        restTemplate.exchange(
            String.format("http://localHost:%d/api", port),
            HttpMethod.POST,
            entity,
            CurrencyConversionResponse.class);
    ResponseEntity<CurrencyConversionResponse> responseGet =
        restTemplate.exchange(
            String.format(
                "http://localHost:%d/api?currencyInput=eur&currencyOutput=pln&transactionType=sell&amount=100",
                port),
            HttpMethod.GET,
            entity,
            CurrencyConversionResponse.class);
    // then
    assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
    assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
    CurrencyConversionResponse body = responsePost.getBody();
    assertEquals(200, responsePost.getStatusCodeValue());
    assertEquals("Kwota do zwrotu", body.getMessage());
    assertNotNull(body.getAmount());
  }

  @Test
  void shouldReturnStatus400AndSpecificMessageForIncorrectInputCurrency() {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("xxx", "pln", BigDecimal.valueOf(100));
    HttpEntity<CurrencyConversionRequest> entity = new HttpEntity<>(currencyConversionRequest);
    // when
    ResponseEntity<CurrencyConversionResponse> responsePost =
        restTemplate.exchange(
            String.format("http://localHost:%d/api", port),
            HttpMethod.POST,
            entity,
            CurrencyConversionResponse.class);
    ResponseEntity<CurrencyConversionResponse> responseGet =
        restTemplate.exchange(
            String.format(
                "http://localHost:%d/api?currencyInput=xxx&currencyOutput=pln&transactionType=sell&amount=100",
                port),
            HttpMethod.GET,
            entity,
            CurrencyConversionResponse.class);
    // then
    assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
    assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
    CurrencyConversionResponse body = responsePost.getBody();
    assertEquals(400, responsePost.getStatusCodeValue());
    assertEquals("Podano niew??????ciw?? dan??: Waluta wej??ciowa.", body.getMessage());
    assertNotNull(body.getAmount());
  }

  @Test
  void shouldReturnStatus400AndSpecificMessageForIncorrectOutputCurrency() {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("pln", "xxx", BigDecimal.valueOf(100));
    HttpEntity<CurrencyConversionRequest> entity = new HttpEntity<>(currencyConversionRequest);
    // when
    ResponseEntity<CurrencyConversionResponse> responsePost =
        restTemplate.exchange(
            String.format("http://localHost:%d/api", port),
            HttpMethod.POST,
            entity,
            CurrencyConversionResponse.class);
    ResponseEntity<CurrencyConversionResponse> responseGet =
        restTemplate.exchange(
            String.format(
                "http://localHost:%d/api?currencyInput=eur&currencyOutput=xxx&transactionType=sell&amount=100",
                port),
            HttpMethod.GET,
            entity,
            CurrencyConversionResponse.class);
    // then
    assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
    assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
    CurrencyConversionResponse body = responsePost.getBody();
    assertEquals(400, responsePost.getStatusCodeValue());
    assertEquals("Podano niew??????ciw?? dan??: Waluta wyj??ciowa.", body.getMessage());
    assertNotNull(body.getAmount());
  }

  @Test
  void shouldReturnStatus400AndSpecificMessageForIncorrectAmount() {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("pln", "eur", BigDecimal.valueOf(-1));
    HttpEntity<CurrencyConversionRequest> entity = new HttpEntity<>(currencyConversionRequest);
    // when
    ResponseEntity<CurrencyConversionResponse> responsePost =
        restTemplate.exchange(
            String.format("http://localHost:%d/api", port),
            HttpMethod.POST,
            entity,
            CurrencyConversionResponse.class);
    ResponseEntity<CurrencyConversionResponse> responseGet =
        restTemplate.exchange(
            String.format(
                "http://localHost:%d/api?currencyInput=eur&currencyOutput=pln&transactionType=sell&amount=-1",
                port),
            HttpMethod.GET,
            entity,
            CurrencyConversionResponse.class);
    // then
    assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
    assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
    CurrencyConversionResponse body = responsePost.getBody();
    assertEquals(400, responsePost.getStatusCodeValue());
    assertEquals("Podano niew??????ciw?? dan??: Kwota.", body.getMessage());
    assertNotNull(body.getAmount());
  }
}
