# Changes for 0.8.0

| PM Issue      | Change        |
| ------------- | ------------- |
| 177  | zOS 3270 Terminal has changed InterruptedException to TerminalInterruptedException to make test catches easier |
| JATconv | Handle READ BUFFER and rejected device selection |
| 258 | Refactor zOS TSO and UNIX command managers |
| 246 | New TPI in Artifact manager to make streaming text content a single step process |
| JATconv | The 3270 Manager will no longer throw an Exception for an invalid Order byte, it will replace it with a space on the screen and write a trace message to the log |
