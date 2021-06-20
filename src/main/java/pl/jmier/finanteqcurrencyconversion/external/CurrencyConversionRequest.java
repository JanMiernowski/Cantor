package pl.jmier.finanteqcurrencyconversion.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CurrencyConversionRequest {
  private String currencyInput;
  private String currencyOutput;
  private BigDecimal amount;
}
