package pl.jmier.finanteqcurrencyconversion.external;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserAmountResponse {

    private String message;
    private BigDecimal amount;

}
