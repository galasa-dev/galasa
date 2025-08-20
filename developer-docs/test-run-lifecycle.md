# Test run lifecycle

When a test runs, it passes through many states which may be apparent to anyone querying the test run as it gets ready to execute, executes, and cleans up.

```mermaid
---
title: Test run states
---
stateDiagram-v2
    [*] --> queued : submitted
    waiting --> queued : re-queued
    queued  --> allocated
    queued --> cancelling : cancel
    cancelling --> finished
    allocated --> started
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