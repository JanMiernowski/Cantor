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
import pl.jmier.finanteqcurrencyconversion.external.CurrencyRateRequest;
import pl.jmier.finanteqcurrencyconversion.external.UserAmountResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = RestTemplateConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CurrencyConversionRestControllerTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyConversionRestController currencyConversionRestController;

    @Test
    void shouldReturnStatus200ForCorrectValues(){
        //given
        CurrencyRateRequest currencyRateRequest = new CurrencyRateRequest("eur", "pln", "sell", BigDecimal.valueOf(100));
        HttpEntity<CurrencyRateRequest> entity = new HttpEntity<>(currencyRateRequest);
        //when
        ResponseEntity<UserAmountResponse> responsePost = restTemplate.exchange(String.format("http://localHost:%d/api", port), HttpMethod.POST, entity, UserAmountResponse.class);
        ResponseEntity<UserAmountResponse> responseGet = restTemplate.exchange(String.format("http://localHost:%d/api?currencyInput=eur&currencyOutput=pln&transactionType=sell&amount=100", port), HttpMethod.GET, entity, UserAmountResponse.class);
        //then
        assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
        assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
        UserAmountResponse body = responsePost.getBody();
        assertEquals(200, responsePost.getStatusCodeValue());
        assertEquals("Kwota do zwrotu", body.getMessage());
        assertNotNull(body.getAmount());
    }

    @Test
    void shouldReturnStatus400AndSpecificMessageForIncorrectInputCurrency(){
        //given
        CurrencyRateRequest currencyRateRequest = new CurrencyRateRequest("xxx", "pln", "sell", BigDecimal.valueOf(100));
        HttpEntity<CurrencyRateRequest> entity = new HttpEntity<>(currencyRateRequest);
        //when
        ResponseEntity<UserAmountResponse> responsePost = restTemplate.exchange(String.format("http://localHost:%d/api", port), HttpMethod.POST, entity, UserAmountResponse.class);
        ResponseEntity<UserAmountResponse> responseGet = restTemplate.exchange(String.format("http://localHost:%d/api?currencyInput=xxx&currencyOutput=pln&transactionType=sell&amount=100", port), HttpMethod.GET, entity, UserAmountResponse.class);
        //then
        assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
        assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
        UserAmountResponse body = responsePost.getBody();
        assertEquals(400, responsePost.getStatusCodeValue());
        assertEquals("Podano niewłąściwą daną: Waluta wejściowa.", body.getMessage());
        assertNotNull(body.getAmount());
    }

    @Test
    void shouldReturnStatus400AndSpecificMessageForIncorrectOutputCurrency(){
        //given
        CurrencyRateRequest currencyRateRequest = new CurrencyRateRequest("pln", "xxx", "sell", BigDecimal.valueOf(100));
        HttpEntity<CurrencyRateRequest> entity = new HttpEntity<>(currencyRateRequest);
        //when
        ResponseEntity<UserAmountResponse> responsePost = restTemplate.exchange(String.format("http://localHost:%d/api", port), HttpMethod.POST, entity, UserAmountResponse.class);
        ResponseEntity<UserAmountResponse> responseGet = restTemplate.exchange(String.format("http://localHost:%d/api?currencyInput=eur&currencyOutput=xxx&transactionType=sell&amount=100", port), HttpMethod.GET, entity, UserAmountResponse.class);
        //then
        assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
        assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
        UserAmountResponse body = responsePost.getBody();
        assertEquals(400, responsePost.getStatusCodeValue());
        assertEquals("Podano niewłąściwą daną: Waluta wyjściowa.", body.getMessage());
        assertNotNull(body.getAmount());
    }

    @Test
    void shouldReturnStatus400AndSpecificMessageForIncorrectTransaction(){
        //given
        CurrencyRateRequest currencyRateRequest = new CurrencyRateRequest("pln", "eur", "xxx", BigDecimal.valueOf(100));
        HttpEntity<CurrencyRateRequest> entity = new HttpEntity<>(currencyRateRequest);
        //when
        ResponseEntity<UserAmountResponse> responsePost = restTemplate.exchange(String.format("http://localHost:%d/api", port), HttpMethod.POST, entity, UserAmountResponse.class);
        ResponseEntity<UserAmountResponse> responseGet = restTemplate.exchange(String.format("http://localHost:%d/api?currencyInput=eur&currencyOutput=pln&transactionType=xxx&amount=100", port), HttpMethod.GET, entity, UserAmountResponse.class);
        //then
        assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
        assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
        UserAmountResponse body = responsePost.getBody();
        assertEquals(400, responsePost.getStatusCodeValue());
        assertEquals("Podano niewłąściwą daną: Niewłaściwa transakcja.", body.getMessage());
        assertNotNull(body.getAmount());
    }

    @Test
    void shouldReturnStatus400AndSpecificMessageForIncorrectAmount(){
        //given
        CurrencyRateRequest currencyRateRequest = new CurrencyRateRequest("pln", "eur", "sell", BigDecimal.valueOf(-1));
        HttpEntity<CurrencyRateRequest> entity = new HttpEntity<>(currencyRateRequest);
        //when
        ResponseEntity<UserAmountResponse> responsePost = restTemplate.exchange(String.format("http://localHost:%d/api", port), HttpMethod.POST, entity, UserAmountResponse.class);
        ResponseEntity<UserAmountResponse> responseGet = restTemplate.exchange(String.format("http://localHost:%d/api?currencyInput=eur&currencyOutput=pln&transactionType=sell&amount=-1", port), HttpMethod.GET, entity, UserAmountResponse.class);
        //then
        assertEquals(responsePost.getBody().getMessage(), responseGet.getBody().getMessage());
        assertEquals(responsePost.getBody().getAmount(), responseGet.getBody().getAmount());
        UserAmountResponse body = responsePost.getBody();
        assertEquals(400, responsePost.getStatusCodeValue());
        assertEquals("Podano niewłąściwą daną: Kwota.", body.getMessage());
        assertNotNull(body.getAmount());
    }

}