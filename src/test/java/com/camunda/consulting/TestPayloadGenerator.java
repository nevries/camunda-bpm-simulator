package com.camunda.consulting;

import java.util.Date;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.joda.time.DateTime;

public class TestPayloadGenerator {
  public Date now() {
    return ClockUtil.getCurrentTime();
  }

  public Date inMinutes(int minutes) {
    return new DateTime(ClockUtil.getCurrentTime().getTime()).plusMinutes(minutes).toDate();
  }
}
