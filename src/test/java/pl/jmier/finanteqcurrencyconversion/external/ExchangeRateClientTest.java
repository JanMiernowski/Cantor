package pl.jmier.finanteqcurrencyconversion.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExchangeRateClientTest {

    @Autowired
    private ExchangeRateClient exchangeRateClient;

    @Test
    void shouldReturnValuesForFriday18thOfJuneWhenCurrencyIsCorrectAndDateRequestIsSaturday19th() throws JsonProcessingException {
        //given
        LocalDate localDate = LocalDate.of(2021, 6,19);
        //when
        CurrencyRateResponse currencyRate = exchangeRateClient.findCurrencyRate("eur", localDate);
        //then
        assertEquals(1, currencyRate.getRates().size());
        assertEquals(LocalDate.of(2021, 6, 18),currencyRate.getRates().get(0).getEffectiveDate());
    }
}