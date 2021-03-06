package pl.jmier.finanteqcurrencyconversion.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionRequest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CurrencyRateServiceTest {

  @Autowired private CurrencyRateService currencyRateService;

  @Test
  void shouldReturnOneForPlnCurrency() throws JsonProcessingException {
    // given
    // when
    BigDecimal rate =
        currencyRateService.getRate("pln", CurrencyRateService.TransactionType.FROM_PLN);
    // then
    assertEquals(BigDecimal.ONE, rate);
  }

  @Test
  void shouldReturnOneForPlnCurrencyEvenTransactionIsNotValid() throws JsonProcessingException {
    // given
    // when
    BigDecimal rate = currencyRateService.getRate("pln", null);
    // then
    assertEquals(BigDecimal.ONE, rate);
  }

  @Test
  void shouldThrowExceptionWhenApiDoesNotContainCurrency() {
    // given
    // when
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> currencyRateService.getRate("XXX", CurrencyRateService.TransactionType.FROM_PLN));
    // then
    assertEquals("Currency XXX not found.", exception.getMessage());
  }

  @Test
  void shouldReturnBidValueForTransactionTypeOnPln() throws JsonProcessingException {
    // given
    // when
    BigDecimal bid =
        currencyRateService.getRate("usd", CurrencyRateService.TransactionType.INTO_PLN);
    // then
    assertNotNull(bid);
  }

  @Test
  void shouldReturnAskValueForTransactionTypeSell() throws JsonProcessingException {
    // given
    // when
    BigDecimal ask =
        currencyRateService.getRate("usd", CurrencyRateService.TransactionType.FROM_PLN);
    // then
    assertNotNull(ask);
  }

  @Test
  void shouldReturnAskAndBidValuesAndAskValueIsBiggerThanBid() throws JsonProcessingException {
    // given
    // when
    BigDecimal ask =
        currencyRateService.getRate("usd", CurrencyRateService.TransactionType.FROM_PLN);
    BigDecimal bid =
        currencyRateService.getRate("usd", CurrencyRateService.TransactionType.INTO_PLN);
    // then
    assertEquals(1, ask.compareTo(bid));
  }

  @Test
  void shouldThrowExceptionForWrongTransaction() {
    // given
    // when
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> currencyRateService.getRate("usd", null));
    // then
    assertEquals("Transaction " + null + " not found.", exception.getMessage());
    // then
  }

  @Test
  void shouldReturnSpecificMessageForIncorrectInputValue() {
    // given
    CurrencyConversionRequest request = new CurrencyConversionRequest("xxx", "eur", BigDecimal.TEN);
    // when
    String message = currencyRateService.validateUserRequest(request);
    // then
    assertEquals("Waluta wej??ciowa.", message);
  }

  @Test
  void shouldReturnSpecificMessageForIncorrectOutputValue() {
    // given
    CurrencyConversionRequest request = new CurrencyConversionRequest("pln", "xyz", BigDecimal.TEN);
    // when
    String message = currencyRateService.validateUserRequest(request);
    // then
    assertEquals("Waluta wyj??ciowa.", message);
  }

  @Test
  void shouldReturnSpecificMessageForIncorrectAmount() {
    // given
    CurrencyConversionRequest request =
        new CurrencyConversionRequest("pln", "eur", BigDecimal.valueOf(-1));
    // when
    String message = currencyRateService.validateUserRequest(request);
    // then
    assertEquals("Kwota.", message);
  }

  @Test
  void shouldReturnSpecificMessageWhenEveryValueIsCorrect() {
    // given
    CurrencyConversionRequest request = new CurrencyConversionRequest("pln", "eur", BigDecimal.TEN);
    // when
    String message = currencyRateService.validateUserRequest(request);
    // then
    assertEquals("Kwota do zwrotu", message);
  }

  // converted currency has been checked on the website
  // https://www.podatki.egospodarka.pl/przelicznik-walut/?kwota=1&cf=USD&ct=EUR&date=2021-06-18&oblicz=Oblicz
  @Test
  void shouldReturnSpecificAmountForDifferentCurrencies() {
    // given
    BigDecimal rateToSell = BigDecimal.valueOf(4.5503); // ask value in euro currency
    BigDecimal rateToBuy = BigDecimal.valueOf(3.8198); // bid value in usd currency
    // when
    BigDecimal amountToReturn =
        currencyRateService.getAmountToReturn(BigDecimal.ONE, rateToBuy, rateToSell);
    // then
    assertEquals(BigDecimal.valueOf(0.82), amountToReturn);
  }

  @Test
  void shouldReturnSpecificAmountWhenUserWantToConvertIntoPln() {
    // given
    BigDecimal rateToSell = BigDecimal.ONE; // ask value in pln currency
    BigDecimal rateToBuy = BigDecimal.valueOf(4.5503); // bid value in euro currency
    // when
    BigDecimal amountToReturn =
        currencyRateService.getAmountToReturn(BigDecimal.ONE, rateToBuy, rateToSell);
    // then
    assertEquals(BigDecimal.valueOf(4.45), amountToReturn);
  }

  @Test
  void shouldReturnSpecificAmountWhenUserWantToConvertFromPln() {
    // given
    BigDecimal rateToSell = BigDecimal.valueOf(4.5503); // ask value in euro currency
    BigDecimal rateToBuy = BigDecimal.ONE; // bid value in pln currency
    // when
    BigDecimal amountToReturn =
        currencyRateService.getAmountToReturn(BigDecimal.ONE, rateToBuy, rateToSell);
    // then
    assertEquals(BigDecimal.valueOf(0.21), amountToReturn);
  }

  @Test
  void shouldReturnOneSpecificValueWhenUserWantToConvertFromTheSameValueUserWantsToConvertTo() {
    // given
    BigDecimal rateToSell = BigDecimal.valueOf(4.5503); // ask value in eur currency
    BigDecimal rateToBuy = BigDecimal.valueOf(4.5503); // bid value in eur currency
    // when
    BigDecimal amountToReturn =
        currencyRateService.getAmountToReturn(BigDecimal.ONE, rateToBuy, rateToSell);
    // then
    assertEquals(BigDecimal.valueOf(0.98), amountToReturn);
  }

  @Test
  void shouldReturnOneSpecificValueWhenUserWantToConvertFromPlnToPln() {
    // given
    BigDecimal rateToSell = BigDecimal.valueOf(1); // ask value in pln currency
    BigDecimal rateToBuy = BigDecimal.valueOf(1); // bid value in pln currency
    // when
    BigDecimal amountToReturn =
        currencyRateService.getAmountToReturn(BigDecimal.ONE, rateToBuy, rateToSell);
    // then
    assertEquals(BigDecimal.valueOf(0.98), amountToReturn);
  }

  @Test
  void shouldReturnStatus200ForCorrectValues() throws JsonProcessingException {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("eur", "usd", BigDecimal.valueOf(100));
    // when
    ResponseEntity<CurrencyConversionResponse> response =
        currencyRateService.getResponse(currencyConversionRequest);
    // then
    assertEquals(response.getBody().getMessage(), response.getBody().getMessage());
    assertEquals(response.getBody().getAmount(), response.getBody().getAmount());
    CurrencyConversionResponse body = response.getBody();
    assertEquals(200, response.getStatusCodeValue());
    assertNotEquals(body.getAmount(), BigDecimal.valueOf(100));
  }

  @Test
  void shouldReturnStatus200AndUnchangedAmountForThisSameCurrencyInputAndOutput()
      throws JsonProcessingException {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("eur", "eur", BigDecimal.valueOf(100));
    // when
    ResponseEntity<CurrencyConversionResponse> response =
        currencyRateService.getResponse(currencyConversionRequest);
    // then
    assertEquals(response.getBody().getMessage(), response.getBody().getMessage());
    assertEquals(response.getBody().getAmount(), response.getBody().getAmount());
    CurrencyConversionResponse body = response.getBody();
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(body.getAmount(), BigDecimal.valueOf(100));
  }

  @Test
  void shouldReturnStatus400ForIncorrectCurrencyInput() throws JsonProcessingException {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("xxx", "pln", BigDecimal.valueOf(100));
    // when
    ResponseEntity<CurrencyConversionResponse> response =
        currencyRateService.getResponse(currencyConversionRequest);
    // then
    assertEquals(response.getBody().getMessage(), response.getBody().getMessage());
    assertEquals(response.getBody().getAmount(), response.getBody().getAmount());
    CurrencyConversionResponse body = response.getBody();
    assertEquals(400, response.getStatusCodeValue());
    assertEquals(body.getAmount(), BigDecimal.valueOf(100));
  }

  @Test
  void shouldReturnStatus400ForIncorrectCurrencyOutput() throws JsonProcessingException {
    // given
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest("pln", "xxx", BigDecimal.valueOf(100));
    // when
    ResponseEntity<CurrencyConversionResponse> response =
        currencyRateService.getResponse(currencyConversionRequest);
    // then
    assertEquals(response.getBody().getMessage(), response.getBody().getMessage());
    assertEquals(response.getBody().getAmount(), response.getBody().getAmount());
    CurrencyConversionResponse body = response.getBody();
    assertEquals(400, response.getStatusCodeValue());
    assertEquals(body.getAmount(), BigDecimal.valueOf(100));
  }
}
