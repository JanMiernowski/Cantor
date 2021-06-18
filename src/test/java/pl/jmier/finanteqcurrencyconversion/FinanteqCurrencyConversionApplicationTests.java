package pl.jmier.finanteqcurrencyconversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyRateResponse;
import pl.jmier.finanteqcurrencyconversion.external.ExchangeRateClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class FinanteqCurrencyConversionApplicationTests {

	@Autowired
	private ExchangeRateClient exchangeRateClient;

	@Test
	void shouldFetchExchangeRate() throws JsonProcessingException {
		//given

		//when
		BigDecimal currencyRate = exchangeRateClient.getRate("EUR", "sell");
		//then
		Assertions.assertNotNull(currencyRate);
	}

	@Test
	void contextLoads() {
	}

}
