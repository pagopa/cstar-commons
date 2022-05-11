package it.gov.pagopa.cstar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.opencsv.bean.CsvToBeanBuilder;
import it.gov.pagopa.cstar.models.Transaction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * Unit test for simple App.
 */
class TransactionTest {

  private Validator validator;

  @BeforeEach
  public void setUp() {
    //Validator for checking transaction fields' correctness
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  /**
   * Parse valid transactions
   */
  @ParameterizedTest
  @CsvFileSource(resources = "/transactions/testTransactions.csv")
  void shouldParseTransactions(String line) {
    StringReader lineReader = new StringReader(line);
    ArrayList<Transaction> trxs = (ArrayList<Transaction>) new CsvToBeanBuilder<Transaction>(
        lineReader).withSeparator(';')
        .withThrowExceptions(true)
        .withType(Transaction.class)
        .build().parse();
    //If the line parsed results in a trx, the test is passed.
    assertEquals(1, trxs.size());

    Set<ConstraintViolation<Transaction>> violations = validator.validate(trxs.get(0));
    assertTrue(violations.isEmpty());
  }

  /**
   * Try to parse invalid transactions
   */
  @ParameterizedTest
  @CsvFileSource(resources = "/transactions/testMalformedTransactions.csv")
  void shouldFailParsingTransactions(String line) {

    StringReader lineReader = new StringReader(line);

    Transaction trx;

    try {
      // Read line by line the transactions.
      // If the parsing results in runtime CsvDataTypeMismatchException, the test is passed.
      trx = new CsvToBeanBuilder<Transaction>(lineReader).withSeparator(';')
          .withThrowExceptions(true)
          .withType(Transaction.class)
          .build().parse().get(0);
    } catch (RuntimeException e) {
      assertTrue(true);
      return;
    }

    Set<ConstraintViolation<Transaction>> violations = validator.validate(trx);

    // If there is at least a constraint violation, the test is passed.
    assertFalse(violations.isEmpty());
  }
}
