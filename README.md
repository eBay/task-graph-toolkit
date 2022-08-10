# Workflow 

This library is an extension of basic Java concurrency patterns along with some other Plain Old Java interfaces that helps organize business logic implementations into a set of interdependent tasks.

Reasons you would want to consider using this library versus other options:

 * Low learning curve as it uses standard Java concurrency patterns and other Plain Old Java interfaces.  Other frameworks tend to use custom annotations that can be difficult figure out their behaviour, especially for less senior developers.
 * Compile time checking of dependency types and task orchestration wiring.
 * Task scheduling is handled by the JVM, which offers reliability, performance and debuggability.
 * Clear error handling semantics provided by task decorator applied to standard Java Future interface.
 * Support for task timeouts separate from time blocking for dependent task results.  Some frameworks only support a timeout for the execution of all tasks in the workflow.
 * Easy to implement your own concurrency patterns within the toolkit, rather than fighting frameworks that control task orchestration.
 * Task decorator profiles time taken by task processing along with time spent blocking for its dependencies.
 * Reliable tracking of inter task dependencies.
 * Profiler data model that allows task processing times and dependencies to be visualized in the profilerview node module.  Task graphs and profiling images in the documentation are screenshots from the tool that displays various views of the profiling data.

## Usage
To build the project:
```shell
mvn clean install
```

# Documentation
The Workflow lib is a collection of interfaces and classes that extend the basic Java concurrency interfaces.  While following basic Java patterns it also introduces some additional patterns to help manage complex graphs of interdependent tasks.

The patterns can be grouped in to two areas:

1. Callable Executor.  This is a set of interfaces that extend the basic Callable and Future Java interfaces.  The purpose of these extensions is to allow for a common and reliable method to track dependencies between tasks and also to allow for a common way to manage task diagnostics, profiling and tracking data.

2. Workflow.  This is a set of interfaces and classes that help organize tasks into a manageable directed acyclic graph (DAG).  It defines patterns for decoupling task business logic from the orchestration and for grouping sets of tasks into coherent, encapsulated and composable units of aggregated business logic.

These patterns can be used to effectively organize and execute very complex orchestrations such as search experience service:

![search_xs](/docs/search_xs.png)

# Callable Executor

Here are the core components:

![callable](/docs/callable.jpg)

## Task

A task is the basic unit of work in the system.  The abstract base class Task contains the components common to all tasks and consists of the task's ResponseContext and the list of other tasks it depends on.  The list of upstream tasks enables the profiler to track how long the task took waiting for its dependencies to be available before starting its own execution.  This allows the time spent executing the task's own business logic to be recorded accurately.

Application task classes implement the ICallableTask interface and are only responsible for implementing the standard Java Callable interface method.

Here are some useful constraints that should be applied to task implementations:

* All task dependencies should be explicitly declared in the task's constructor.  Constructor injection is the preferred form of dependency injection because it makes it very clear what the dependencies of the task are.

* Tasks should not read from or write to any global shared contexts.  This includes writing tracking data to shared tracking objects.  Task tracking data should be logged in the ResponseContext to decouple what is being tracked from the system that records it.  It also simplifies unit testing tracking required by the task.

* Tasks should not write to any of its dependencies data models.  Ideally this should be enforced by all contexts and task result objects being immutable.

* Task dependencies should be limited to the scope of the task with no unnecessary data passed in.  Avoid passing contexts that contain data outside the scope of the task,  especially large application scope contexts.

* Tasks should not handle unrecoverable exceptions that result in a null response from the task.  The decorator added by the executor will ensure exceptions are logged appropriately with the corresponding task name.

Adhering to the above constraints will reduce the vast majority of concurrency and orchestration problems associated with task business logic implementations.

## ResponseContext

The response context is a class owned by each task that records data associated with the task's processing that is not part of the data model returned by the task's Callable method.

Data managed by this class:

