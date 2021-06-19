package pl.jmier.finanteqcurrencyconversion.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyRateRequest;
import pl.jmier.finanteqcurrencyconversion.external.CurrencyRateResponse;
import pl.jmier.finanteqcurrencyconversion.external.ExchangeRateClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class CurrencyRateService {

    @Autowired
    private ExchangeRateClient exchangeRateClient;
    private static final BigDecimal commission = BigDecimal.valueOf(0.98);
    private static final List<String> currenciesList = List.of("PLN", "EUR", "USD", "GBD");
    public static BigDecimal rateToBuy;
    public static BigDecimal rateToSell;

    public String validateRequestAndGetRates(CurrencyRateRequest request) {
        String currencyInput = request.getCurrencyInput();
        String currencyOutput = request.getCurrencyOutput();
        BigDecimal amount = request.getAmount();
        String transactionType = request.getTransactionType();
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
            rateToBuy = getRate(currencyOutput, transactionType);
            rateToSell = getRate(currencyInput, transactionType);
        } catch (JsonProcessingException e) {
            return "Niespodziwany błąd. Skontaktuj się z administratorem sieci.";
        }
        return "Kwota do zwrotu";
    }

    //should be private
    public BigDecimal getRate(String currency, String transactionType)
            throws JsonProcessingException {
        if ("PLN".equals(currency.toUpperCase())) { // in table C in nbp api does not exists json for pln, pln is a basic currency so it is shown as 1(task requirements)
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
                .divide(rateToBuy.divide(rateToSell, 4, RoundingMode.CEILING), 4, RoundingMode.CEILING)
                .multiply(commission);
    }

}
