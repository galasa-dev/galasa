---
title: "Key principles for writing tests"
---

These suggestions are written in the anticipation that your tests are ultimately to run in an automation environment targeting multiple application instances at the same time. Despite the fact that when you first start working with Galasa, you may be running tests locally, sequentially, and against just a single target environment, it is recommended that you follow these guidelines to develop good habits for testing in the large.

<details><summary>Expect a test to be run multiple times in parallel</summary>

- In a CI/CD pipeline, it is very likely that an automated test will be running against different target environments at the same time.
- When using remote resources, ensure their use will not clash with another test instance.
- Use the Galasa framework to understand which test instance you are testing against.
- Do not hard code any locations, ports or names in your tests - these might change.
- Do not assume a specific number of test environments - this might scale dynamically.
  </details>

<details><summary>Do not hard code resource names</summary>

- If you have not developed an application Manager to describe your target environment, then use test properties to pass resource names to the test.
- If you hard code resource names, application ids, target hostnames and so on, it means that the test is not portable. You will be unable to run the same test against a development target environment one day and a QA environment the next.
- By not hard coding resource names, you reduce the technical debt being built up within your test code.
  </details>

<details><summary>Many short, sharp tests are far better than a few long tests</summary>

- Lots of short, sharp tests means you can run more tests in parallel. Galasa's ecosystem has been designed for scale, and can cope with thousands of tests running in parallel.
- In a CI/CD pipeline, the more parallelism you can introduce, the more testing can happen in the shortest time, meaning your developers can receive feedback faster.
- A single test class that takes an hour to run six test methods can be run in ten minutes if those six methods could be split apart and run in parallel.
  </details>

<details><summary>Use the facilities provided by Managers instead of writing your own code</summary>

- If there is a Manager that does what you need, then use it instead of writing your own code in the test. Managers' code uses best practice and has been battle-proven. If a better solution for a Manager arrives, your tests will automatically benefit.
- As you write your own tests, you will discover common code between them that could be abstracted into an Application Manager, so expect some churn and volatility as your test's functionality settles down.
- Using Managers helps to reduce the technical debt building up in your test code.
  </details>

<details><summary>Understand the difference between testing and exercising code</summary>

- When you test a specific function, you will generally examine all parts of it - that is, all aspects of the UI and API, and confirm that any logging or audit messages are printed correctly. However, once satisfied that it is working, you might want to exercise that function while testing another function. When simply exercising a function, you can choose to not examine all parts of it as if you were testing it, and just accept that it works or not. This speeds up your tests.
  </details

<details><summary>Use <code>@Before</code> and <code>@After</code> to clean up state</summary>

- A method annotated with `@Before` runs before each test method. This can be very useful for resetting resources such as HTTP clients or screens before each test invocation. Similarly, a method annotated with `@After` only runs after each test method, and again can be used for resetting resources after a test method completes.
  </details>

<details><summary>Understand your meta-information</summary>

- Meta-information held within annotations in your test class can be built into a test catalog and used when selecting tests to run or reporting on tests that have run.
- How will you divide up your tests? Will this be based on functional area, type of test performed and so on? Codify this into each test through annotations as soon as you can to inject structure into your tests.
  </details>

<details><summary>Scope tests well</summary>

- Although you might be creating integration tests with Galasa that require interactions with many components, it is important that each test is scoped to test just a single application function.
- Test classes that test multiple functions don't scale well, and are more difficult to maintain and debug as they will contain a lot of information.
  </details>

<details><summary>Make tests easy to debug</summary>

- Use the logger via the `@Logger` annotation to pepper the run log with test state, possibly logging variables pertinent to the running test. This will help align the run log and the test class when analysing a problem. There is an example of this annotation in the SimBank <a href="/docs/running-simbank-tests/provisioned-account-credit-tests" target="_blank">`ProvisionedAccountCreditTest`</a> example.
- When your test encounters an error, use the logger to be explicit about the type of error encountered. Messages such as `Did not see the expected result` are far less useful than `Expected to see message-A but actually saw message-B`.
- Use the Stored Artifact facility to save test material that will help you to diagnose test failures when tests are running in an unsupervised manner. It is annoying when the rerun of a test works flawlessly, but you know perfectly well that there is still a bug in there somewhere.
- The Stored Artifact facility can flag particularly interesting information. Use it in failure scenarios to raise engineers' attention to the key failure-causing output. There is an example of this annotation in the SimBank <a href="/docs/running-simbank-tests/provisioned-account-credit-tests" target="_blank">`ProvisionedAccountCreditTest`</a> example.
  </details>

<details><summary>Your test code is a valuable asset</summary>

- Your automation test code is just as important to your business as your application code, as it is a key indicator of application quality.
- If your test code is poor, the whole view of the quality of your application is jeopardised.
- All test code should be source control managed.
- Consider using a static code analyser or performing buddy checks to ensure your test code is of a high quality.
  </details>