* Profiler data - The profiler decorator records how long the task takes to complete along with the time spent waiting for its dependencies.  It also contains the tasks that consume the output of the task's Callable method.  This allows the task dependency graph to be visualized in the profilerview tool.

* Diagnostic data - Task specific diagnostic data.

* Tracking data - tracking tags that need to be recorded based on the task business logic execution.

* Errors recorded during task processing.

## ICallableTask

Defines the interface of a task.  Extends Java Callable interface with these methods:

* getContext - accessor to the task response context.  Used by the executor to aggregate the response contexts of the tasks it manages.

* getTaskConfig - accessor to the task configuration.

* waitForDependencies - used by the profile decorator to time how long the task blocks for its dependencies to complete before executing.  The default implementation of this method is to block for all the task's dependencies to complete but it does allow for custom implementations.  E.g. if a task has a critical dependency but has other dependencies it doesn't have to wait for.

## ProfileDecorator

Decorator class for ICallableTask added by the executor class for each task.  Handles the following:

* Times how long the task waits for its dependencies to complete and how long the task's Callable method takes to execute.  Logs this data in the profiler object in the task's ResponseContext.

* Handles exceptions thrown by task's callable method and logs them with the task name.

## SynchronousFuture

Implementation of the Java Future interface that calls the corresponding ICallableTask.call() method synchronously on the thread of a dependent task.  

## ICallableTaskFuture

This extension of the Java Future interface which adds an additional get(ICallableTask<?> caller) method that allows the underlying task to keep track of consumers of its result in the ResponseContext.  This enables the profilerview tool to visually represent the DAG of tasks.  There is also an accessor method for the underlying ICallableTask.  

## CallableTaskFuture

A decorator implementation of the ICallableTaskFuture interface.  Executor implementations add this decorator to the Futures of all tasks that are submitted.

Given that the tracking of the graph of dependencies is such an important feature of the Workflow lib, the implementation of the standard get() methods of the Java Future interface are not supported and throw UnsupportedOperationException.  This constrains developers to a single accessor of the task result and ensures that the task dependencies are recorded reliably.

The other advantage of not using the non standard get methods is that it does not expose the Java execution exceptions to the caller.  These exceptions are handled by the getNoThrow method and are logged as task exceptions in the same manner as task implementation exceptions.  This simplifies downstream task implementations when they access the result of the Future because they no longer need to handle the standard checked exceptions.  Tasks don't care if the result of a dependent Future is null because of an execution exception or because of some application business logic error in the task, their behavior will be the same in any case.  The errors will be logged in any event so there's no loss of data and the business logic flow becomes more straightforward.

## ICallableTaskExecutor

Interface that abstracts the functionality of a task executor so an application can easily switch between and compare implementations.  Defines following methods:

* addTask - adds a task to the executor and wraps it in a profiler decorator to handle exceptions and profiling.

* getTask - returns the Future of a task in the executor by the task name

* collectResponseContext - aggregates the response contexts of all tasks submitted to the executor

Note: task names must be unique.  Multiple instances of the same task class need to be itemized with unique names.

## JavaCallableTaskExecutor

Sample executor implementation that uses the standard Java ExecutorService to manage the execution of tasks.

# Workflow

While the Callable Executor interfaces and classes offer useful extensions to basic Java concurrency patterns they don't solve the problem of managing complex applications that can have graphs of hundreds of dependent tasks.  The Workflow patterns address this by providing a way to encapsulate groups of tasks into coherent, reusable components of business logic.

Here are the basic components:

![workflow](/docs/workflow.jpg)

## IWorkflowFactory

Interface that allows clients of the library to instantiate workflows with platform specific task executors.

## IWorkflow

Interface defining a workflow.  This is a templated class that defines the Java type of the final result of the workflow.

* getTask - returns the ICallableTask that manages the execution of the workflow.  The return value of this Callable will be the result of the workflow after all the corresponding tasks have completed

