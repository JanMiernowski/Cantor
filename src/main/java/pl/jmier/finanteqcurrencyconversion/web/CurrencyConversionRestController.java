package pl.jmier.finanteqcurrencyconversion.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jmier.finanteqcurrencyconversion.domain.CurrencyRateService;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionRequest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionResponse;
import pl.jmier.finanteqcurrencyconversion.external.ExchangeRateClient;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CurrencyConversionRestController {

  @Autowired private CurrencyRateService currencyRateService;

  @Autowired private ExchangeRateClient exchangeRateClient;

  @PostMapping
  public ResponseEntity<CurrencyConversionResponse> exchangeUserAmountPostMethod(
      @RequestBody CurrencyConversionRequest currencyConversionRequest)
      throws JsonProcessingException {
    return currencyRateService.getResponse(currencyConversionRequest);
  }

  @GetMapping
  public ResponseEntity<CurrencyConversionResponse> exchangeUserAmountGetMethod(
      @RequestParam("currencyInput") String currencyInput,
      @RequestParam("currencyOutput") String currencyOutput,
      @RequestParam("amount") BigDecimal amount)
      throws JsonProcessingException {
    CurrencyConversionRequest currencyConversionRequest =
        new CurrencyConversionRequest(currencyInput, currencyOutput, amount);
    return currencyRateService.getResponse(currencyConversionRequest);
  }
}
