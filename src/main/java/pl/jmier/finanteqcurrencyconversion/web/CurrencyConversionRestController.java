package pl.jmier.finanteqcurrencyconversion.web;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jmier.finanteqcurrencyconversion.domain.CurrencyRateService;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionRequest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionResponse;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CurrencyConversionRestController {

  @Autowired private CurrencyRateService currencyRateService;

  @PostMapping
  public ResponseEntity<CurrencyConversionResponse> exchangeUserAmountPostMethod(
      @RequestBody CurrencyConversionRequest currencyConversionRequest) {
    String message = currencyRateService.validateRequestAndGetRates(currencyConversionRequest);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(
              new CurrencyConversionResponse(
                  "Podano niewłąściwą daną: " + message, currencyConversionRequest.getAmount()));
    }

    return ResponseEntity.ok(
        new CurrencyConversionResponse(
            message, currencyRateService.getAmountToReturn(currencyConversionRequest.getAmount())));
  }

  @GetMapping
  public ResponseEntity<CurrencyConversionResponse> exchangeUserAmountGetMethod(
      @RequestParam("currencyInput") String currencyInput,
      @RequestParam("currencyOutput") String currencyOutput,
      @RequestParam("transactionType") String transactionType,
      @RequestParam("amount") BigDecimal amount) {
    CurrencyConversionRequest request =
        new CurrencyConversionRequest(currencyInput, currencyOutput, transactionType, amount);
    String message = currencyRateService.validateRequestAndGetRates(request);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(new CurrencyConversionResponse("Podano niewłąściwą daną: " + message, amount));
    }

    return ResponseEntity.ok(
        new CurrencyConversionResponse(message, currencyRateService.getAmountToReturn(amount)));
  }
}