* addTask - adds a task to the callable executor the workflow was initialized with

* collectResponseContext - aggregates the response contexts of all tasks that were added to the workflow.

## WorkflowTask

A CallableTask that is responsible for creating a workflow through the IWorkflowFactory interface and then executing an IWorkflowExecutor with this workflow.  The workflow task manages the lifecycle of all the tasks that have been added to its workflow.  Once the executor has completed submitting and waiting for the completion of its tasks, the WorkflowTask aggregates the ResponseContexts of those tasks into its own context.

## Workflow

Whereas in the Callable Executor the Task was the basic building block of the application, the Workflow class is a higher level abstraction that manages a set of tasks that represent a broader coherent set of business logic.

A workflow contains an instance of the currently configured executor which maintains a list of all tasks submitted to the workflow.

The workflow class imposes a constraint that every task added needs to be instantiated on the same thread.  This is important because it ensures that the lifecycle of each task can be reliably managed by the workflow's parent task execution thread.  It also creates an orderly structure of layers of tasks that helps simplify the organization of complex applications consisting of many tasks.  This constraint in no way hinders performance because a task in one workflow can have a dependency on a task in another workflow without blocking on the parent workflow aggregation task.

## IWorkflowBuilder

This interface allows the application to define specific contexts for task factories to use when determining if they should add a task to the workflow.  It maintains a collection of task factories that determine whether tasks are added to the workflow based on the request and configuration of the application.  This allows tasks to find references to their dependent tasks and allows those tasks to be recursively instantiated.

## IWorkflowExecutor

An interface for defining a unit of work that requires submitting tasks to a Workflow in order to execute its business logic.

## ITaskFactory

The task factory interface allows for a common pattern for determining whether or not a particular task should be added to a workflow.  It contains a single create method that takes the current workflow builder as a parameter and returns a task Future.  In the case the task was not added to the workflow a Null Object Future should be returned in the case that this task is an optional input to another task.

Here's an example of a task factory from the unit tests:
  
<pre><code>
public static class OptionalTaskFactory implements ITaskFactory&lt;WaitForCriticalDataOnlyBuilder, String&gt; {

    public static final OptionalTaskFactory INSTANCE = new OptionalTaskFactory();

    @Override
    public ICallableTaskFuture&lt;String&gt; create(WaitForCriticalDataOnlyBuilder builder) {
        if (builder.request.addOptional) {
            ICallableTaskFuture&lt;String&gt; task = builder.addTask(new DataSourceTask("optional", builder.async, "optional", builder.request.secondTime));
            builder.responseVisitors.add(new OptionalResponseVisitor(builder.sync, task, builder.getTask(DataSourceFactory.Factory3.INSTANCE)));
            return task;
        }
        return new CallableTaskResultNull&lt;&gt;();
    }

}
</code></pre>

## TaskInstance

A helper class that defines a single instance of a task within a workflow.  Allows multiple tasks to take a dependency on another task but ensures there's only a single instance of that task added to the workflow.

## TaskInstanceHolder

A helper class that contains a collection of TaskInstances.  Used by workflow builders to allow lazy initialization of tasks added to the workflow.  Allows other tasks to find their dependent tasks in a type safe manner without explicitly enumerating every task in the builder.

## VisitorTask

Generally the results of business logic tasks need to be added to the response object of a particular service.  Visitor tasks provide a standard way for filling out response objects based on the results of business logic tasks.  The application task will maintain a list of visitor tasks that are created by the business logic task factories.

For example here's a simple example of the result of a task being added to a response object:

<pre><code>
public class MyTaskVisitor extends VisitorTask&lt;MyTaskResult&gt; {

    private final ICallableTaskFuture&lt;List&lt;MyTaskResult&gt;&gt; task;

    public MyTaskVisitor(CallableTaskConfig taskConfig, ICallableTaskFuture&lt;List&lt;MyTaskResult&gt;&gt; task) {
        super(taskConfig, task);
        this.task = task;
    }

