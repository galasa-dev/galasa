# Test run lifecycle

When a test runs, it passes through many states which may be apparent to anyone querying the test run as it gets ready to execute, executes, and cleans up.

```mermaid
---
title: Test run states
---
stateDiagram-v2
    [*] --> queued :  api server submits the run
    waiting --> queued : re-queued after a back-off time has expired
    queued: "Queued" awaiting and engine controller to scheduled it
    queued  --> allocated : Engine controller creates a pod
    allocated: "Allocated" pod has been created to run the test
    queued --> cancelling : cancel 
    note right of cancelling: User has cancelled the test. It can't now be scheduled by the engine controller
    cancelling --> finished
    allocated --> started : kube starts code running on the pod
    started: "Started" test pod is running java code
    started --> building
    building --> provstart
    provstart --> generating
    generating --> up
    generating --> waiting : No resources
    up --> running
    running --> rundone : test code executes
    rundone --> ending
    ending --> finished
    finished --> [*]
```


If a manager used by the test fails to acquire the required resources, it enters the `waiting` state, and stays there for a while, then gets re-queued.

