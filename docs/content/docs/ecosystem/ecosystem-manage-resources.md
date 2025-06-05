---
title: "Configuring an Ecosystem using resource files"
---

You might want to create or update a number of different Galasa properties and associated values at the same time, for example to configure a different Galasa Ecosystem. A good way to do this is by using a YAML file containing Galasa resources and their associated values to set the configuration of your Ecosystem, as described in the [Creating and updating resources using a YAML resource file](#creating-and-updating-resources-using-a-yaml-file) section.

## Configuration Properties as GalasaProperty resources

The topic [Managing integrated test runs](./ecosystem-manage-cps.md) describes how to use the `galasactl properties get` command with the `--format yaml` flag specified to extract a YAML file which describes Galasa properties and property values. 

If more than one property is returned, each property is separated in the file by three dashes, `---`, as shown in the following example: 

```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: engine.LOCAL.hostname
  namespace: docker
data:
  value: 127.0.0.1
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: engine.REMOTE.hostname
  namespace: docker
data:
  value: 103.67.89.6
```


Alternatively, if you want to create a new YAML file, you can do so but you must use the following example format:


```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: my.property1
  namespace: myNamespace1
data:
  value: myValue1
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: my.property2
  namespace: myNamespace2
data:
  value: myValue2
```

where:
- `apiVersion` is the version of the API that you are using
- `name` is the name of the property that you want to create or update
- `namespace` is the namespace in which the property is contained in the configuration properties store (cps.properties). 
- `value` is the value that you want to assign to the specified property in the specified namespace


You can define multiple properties and values in the same YAML file by separating them using three dashes, `---`, as shown in the example.

You can save the file with a `.yaml` or `.yml` file extension.


## Credentials as GalasaSecret resources

The topic [Managing credentials in an Ecosystem](./ecosystem-manage-creds.md) describes how to use the `galasactl secrets get` command with the `--format yaml` flag specified to extract a YAML file which describes Galasa secrets.

If more than one secret is returned, each secret is separated in the file by three dashes, `---`, as shown in the following example: 

```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaSecret
metadata:
  name: SYSTEM1
  type: UsernamePassword
  encoding: base64
  description: an example username and password secret
  lastUpdatedTime:
  lastUpdatedBy: myuser
data:
  username: <base64-encoded-username>
  password: <base64-encoded-password>
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaSecret
metadata:
  name: SYSTEM2
  type: Token
  encoding: base64
  description: an example token secret
  lastUpdatedTime:
  lastUpdatedBy: anotheruser
data:
  token: <base64-encoded-token>
```

You can update the values in this YAML file and then create, update, or apply those updates by using the galasactl command line tool, as described in the following section. 


Alternatively, if you want to create a new YAML file, you can do so using the following example format:


```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaSecret
metadata:
  name: SYSTEM1
  type: UsernameToken
  description: an example username and token secret
data:
  username: <username>
  token: <token>
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaSecret
metadata:
  name: SYSTEM2
  type: Username
  encoding: base64
  description: an example username secret
data:
  username: <base64-encoded-username>
```

where:
- `apiVersion` is the version of the API that you are using
- `name` is the name of the secret that you want to create or update
- `type` is the type of secret that you want to create or update. Supported values are: `Username`, `UsernamePassword`, `UsernameToken`, and `Token`
- `encoding` is an optional encoding scheme applied to all fields in the `data` section. The currently available value for this is `base64`. If this is not provided, then the fields in the `data` section are assumed to be provided without any encoding applied
- `description` is an optional field that allows you to supply a description associated with the secret being created or updated
- `username` is the username value to be set for a secret, and is provided when the `type` is `Username`, `UsernamePassword`, or `UsernameToken`
- `password` is the password value to be set for a secret, and is provided when the `type` is `UsernamePassword`
- `token` is the token value to be set for a secret, and is provided when the `type` is `Token` or `UsernameToken`


You can define multiple secrets in the same YAML file by separating them using three dashes, `---`, as shown in the example.

You can save the file with a `.yaml` or `.yml` file extension.


## Creating and updating resources using a YAML file

You can use the galasactl command line tool to submit a YAML file to create new Galasa resources, including properties and credentials, or to update existing ones. The YAML files can contain different types of Galasa resources, where each resource is separated by three dashes `---`. For example, a GalasaProperty resource and a GalasaSecret resource can both be created using the following format:

```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaSecret
metadata:
  name: SYSTEM1
  type: Token
  description: an example token secret
data:
  token: myToken
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
  name: my.property1
  namespace: myNamespace1
data:
  value: myValue1
```

Use the following command to create Galasa resources by using a YAML file called `myFile.yaml`:

```shell
galasactl resources create -f myFile.yaml
```

Use the following command to update Galasa resources by using a YAML file called `myFile.yaml`:

```shell
galasactl resources update -f myFile.yaml
```

Use the following command to create a new Galasa resource if the resource does not exist or update an existing resource by using a YAML file called `myFile.yaml`:

```shell
galasactl resources apply -f myFile.yaml
```

An error message is returned if the action is not able to complete successfully. For example, if a property could not be created in a particular namespace because the property already exists in that namespace.
 

## Deleting resources using a YAML file

When maintaining an Ecosystem, you might have a YAML file containing Galasa property resource definitions and want to delete a corresponding set of Galasa resources stored on the server in an Ecosystem. You can do this by using the following command: 

```shell
galasactl resources delete -f {filename}
```

where:

`{filename}` is the name of the YAML file that contains the list of Galasa resources that you want to delete.

For example, you might have a list of resources that you want to delete in a file called `resources_to_delete.yaml`. You can delete those resources by running the following command:

```
galasactl resources delete -f resources_to_delete.yaml
```


For a complete list of supported parameters see the [galasactl resources](https://github.com/galasa-dev/cli/blob/main/docs/generated/galasactl_resources.md){target="_blank"} documentation in the Galasa CLI repository.
