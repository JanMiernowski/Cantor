package pl.jmier.finanteqcurrencyconversion.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.jmier.finanteqcurrencyconversion.domain.CurrencyRateService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CurrencyRateServiceTest {

  @Autowired private CurrencyRateService currencyRateService;

  @Test
  void shouldReturnOneForPlnCurrency() throws JsonProcessingException {
    // given
    // when
    BigDecimal rate = currencyRateService.getRate("pln", "sell");
    // then
    assertEquals(BigDecimal.ONE, rate);
  }

  @Test
  void shouldReturnOneForPlnCurrencyEvenTransactionIsNotValid() throws JsonProcessingException {
    // given
    // when
    BigDecimal rate = currencyRateService.getRate("pln", "xxx");
    // then
    assertEquals(BigDecimal.ONE, rate);
  }

  @Test
  void shouldThrowExceptionWhenApiDoesNotContainCurrency() throws JsonProcessingException {
    // given
    // when
    RuntimeException runtimeException =
        assertThrows(RuntimeException.class, () -> currencyRateService.getRate("XXX", "sell"));
    // then
    assertEquals("Currency XXX not found.", runtimeException.getMessage());
  }

  @Test
  void shouldReturnBidValueForTransactionTypeSell() throws JsonProcessingException {
    // given
    // when
    BigDecimal bid = currencyRateService.getRate("usd", "sell");
    // then
    assertNotNull(bid);
  }

  @Test
  void shouldReturnAskValueForTransactionTypeSell() throws JsonProcessingException {
    // given
    // when
    BigDecimal ask = currencyRateService.getRate("usd", "buy");
    // then
    assertNotNull(ask);
  }

  @Test
  void shouldReturnAskAndBidValuesAndAskValueIsBiggerThanBid() throws JsonProcessingException {
    // given
    // when
    BigDecimal ask = currencyRateService.getRate("usd", "buy");
    BigDecimal bid = currencyRateService.getRate("usd", "sell");
    // then
    assertEquals(1, ask.compareTo(bid));
  }
  @Test
  void shouldThrowExceptionForWrongTransaction() throws JsonProcessingException {
    // given
    // when
    RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> currencyRateService.getRate("usd", "XXX"));
    // then
    assertEquals("Transaction XXX not found.", runtimeException.getMessage());
  }
}
