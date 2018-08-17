package com.camunda.consulting.jobhandler;

import com.camunda.consulting.PayloadGenerator;
import com.camunda.consulting.SimulationExecutor;
import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

@Deployment(resources = "externalTaskCompleteModel.bpmn")
public class CompleteExternalTaskJobHandlerTest {


  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
    Mocks.register("generator", new PayloadGenerator());
  }

  @Test
  public void shouldExecuteCompleteExternalTaskJob() {

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("externalTaskComplete");

    assertThat(processInstance).isStarted().isWaitingAt("externalTask");
    assertThat(jobQuery().count()).isEqualTo(1); // simulation job was created

    SimulationExecutor.execute(DateTime.now().minusMinutes(5).toDate(), DateTime.now().plusMinutes(5).toDate());

    assertThat(processInstance).isEnded();

  }


}