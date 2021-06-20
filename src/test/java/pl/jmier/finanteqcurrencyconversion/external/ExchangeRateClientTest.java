package pl.jmier.finanteqcurrencyconversion.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ExchangeRateClientTest {

  @Autowired private ExchangeRateClient exchangeRateClient;

  //
  @Test
  void shouldReturnValuesForFriday18thOfJuneWhenCurrencyIsCorrectAndDateRequestIs18th()
      throws JsonProcessingException {
    // given
    LocalDate localDate = LocalDate.of(2021, 6, 18);
    // when
    CurrencyRateResponse currencyRate = exchangeRateClient.findCurrencyRate("eur", localDate);
    // then
    assertEquals(1, currencyRate.getRates().size());
    assertEquals(LocalDate.of(2021, 6, 18), currencyRate.getRates().get(0).getEffectiveDate());
  }

  @Test
  void shouldReturnValuesForFriday18thOfJuneWhenCurrencyIsCorrectAndDateRequestIsSaturday19th()
      throws JsonProcessingException {
    // given
    LocalDate localDate = LocalDate.of(2021, 6, 19);
    // when
    CurrencyRateResponse currencyRate = exchangeRateClient.findCurrencyRate("eur", localDate);
    // then
    assertEquals(1, currencyRate.getRates().size());
    assertEquals(LocalDate.of(2021, 6, 18), currencyRate.getRates().get(0).getEffectiveDate());
  }

  @Test
  void shouldReturnValuesForFriday18thOfJuneWhenCurrencyIsCorrectAndDateRequestIsSunday20th()
      throws JsonProcessingException {
    // given
    LocalDate localDate = LocalDate.of(2021, 6, 20);
    // when
    CurrencyRateResponse currencyRate = exchangeRateClient.findCurrencyRate("eur", localDate);
    // then
    assertEquals(1, currencyRate.getRates().size());
    assertEquals(LocalDate.of(2021, 6, 18), currencyRate.getRates().get(0).getEffectiveDate());
  }

  @Test
  void shouldThrowExceptionWhenCurrencyIsIncorrect() throws JsonProcessingException {
    // given
    LocalDate localDate = LocalDate.of(2021, 6, 20);
    // when
    RuntimeException exception =
        assertThrows(
            RuntimeException.class, () -> exchangeRateClient.findCurrencyRate("xxx", localDate));
    // then
    assertEquals("Currency xxx not found.", exception.getMessage());
  }
}
