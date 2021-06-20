package pl.jmier.finanteqcurrencyconversion.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CurrencyRateServiceTest {

  @Autowired private CurrencyRateService currencyRateService;

  @Test
  void shouldReturnOneForPlnCurrency()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    // given
    Method method =
        CurrencyRateService.class.getDeclaredMethod("getRate", String.class, String.class);
    method.setAccessible(true);
    // when
    BigDecimal rate = (BigDecimal) method.invoke(currencyRateService, "pln", "sell");
    // then
    assertEquals(BigDecimal.ONE, rate);
  }

  @Test
  void shouldReturnOneForPlnCurrencyEvenTransactionIsNotValid()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // given
    Method method =
        CurrencyRateService.class.getDeclaredMethod("getRate", String.class, String.class);
    method.setAccessible(true);
    // when
    BigDecimal rate = (BigDecimal) method.invoke(currencyRateService, "pln", "xxx");
    // then
    assertEquals(BigDecimal.ONE, rate);
  }

  @Test
  void shouldThrowExceptionWhenApiDoesNotContainCurrency() throws NoSuchMethodException {
    // given
    Method method =
        CurrencyRateService.class.getDeclaredMethod("getRate", String.class, String.class);
    method.setAccessible(true);
    // when
    assertThrows(
        InvocationTargetException.class, () -> method.invoke(currencyRateService, "XXX", "sell"));
    // then
  }

  @Test
  void shouldReturnBidValueForTransactionTypeSell()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    // given
    Method method =
        CurrencyRateService.class.getDeclaredMethod("getRate", String.class, String.class);
    method.setAccessible(true);
    // when
    BigDecimal bid = (BigDecimal) method.invoke(currencyRateService, "usd", "sell");
    // then
    assertNotNull(bid);
  }

  @Test
  void shouldReturnAskValueForTransactionTypeSell()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // given
    Method method =
        CurrencyRateService.class.getDeclaredMethod("getRate", String.class, String.class);
    method.setAccessible(true);
    // when
    BigDecimal ask = (BigDecimal) method.invoke(currencyRateService, "usd", "buy");
    // then
    assertNotNull(ask);
  }

  @Test
  void shouldReturnAskAndBidValuesAndAskValueIsBiggerThanBid()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // given
    Method method =
        CurrencyRateService.class.getDeclaredMethod("getRate", String.class, String.class);
    method.setAccessible(true);
    // when
    BigDecimal ask = (BigDecimal) method.invoke(currencyRateService, "usd", "buy");
    BigDecimal bid = (BigDecimal) method.invoke(currencyRateService, "usd", "sell");
    // then
    assertEquals(1, ask.compareTo(bid));
  }

  @Test
  void shouldThrowExceptionForWrongTransaction() throws NoSuchMethodException {
    // given
    Method method =
        CurrencyRateService.class.getDeclaredMethod("getRate", String.class, String.class);
    method.setAccessible(true);
    // when
    assertThrows(
        InvocationTargetException.class, () -> method.invoke(currencyRateService, "usd", "XXX"));
    // then
  }

  @Test
  void shouldReturnSpecificMessageForIncorrectInputValue() {
    // given
    CurrencyConversionRequest request = new CurrencyConversionRequest("xxx", "eur", "sell", BigDecimal.TEN);
    // when
    String message = currencyRateService.validateRequestAndGetRates(request);
    // then
    assertEquals("Waluta wejściowa.", message);
  }

  @Test
  void shouldReturnSpecificMessageForIncorrectOutputValue() {
    // given
    CurrencyConversionRequest request = new CurrencyConversionRequest("pln", "xyz", "buy", BigDecimal.TEN);
    // when
    String message = currencyRateService.validateRequestAndGetRates(request);
    // then
    assertEquals("Waluta wyjściowa.", message);
  }

  @Test
  void shouldReturnSpecificMessageForIncorrectTransactionType() {
    // given
    CurrencyConversionRequest request = new CurrencyConversionRequest("pln", "eur", "xxx", BigDecimal.TEN);
    // when
    String message = currencyRateService.validateRequestAndGetRates(request);
    // then
    assertEquals("Niewłaściwa transakcja.", message);
  }

  @Test
  void shouldReturnSpecificMessageForIncorrectAmount() {
    // given
    CurrencyConversionRequest request =
        new CurrencyConversionRequest("pln", "eur", "buy", BigDecimal.valueOf(-1));
    // when
    String message = currencyRateService.validateRequestAndGetRates(request);
    // then
    assertEquals("Kwota.", message);
  }

  @Test
  void shouldReturnSpecificMessageWhenEveryValueIsCorrect() {
    // given
    CurrencyConversionRequest request = new CurrencyConversionRequest("pln", "eur", "buy", BigDecimal.TEN);
    // when
    String message = currencyRateService.validateRequestAndGetRates(request);
    // then
    assertEquals("Kwota do zwrotu", message);
  }

  // converted currency has been checked on the website
  // https://www.podatki.egospodarka.pl/przelicznik-walut/?kwota=1&cf=USD&ct=EUR&date=2021-06-18&oblicz=Oblicz
  @Test
  void shouldReturnSpecificAmountForDifferentCurrencies() {
    // given
    CurrencyRateService.rateToBuy = BigDecimal.valueOf(4.5503); // ask value in euro currency
    CurrencyRateService.rateToSell = BigDecimal.valueOf(3.8198); // bid value in usd currency
    // when
    BigDecimal amountToReturn = currencyRateService.getAmountToReturn(BigDecimal.ONE);
    // then
    assertEquals(BigDecimal.valueOf(0.8226), amountToReturn);
  }
}
