package com.camunda.consulting;

import java.util.ArrayList;
import java.util.List;

import com.camunda.consulting.jobhandler.CompleteUserTaskJobHandler;
import com.camunda.consulting.jobhandler.FireEventJobHandler;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;

public class SimulatorPlugin implements ProcessEnginePlugin {

  private static ProcessEngineConfigurationImpl processEngineConfiguration;
  private static ProcessEngine processEngine;

  protected static ProcessEngine getProcessEngine() {
    return processEngine;
  }

  protected static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    SimulatorPlugin.processEngineConfiguration = processEngineConfiguration;
    
    List<BpmnParseListener> parseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
    if (parseListeners == null) {
      parseListeners = new ArrayList<>();
      processEngineConfiguration.setCustomPreBPMNParseListeners(parseListeners);
    }
    parseListeners.add(new SimulationParseListener());

    List<JobHandler> customJobHandlers = processEngineConfiguration.getCustomJobHandlers();
    if(customJobHandlers == null){
      customJobHandlers = new ArrayList<>();
      processEngineConfiguration.setCustomJobHandlers(customJobHandlers);
    }
    customJobHandlers.add(new CompleteUserTaskJobHandler());
    customJobHandlers.add(new FireEventJobHandler());

  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    SimulatorPlugin.processEngine = processEngine;
  }

}
