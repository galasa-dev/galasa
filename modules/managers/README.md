# Galasa Managers
This repository contains code relating to Galasa Managers. Managers are the power-houses of Galasa and are used to reduce the amount of boilerplate code within a test and provide proven tool interaction code.
The repository has a hierarchical structure with a "galasa-managers-parent" folder at the top containing a "pom.xml" file, listing all currently available Managers.  
Managers are grouped by type within the parent folder, for example, the "galasa-managers-core-parent" folder contains the code and IVT (Installation Verification Test) tests for the Core Manager and Artifact Manager.
Each Manager comes with its own set of associated IVT tests and Javadoc. 
We're adding new Manager every month. Look at the [summary table of Managers](https://galasa.dev/docs/managers/) for a list of available and planned Managers and to find out what they do. 


## Contributing
If you are interested in the development of Galasa, take a look at the documentation and feel free to post a question on the <a href="https://join.slack.com/t/galasa/shared_invite/zt-ele2ic8x-VepEO1o13t4Jtb3ZuM4RUA" target="_blank"> Galasa Slack channel</a>, or start sharing usage and development experiences with other Galasa users and the IBM team. You can also raise new ideas / features / bugs etc. as issues on [GitHub](https://github.com/galasa-dev/projectmanagement).  

Take a look at the [contribution guidelines](https://github.com/galasa-dev/projectmanagement/blob/main/contributing.md) and the [Contributor's Guide](https://github.com/galasa-dev/galasa/blob/main/CONTRIBUTING.md).
 

## Documentation
More information can be found on the [Galasa Homepage](https://galasa.dev). Questions related to the usage of Galasa can be posted on the [Galasa Slack channel](https://galasa.slack.com).


## Where can I get the latest release?
Find out how to install the Galasa Eclipse plug-in from our [Installing the Galasa plug-in](https://galasa.dev/docs/getting-started/installing) documentation.

Other Galasa repositories are available on [GitHub](https://github.com/galasa-dev). 


## License
This code is under the [Eclipse Public License 2.0](https://github.com/galasa-dev/galasa/blob/main/LICENSE).


## Building locally
Use the `./build-locally.sh` script to build locally.


## Updating the versions of things
Use the `./build-release-yaml.sh` script to scan the contents of the managers source code and populate the `release.yaml` file with version information taken from each manager source folder.

## Manager testing and documentation levels
Below is a table with the currently available Galasa Managers as seen documented on galasa.dev or visible on Maven Central, the level at which they were tested/are being tested, and the readiness indicator as described on the Galasa website.

**Manager readiness indicator:**

- Alpha: This Manager is being actively developed. It is subject to change and has not been extensively tested.
- Beta: This Manager is almost ready. It has been tested and the TPI is stable, but there may be minor changes to come.
- Release: This Manager is feature complete, has passed all tests and is deemed release grade.

**Manager testing levels:**

- Local: This Manager has been tested in a local Galasa Ecosystem
- Isolated: This Manager has been tested in a Galasa Ecosystem with the Galasa Isolated configuration.
- MVP: This Manager has been tested in a Galasa Ecosystem with the Galasa MVP configuration as it is shipped as part of the MVP.
- IVT: This Manager has been tested locally during development.
- Other Managers: This Manager does not have its own test but is used for the provisioning of other tests.


**Managers documented on galasa.dev:**
| Manager | Level of testing | Documented |
| --- | --- | --- |
| CICSTS Managers || 
| CICSTS CECI | CECIManagerIVT ran in a Local and Isolated Ecosystem | Release |
| CICSTS CEDA | CedaManagerIVT ran in a Local and Isolated Ecosystem | Alpha |
| CICSTS CEMT | CEMTManagerIVT ran in a Local and Isolated Ecosystem | Alpha |
| CICSTS | CICSTSManagerIVT ran in a ran in a Local and Isolated Ecosystem | Alpha |
| Cloud Managers || 
| Docker | DockerManagerIVT ran in a Local Ecosystem | Release | 
| Kubernetes | KubernetesManagerIVT ran locally during development | Alpha |
| OpenStack | Other Managers | Alpha |
| Communication Managers || 
| HTTP | HttpManagerIVT ran in a Local Ecosystem | Release | 
| IP Network | Other Managers | Alpha |
| MQ | MqManagerIVT ran locally during development | Alpha |
| Core Managers || 
| Artifact | ArtifactManagerIVT ran in a Local, Isolated and MVP Ecosystem | Release |
| Core | CoreManagerIVT ran in a Local, Isolated and MVP Ecosystem | Release |
| Logging Managers || 
| Elastic Log | - | Alpha | 
| Ecosystem Managers || 
| Galasa Ecosystem | Other Managers | Alpha | 
| Test Tool Managers || 
| JMeter | JMeterManagerIVT ran locally during development | Beta |
| Selenium | SeleniumManagerIVT ran locally during development | Beta |
| Unix Managers || 
| Linux | LinuxManagerIVT ran locally during development / Other Managers | Alpha |
| Workflow Managers || 
| GitHub Issue | Other Managers (Adhoc) | Release | 
| z/OS Managers || 
| RSE API | - | Alpha |
| z/OS 3270 | Zos3270IVT ran in a Local Ecosystem | Beta | 
| z/OS Batch z/OS MF | ZosManagerBatchIVT ran in a Local, Isolated and MVP Ecosystem with overrides = "zos.bundle.extra.batch.manager": "dev.galasa.zosbatch.rseapi.manager" | Beta | 
| z/OS Batch RSE API | ZosManagerBatchIVT ran in a Local, Isolated and MVP Ecosystem with overrides = "zos.bundle.extra.batch.manager": "dev.galasa.zosbatch.zosmf.manager" | Alpha |
| z/OS Console OE Console | - | Alpha | 
| z/OS Console z/OS MF | - | Beta | 
| z/OS File RSE API | ZosManagerFileIVT, ZosManagerFileDatasetIVT and ZosManagerFileVSAMIVT ran in a Local, Isolated and MVP Ecosystem with overrides = "zos.bundle.extra.file.manager": "dev.galasa.zosfile.rseapi.manager" | Alpha |
| z/OS File z/OS MF | ZosManagerFileIVT, ZosManagerFileDatasetIVT and ZosManagerFileVSAMIVT ran in a Local, Isolated and MVP Ecosystem with overrides = "zos.bundle.extra.file.manager": "dev.galasa.zosfile.zosmf.manager" | Beta | 
| z/OS | ZosManagerIVT ran in a Local, Isolated and MVP Ecosystem  | Beta |
| z/OS MF | Other Managers | Beta | 
| z/OS Program | Other Managers (CECI) | Alpha | 
| z/OS TSO Command SSH Manager | ZosManagerTSOCommandIVT ran in a Local, Isolated and MVP Ecosystem | Alpha | 
| z/OS Unix Command SSH Manager | Other Managers | Alpha | 


**Managers documented as 'Future Managers' on galasa.dev but already released on Maven Central:**
| Manager | Progress | 
| --- | --- |
| Liberty v0.21.0 | Empty interfaces but nothing implemented |
| Windows v0.21.0 | Minimal implementation |


**Managers not documented on galasa.dev but already released on Maven Central:**
| Manager | Level of testing |
| --- | --- |
| CICSTS Resource | - | 
| Cloud | - | 
| DB2 | Db2ManagerIVT ran locally during development | 
| Eclipse Runtime | - |
| Eclipse Runtime Ubuntu | - |
| Java | - |
| Java Ubuntu | Other Managers |
| Java Windows | - |
| Text Scan | Other Managers |
| z/OS Liberty Angel | - | 
| z/OS Liberty | - | 


**Managers set to be removed from Open Source Galasa:**
* Phoenix
* VTP