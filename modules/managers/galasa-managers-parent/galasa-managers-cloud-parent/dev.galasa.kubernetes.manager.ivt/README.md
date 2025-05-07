## Kubernetes Manager IVT

This is an IVT for the Kubernetes Manager, which tests that the manager can be used to manipulate Kubernetes Resources.

### Running the IVT locally

To run the IVT locally:

1. Install [minikube](https://minikube.sigs.k8s.io/docs/start) and start it by running `minikube start`

2. Once minikube is started, start the Kubernetes API server by running `minikube kubectl proxy` (or `kubectl proxy` if kubectl is installed on your system)

3. Add the following properties to your `cps.properties` file:
    ```properties
    kubernetes.cluster.K8S.namespaces=default
    kubernetes.cluster.K8S.url=http://localhost:8001
    ```

4. Run the IVT:
   ```
   galasactl runs submit local --obr mvn:dev.galasa/dev.galasa.uber.obr/0.42.0/obr --class dev.galasa.kubernetes.manager.ivt/dev.galasa.kubernetes.manager.ivt.KubernetesManagerIVT --log -
   ```
