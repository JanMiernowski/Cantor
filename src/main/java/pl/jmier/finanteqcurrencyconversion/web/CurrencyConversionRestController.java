package pl.jmier.finanteqcurrencyconversion.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyRateRequest;
import pl.jmier.finanteqcurrencyconversion.external.ExchangeRateClient;
import pl.jmier.finanteqcurrencyconversion.external.UserAmountResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CurrencyConversionRestController {

  @Autowired private ExchangeRateClient exchangeRateClient;
  private static final BigDecimal commission = BigDecimal.valueOf(0.98);
  private static final List<String> currenciesList = List.of("PLN", "EUR", "USD", "GBD");
  private static BigDecimal rateToBuy;
  private static BigDecimal rateToSell;

  @PostMapping
  public ResponseEntity<UserAmountResponse> exchangeUserAmountPostMethod(
      @RequestBody CurrencyRateRequest currencyRateRequest) {
    String message =
        validateRequestAndGetRates(
            currencyRateRequest.getCurrencyInput(),
            currencyRateRequest.getCurrencyOutput(),
            currencyRateRequest.getTransactionType(),
            currencyRateRequest.getAmount());
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(
              new UserAmountResponse(
                  "Podano niewłąściwą daną: " + message, currencyRateRequest.getAmount()));
    }

    return ResponseEntity.ok(
        new UserAmountResponse(message, getAmountToReturn(currencyRateRequest.getAmount())));
  }

  @GetMapping
  public ResponseEntity<UserAmountResponse> exchangeUserAmountGetMethod(
      @RequestParam("currencyInput") String currencyInput,
      @RequestParam("currencyOutput") String currencyOutput,
      @RequestParam("transactionType") String transactionType,
      @RequestParam("amount") BigDecimal amount) {
    String message =
        validateRequestAndGetRates(currencyInput, currencyOutput, transactionType, amount);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(new UserAmountResponse("Podano niewłąściwą daną: " + message, amount));
    }

    return ResponseEntity.ok(new UserAmountResponse(message, getAmountToReturn(amount)));
  }

  protected BigDecimal getAmountToReturn(@RequestParam("amount") BigDecimal amount) {
    return amount
        .divide(rateToBuy.divide(rateToSell, 4, RoundingMode.CEILING), 4, RoundingMode.CEILING)
        .multiply(commission);
  }

  private String validateRequestAndGetRates(
      String currencyInput, String currencyOutput, String transactionType, BigDecimal amount) {
    if (currenciesList.stream()
        .noneMatch(currency -> currency.equals(currencyInput.toUpperCase()))) {
      return "Waluta wejściowa.";
    }
    if (currenciesList.stream()
        .noneMatch(currency -> currency.equals(currencyOutput.toUpperCase()))) {
      return "Waluta wyjściowa.";
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      return "Kwota.";
    }
    if (!"sell".equals(transactionType) && !"buy".equals(transactionType)) {
      return "Niewłaściwa transakcja.";
    }
    try {
      rateToBuy = exchangeRateClient.getRate(currencyOutput, transactionType);
      rateToSell = exchangeRateClient.getRate(currencyInput, transactionType);
    } catch (JsonProcessingException e) {
      return "Niespodziwany błąd. Skontaktuj się z administratorem sieci.";
    }
    return "Kwota do zwrotu";
  }
}