    @Override
    public void visit(MyResponse response) {
        List&lt;MyTaskResult&gt; taskResult = task.getNoThrow(this);
        if (taskResult != null && !taskResult.isEmpty()) {
            response.setMyResult(taskResult);
        }
    }

}
</code></pre>


It first blocks for the business logic task to complete and if it was successful, adds its result to the response.


## VisitorWorkflowExecutor

Visitor tasks are aggregated into a workflow that is managed by a VisitorWorkflowExecutor.  The executor is initialized with an IVisiteeProvider which returns the list of objects to be visited and the list of VisitorTasks that will do the visiting.  The executor adds each visitor task to its workflow as it is executed:

<pre><code>
    @Override
    public List&lt;T&gt; execute(IWorkflow&lt;T&gt; workflow) {
        List&lt;T&gt; visitees = this.visiteeProvider.get();
        if (visitees != null && visitees.size() &gt; 0) {
            for (VisitorTask&lt;T&gt; visitor : this.visitors) {
                visitor.setVisitees(visitees);
                workflow.addTask(visitor).getNoThrow(this);
            }
        }
        return visitees;
    }
</code></pre>

The VisitorWorkflowTaskFactory wraps the visitor executor in a WorkflowTask that instantiates the workflow and collects the task ResponseContexts once the executor is finished.

This allows the visitor tasks to be grouped together in the profiler tool:

![visitor_task](/docs/visitor_task.png)

## Sample Application

The WaitForCriticalDataOnly unit test is an example of a simple application that uses the interfaces and classes to demonstrate workflow execution.  It contains a top level workflow and a response visitor sub workflow.

The runTest method takes a request object that configures different test case scenarios and the IWorkflowFactory that will be used to create the workflows.

It creates an instance of IWorkflowExecutor and passes this to a new instance of a WorkflowTask that will invoke the execute() method on the executor and collect the task response contexts from the workflow when done.

It invokes the call() method directly on the class because the code is running on the top level application thread and so there's no need to submit the task to an executor.

<pre><code>
private static SampleOrchestrationResponse runTest(SampleOrchestrationRequest request, IWorkflowFactory workflowFactory, String name) {
    WaitForCriticalDataOnlyExecutor executor = new WaitForCriticalDataOnlyExecutor(request);

    WorkflowTask&lt;SampleOrchestrationResponse&gt; task = new WorkflowTask&lt;&gt;(name, CallableTaskConfig.simple(DIAGNOSTIC_CONFIG), workflowFactory, executor);
    try {
        return task.call();
    } finally {
        ProfilerHelper.print(task.getContext());
    }    
}
</code></pre>

The executor class instantiates the IWorkflowBuilder and the calls its getTask() method with the top level task factory.  The full graph of tasks is recursively created based on the dependencies of each task defined in their corresponding task factory.

<pre><code>
public class WaitForCriticalDataOnlyExecutor implements IWorkflowExecutor&lt;SampleOrchestrationResponse&gt; {

    public final SampleOrchestrationRequest request;

    public WaitForCriticalDataOnlyExecutor(SampleOrchestrationRequest request) {
        this.request = request;
    }

    @Override
    public SampleOrchestrationResponse execute(IWorkflow&lt;SampleOrchestrationResponse&gt; workflow) {
        WaitForCriticalDataOnlyBuilder builder = new WaitForCriticalDataOnlyBuilder(workflow, this.request);
        return builder.getTask(WaitOnlyForCriticalTask.Factory.INSTANCE).getNoThrow(workflow.getTask());
    }

}
</code></pre>

This is the graph of tasks generated for the optionalReady test case:

![optionalReady_workflow](/docs/optionalReady_workflow.png)

