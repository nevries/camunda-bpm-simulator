package com.camunda.consulting;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.joda.time.DateTime;

public class SimulationExecutor {

  // TODO make configurable
  public static final int METRIC_WRITE_INTERVAL_MINUTES = 15;

  public static void execute(Date start, Date end) {

    ProcessEngineConfigurationImpl processEngineConfigurationImpl = SimulatorPlugin.getProcessEngineConfiguration();
    ProcessEngine processEngine = SimulatorPlugin.getProcessEngine();
    CommandExecutor commandExecutor = processEngineConfigurationImpl.getCommandExecutorTxRequired();

    runWithPreparedEngineConfiguration(processEngineConfigurationImpl, processEngine, () -> {
      // TODO have somting to create initial jobs for start events
      ClockUtil.setCurrentTime(start);
      DateTime lastMetricUpdate = null;

      Optional<Job> job;
      do {
        // execute all jobs that are due before current time
        do {
          // work around engine "bug"
          makeTimeGoBy();

          // by setting the processEngineConfigurationImpl.setJobExecutor*
          // properties we can be sure to get the next job with minimum due date
          List<JobEntity> jobs = commandExecutor.execute(new Command<List<JobEntity>>() {
            @Override
            public List<JobEntity> execute(CommandContext commandContext) {
              return commandContext.getJobManager().findNextJobsToExecute(new Page(0, 1));
            }
          });
          job = jobs.stream().map(jobEntity -> (Job) jobEntity).findFirst();
          job.map(Job::getId).ifPresent(processEngine.getManagementService()::executeJob);

          job.map(Object::toString).ifPresent(System.out::println);

          // write metrics from time to time
          if (lastMetricUpdate == null || lastMetricUpdate.plusMinutes(METRIC_WRITE_INTERVAL_MINUTES).isBefore(ClockUtil.getCurrentTime().getTime())) {
            lastMetricUpdate = new DateTime(ClockUtil.getCurrentTime().getTime());
            processEngineConfigurationImpl.getDbMetricsReporter().reportNow();
          }
        } while (job.isPresent() && (job.get().getDuedate() == null || !job.get().getDuedate().after(end)));

        // get the next job that is due after current time and adjust clock to
        // its due date
        job = processEngine.getManagementService().createJobQuery().orderByJobDuedate().asc().listPage(0, 1).stream().findFirst();
        job.map(Job::getDuedate).ifPresent(ClockUtil::setCurrentTime);

        System.out.println("Set time to: " + ClockUtil.getCurrentTime());
      } while (job.isPresent() && (job.get().getDuedate() == null || !job.get().getDuedate().after(end)));
    });
  }

  private static void runWithPreparedEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfigurationImpl, ProcessEngine processEngine,
      Runnable runnable) {

    boolean metrics = processEngineConfigurationImpl.isMetricsEnabled() && processEngineConfigurationImpl.isDbMetricsReporterActivate();
    boolean jobExecutorEnabled = processEngineConfigurationImpl.getJobExecutor().isActive();

    boolean jobExecutorAcquireByPriority = processEngineConfigurationImpl.isJobExecutorAcquireByPriority();
    boolean jobExecutorPreferTimerJobs = processEngineConfigurationImpl.isJobExecutorPreferTimerJobs();
    boolean jobExecutorAcquireByDueDate = processEngineConfigurationImpl.isJobExecutorAcquireByDueDate();

    if (jobExecutorEnabled) {
      processEngineConfigurationImpl.getJobExecutor().shutdown();
    }

    if (metrics) {
      processEngineConfigurationImpl.getDbMetricsReporter().setReporterId("DEMO-DATA-GENERATOR");
    }

    processEngineConfigurationImpl.setJobExecutorAcquireByPriority(false);
    processEngineConfigurationImpl.setJobExecutorPreferTimerJobs(false);
    processEngineConfigurationImpl.setJobExecutorAcquireByDueDate(true);

    try {
      runnable.run();
    } finally {
      ClockUtil.reset();
      if (metrics) {
        processEngineConfigurationImpl.getDbMetricsReporter().reportNow();

        processEngineConfigurationImpl.getDbMetricsReporter()
            .setReporterId(processEngineConfigurationImpl.getMetricsReporterIdProvider().provideId(processEngine));
      }
      processEngineConfigurationImpl.setJobExecutorAcquireByPriority(jobExecutorAcquireByPriority);
      processEngineConfigurationImpl.setJobExecutorPreferTimerJobs(jobExecutorPreferTimerJobs);
      processEngineConfigurationImpl.setJobExecutorAcquireByDueDate(jobExecutorAcquireByDueDate);
      if (jobExecutorEnabled) {
        processEngineConfigurationImpl.getJobExecutor().start();
      }
    }
  }

  private static void makeTimeGoBy() {
    /*
     * Caused by DurationHelper.getDateAfterRepeat: return next.before(date) ?
     * null : next;
     * 
     * This leads to endless loop if we call a timer job at exactly the time it
     * will schedule next. Cannot be handled by engine, because there is no
     * "counter" in the database for executions - it has to trust the clock on
     * the wall.
     * 
     * Hence, we solve that by advancing the time as it would happen in real
     * live systems...
     */
    Calendar cal = Calendar.getInstance();
    cal.setTime(ClockUtil.getCurrentTime());
    cal.add(Calendar.MILLISECOND, 1);
    ClockUtil.setCurrentTime(cal.getTime());
  }
}
