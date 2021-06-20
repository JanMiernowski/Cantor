package pl.jmier.finanteqcurrencyconversion.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyConversionRequest;
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
  private static final List<String> CURRENCIES_LIST = List.of("PLN", "EUR", "USD", "GBD");
  public static BigDecimal rateToBuy;
  public static BigDecimal rateToSell;

  public String validateRequestAndGetRates(CurrencyConversionRequest request) {
    String currencyInput = request.getCurrencyInput();
    String currencyOutput = request.getCurrencyOutput();
    BigDecimal amount = request.getAmount();
    String transactionType = request.getTransactionType();
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
    if (!"sell".equals(transactionType) && !"buy".equals(transactionType)) {
      return "Niewłaściwa transakcja.";
    }
    try {
      rateToBuy = getRate(currencyOutput, transactionType);
      rateToSell = getRate(currencyInput, transactionType);
    } catch (JsonProcessingException e) {
      return "Niespodziwany błąd. Skontaktuj się z administratorem sieci.";
    }
    return "Kwota do zwrotu";
  }
  // in table C in nbp api does not exists json for pln, pln is a
  // basic currency, so it is shown as 1(task requirements)
  private BigDecimal getRate(String currency, String transactionType)
      throws JsonProcessingException {
    if ("PLN".equals(currency.toUpperCase())) {

      return BigDecimal.ONE;
    }
    CurrencyRateResponse.Rate rate =
        exchangeRateClient.findCurrencyRate(currency, LocalDate.now()).getRates().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Currency " + currency + " not found."));
    if ("sell".equals(transactionType)) {
      return rate.getBid();
    }
    if ("buy".equals(transactionType)) {
      return rate.getAsk();
    }
    throw new RuntimeException("Transaction " + transactionType + " not found.");
  }

  public BigDecimal getAmountToReturn(BigDecimal amount) {
    return amount
        .divide(rateToBuy.divide(rateToSell, 4, RoundingMode.FLOOR), 4, RoundingMode.FLOOR)
        .multiply(COMMISSION)
        .setScale(2, RoundingMode.FLOOR);
  }
}