The yellow ringed nodes indicate workflow tasks.  It's a different color than its child nodes because it represents a different layer in the orchestration.  Each workflow introduces a new layer of orchestration.  The tasks managed by the workflow belong to a layer below the one the workflow task itself belongs to.

# Unit Tests
There are some unit tests that illustrate some of the more complex patterns that can be used by organizing business logic as a set of dependent tasks.

## OptionalTimeoutTest
This test defines a workflow that includes a task for fetching some critical data and 2 optional tasks that fetch data after the critical task has completed.  These optional tasks are examples of ExecType.ASYNC_TIMEOUT tasks which allows the workflow builder to assign timeout values to the execution of these tasks independent from the time these tasks block and wait for the critical dependency to complete.

### optionalWithinTimeoutTest

Here is the graph of task dependencies for the test configuration defining the optional tasks complete within the allotted time:

![optionalWithinTimeoutTest_tasks](/docs/optionalWithinTimeoutTest_tasks.png)

Note that solid nodes indicate synchronous tasks and doughnut shaped nodes are asynchronous.

Here is the profiler view of the task graph execution where you can see the optional tasks completing within the timeout value of 200ms.

![optionalWithinTimeoutTest_profile](/docs/optionalWithinTimeoutTest_profile.png)


### optionalOverTimeoutTest

This configuration of the test has the optional1 task duration set to greater than the timeout.  The graph of tasks shows the timeout task as a red node indicating the task has handled an exception, in this case a TimeoutException:

![optionalOverTimeoutTest_tasks](/docs/optionalOverTimeoutTest_tasks.png)

In the profiler view notice that the optional1_timeoutTask duration is just a little over the timeout value of 200ms.  The OptionalTimeoutTask kicks off shortly after while the optional1 task continues to run.

![optionalOverTimeoutTest_profile](/docs/optionalOverTimeoutTest_profile.png)

## WaitForCriticalDataOnlyTest

This test defines a workflow which blocks for a critical data task and also collects optional data if the corresponding task completes within the time frame of the critical task.  This is an example of implementing a custom waitForDependencies() task method.  It also illustrates the use of response visitors to populate a workflow response based on the results of other tasks.

### optionalReady

This configuration of the test shows the case where the optional data is ready before the critical task completes:

![optionalReady_tasks](/docs/optionalReady_tasks.png)

You can see in the profiler view of an execution of this test that the optional task completes comfortably before the critical task:

![optionalReady_profile](/docs/optionalReady_profile.png)

### optionalNotReady

This configuration has the optional task taking longer to process than the critical task.  Note that the WaitOnlyForCriticalTask starts its processing soon after the critical task is complete and does not block for the optional task:

![optionalNotReady_profile](/docs/optionalNotReady_profile.png)

### criticalFatalException

This test shows how an ApplicationException thrown in the critical task propagates through the workflow and aborts the execution of the response visitors:

![criticalFatalException_tasks](/docs/criticalFatalException_tasks.png)

### criticalFatalExceptionSyncTask

Illustrates how an ApplicationException in a synchronous task is propagated through the workflow.  It verifies that the ResponseVisitorTask workflow task does not duplicate profiler entries when its synchronous future is called multiple times:

![criticalFatalExceptionSyncTask_tasks](/docs/criticalFatalExceptionSyncTask_tasks.png)

## WaitForFastestTest

Shows an example of how to use a count down latch task to trigger the execution of another task.

### secondFastest

Configures the workflow builder so that the second task completes before the first.  When the second task completes it decrements the count on the latch and this unblocks the WaitForSingleCountDownLatchTask which is waiting on the latch.  The LatchTestResponseBuilderTask then populates the response object with the result of the latch task.

![secondFastest_tasks](/docs/secondFastest_tasks.png)

Note the dangling firstTask node that indicates that the result of this task is ignored.

In the profiler view it is clear that the response builder task starts executing once the second task has triggered the latch:

![secondFastest_profile](/docs/secondFastest_profile.png)

