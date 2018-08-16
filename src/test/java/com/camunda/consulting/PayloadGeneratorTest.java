package com.camunda.consulting;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.junit.Test;

public class PayloadGeneratorTest {

  PayloadGenerator g = new PayloadGenerator();

  @Test
  public void generateNormalNumbers() {
    assertThat(g.normal("a", 15, 3)).isBetween(0.0, 30.0);
    assertThat(g.nowPlusHours(2)).isBetween(DateTime.now().plusHours(1).toDate(), DateTime.now().plusHours(3).toDate());
  }
}
