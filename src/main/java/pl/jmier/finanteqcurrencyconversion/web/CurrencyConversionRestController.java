package pl.jmier.finanteqcurrencyconversion.web;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jmier.finanteqcurrencyconversion.domain.CurrencyRateService;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyRateRequest;
import pl.jmier.finanteqcurrencyconversion.external.UserAmountResponse;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CurrencyConversionRestController {

  @Autowired private CurrencyRateService currencyRateService;

  @PostMapping
  public ResponseEntity<UserAmountResponse> exchangeUserAmountPostMethod(
      @RequestBody CurrencyRateRequest currencyRateRequest) {
    String message = currencyRateService.validateRequestAndGetRates(currencyRateRequest);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(
              new UserAmountResponse(
                  "Podano niewłąściwą daną: " + message, currencyRateRequest.getAmount()));
    }

    return ResponseEntity.ok(
        new UserAmountResponse(message, currencyRateService.getAmountToReturn(currencyRateRequest.getAmount())));
  }

  @GetMapping
  public ResponseEntity<UserAmountResponse> exchangeUserAmountGetMethod(
      @RequestParam("currencyInput") String currencyInput,
      @RequestParam("currencyOutput") String currencyOutput,
      @RequestParam("transactionType") String transactionType,
      @RequestParam("amount") BigDecimal amount) {
    CurrencyRateRequest request = new CurrencyRateRequest(currencyInput, currencyOutput, transactionType, amount);
    String message =
            currencyRateService.validateRequestAndGetRates(request);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(new UserAmountResponse("Podano niewłąściwą daną: " + message, amount));
    }

    return ResponseEntity.ok(new UserAmountResponse(message, currencyRateService.getAmountToReturn(amount)));
  }
}
