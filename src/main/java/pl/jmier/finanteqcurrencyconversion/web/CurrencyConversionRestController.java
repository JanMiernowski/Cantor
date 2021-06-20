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
    String message = currencyRateService.validateRequestAndGetRates(currencyConversionRequest);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(
              new CurrencyConversionResponse(
                  "Podano niewłąściwą daną: " + message, currencyConversionRequest.getAmount()));
    }
    if (currencyConversionRequest
        .getCurrencyInput()
        .equals(currencyConversionRequest.getCurrencyOutput())) {
      return ResponseEntity.ok(
          new CurrencyConversionResponse(message, currencyConversionRequest.getAmount()));
    }
    BigDecimal rateToSell = BigDecimal.ONE;
    BigDecimal rateToBuy = BigDecimal.ONE;
    if (!"PLN".equals(currencyConversionRequest.getCurrencyInput().toUpperCase())) {
      rateToBuy =
          currencyRateService.getRate(
              currencyConversionRequest.getCurrencyInput(),
              CurrencyRateService.TransactionType.INTO_PLN);
    }
    if (!"PLN".equals(currencyConversionRequest.getCurrencyOutput().toUpperCase())) {
      rateToSell =
          currencyRateService.getRate(
              currencyConversionRequest.getCurrencyOutput(),
              CurrencyRateService.TransactionType.FROM_PLN);
    }

    return ResponseEntity.ok(
        new CurrencyConversionResponse(
            message,
            currencyRateService.getAmountToReturn(
                currencyConversionRequest.getAmount(), rateToBuy, rateToSell)));
  }

  @GetMapping
  public ResponseEntity<CurrencyConversionResponse> exchangeUserAmountGetMethod(
      @RequestParam("currencyInput") String currencyInput,
      @RequestParam("currencyOutput") String currencyOutput,
      @RequestParam("amount") BigDecimal amount)
      throws JsonProcessingException {
    CurrencyConversionRequest request =
        new CurrencyConversionRequest(currencyInput, currencyOutput, amount);
    String message = currencyRateService.validateRequestAndGetRates(request);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(new CurrencyConversionResponse("Podano niewłąściwą daną: " + message, amount));
    }
    if (currencyInput.equals(currencyOutput)) {
      return ResponseEntity.ok(new CurrencyConversionResponse(message, amount));
    }
    BigDecimal rateToSell = BigDecimal.ONE;
    BigDecimal rateToBuy = BigDecimal.ONE;
    if (!"PLN".equals(currencyInput.toUpperCase())) {
      rateToBuy =
          currencyRateService.getRate(currencyInput, CurrencyRateService.TransactionType.INTO_PLN);
    }
    if (!"PLN".equals(currencyOutput.toUpperCase())) {
      rateToSell =
          currencyRateService.getRate(currencyOutput, CurrencyRateService.TransactionType.FROM_PLN);
    }
    return ResponseEntity.ok(
        new CurrencyConversionResponse(
            message, currencyRateService.getAmountToReturn(amount, rateToBuy, rateToSell)));
  }
}
