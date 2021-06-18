package pl.jmier.finanteqcurrencyconversion.external;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Getter
@Setter
public class CurrencyRateRequest {

    private String currencyToSell;
    private String currencyToBuy;
    private String transactionType;
    private BigDecimal amount;
}
