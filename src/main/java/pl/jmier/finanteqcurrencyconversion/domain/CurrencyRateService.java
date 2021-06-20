package pl.jmier.finanteqcurrencyconversion.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionRequest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionResponse;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyRateResponse;
import pl.jmier.finanteqcurrencyconversion.external.ExchangeRateClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class CurrencyRateService {

  @Autowired private ExchangeRateClient exchangeRateClient;
  private static final BigDecimal COMMISSION = BigDecimal.valueOf(0.98);
  private static final List<String> CURRENCIES_LIST = List.of("PLN", "EUR", "USD", "GBP");

  public String validateUserRequest(CurrencyConversionRequest request) {
    String currencyInput = request.getCurrencyInput();
    String currencyOutput = request.getCurrencyOutput();
    BigDecimal amount = request.getAmount();
    if (CURRENCIES_LIST.stream()
        .noneMatch(currency -> currency.equals(currencyInput.toUpperCase()))) {
      return "Waluta wejściowa.";
    }
    if (CURRENCIES_LIST.stream()
        .noneMatch(currency -> currency.equals(currencyOutput.toUpperCase()))) {
      return "Waluta wyjściowa.";
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      return "Kwota.";
    }

    return "Kwota do zwrotu";
  }
  // in table C in nbp api does not exists json for pln, pln is a
  // basic currency, so it is shown as 1(task requirements)
  public BigDecimal getRate(String currency, TransactionType transactionType)
      throws JsonProcessingException {
    if ("PLN".equals(currency.toUpperCase())) {
      return BigDecimal.ONE;
    }
    CurrencyRateResponse.Rate rate =
        exchangeRateClient.findCurrencyRate(currency, LocalDate.now()).getRates().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Currency " + currency + " not found."));
    if (TransactionType.INTO_PLN.equals(transactionType)) {
      return rate.getBid();
    }
    if (TransactionType.FROM_PLN.equals(transactionType)) {
      return rate.getAsk();
    }
    throw new RuntimeException("Transaction " + transactionType + " not found.");
  }

  public BigDecimal getAmountToReturn(
      BigDecimal amount, BigDecimal rateToBuy, BigDecimal rateToSell) {
    if (!rateToBuy.equals(BigDecimal.ONE) && !rateToSell.equals(BigDecimal.ONE)) {
      return amount
          .multiply(rateToBuy.divide(rateToSell, 4, RoundingMode.FLOOR))
          .multiply(COMMISSION)
          .setScale(2, RoundingMode.DOWN);
    }
    if (!rateToBuy.equals(BigDecimal.ONE)) {
      return amount.multiply(rateToBuy).multiply(COMMISSION).setScale(2, RoundingMode.FLOOR);
    }
    if (!rateToSell.equals(BigDecimal.ONE)) {
      return amount
          .divide(rateToSell, 4, RoundingMode.FLOOR)
          .multiply(COMMISSION)
          .setScale(2, RoundingMode.FLOOR);
    }
    return BigDecimal.ONE.multiply(COMMISSION);
  }

  public ResponseEntity<CurrencyConversionResponse> getResponse(CurrencyConversionRequest request)
      throws JsonProcessingException {
    String message = validateUserRequest(request);
    if (!message.equals("Kwota do zwrotu")) {
      return ResponseEntity.badRequest()
          .body(
              new CurrencyConversionResponse(
                  "Podano niewłąściwą daną: " + message, request.getAmount()));
    }
    if (request.getCurrencyInput().equals(request.getCurrencyOutput())) {
      return ResponseEntity.ok(new CurrencyConversionResponse(message, request.getAmount()));
    }
    return convertAmountAndGet200Response(request, message);
  }

  private ResponseEntity<CurrencyConversionResponse> convertAmountAndGet200Response(
      CurrencyConversionRequest request, String message) throws JsonProcessingException {
    BigDecimal rateToSell = BigDecimal.ONE;
    BigDecimal rateToBuy = BigDecimal.ONE;
    if (!"PLN".equals(request.getCurrencyInput().toUpperCase())) {
      rateToBuy = getRate(request.getCurrencyInput(), TransactionType.INTO_PLN);
    }
    if (!"PLN".equals(request.getCurrencyOutput().toUpperCase())) {
      rateToSell = getRate(request.getCurrencyOutput(), TransactionType.FROM_PLN);
    }
    return ResponseEntity.ok(
        new CurrencyConversionResponse(
            message, getAmountToReturn(request.getAmount(), rateToBuy, rateToSell)));
  }

  public enum TransactionType {
    INTO_PLN,
    FROM_PLN
  }
}
