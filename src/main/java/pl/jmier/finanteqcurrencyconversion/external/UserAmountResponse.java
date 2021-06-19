package pl.jmier.finanteqcurrencyconversion.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAmountResponse {
  private String message;
  private BigDecimal amount;
}
