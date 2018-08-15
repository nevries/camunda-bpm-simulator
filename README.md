# camunda-bpm-simulator

Camunda process engine plugin to simulate process execution.

## Purpose

When execution process definitions, the process engine waits from time to time for external events, for example user actions, conditions, timeouts etc.
This process engine plugin will generate these events to run processes without external interaction, hence, to simulate real world scenarios.

They way if and when these events are triggered and if and what payload data is to be generated is configured by [Camunda Properties](https://docs.camunda.org/manual/7.9/reference/bpmn20/custom-extensions/extension-elements/#properties) in the bpmn-files.
Optionally, all these properties can be set externally, for example in property files.

The plugin is able to simulate the past...

## Properties for controlling

### None start event

Like timer start event, defined by expression.

### Message/Signal start event

Correlates message/trigger sigal like timer start event, defined by expression.

### Conditional start event

Timer expression and payload expression needed.
The plugin generates the payload when the timer is due and triggers the condition.

### Message Receive events, Receive Task

Correlate message after time, defined by expression.

### Signal receive events

Trigger signal after time, defined by expression.
If no expression is given, we assume that the signal is thrown within the models.

### User Task

Complete after time, defined by expression.
Ideas: Also simulate assignee, candidates, claiming...

### Service Task (Send Task, Business Rule Task, Script Task)

Replace behaviour by no-op.

### External Service Task

Complete after time, defined by expression.


### all

#### Execution (Task) Listeners

Replace by no-op.

## Properties for payload generation

Every flow node can have multiple properties named `simulatePayload` with value of the form `varname=juelExpression`.
For expressions, the plugin provides 'stuff' with common data generation function.

## TODO

* think about throwing BPMN errors
* think about keeping execution/task listeners