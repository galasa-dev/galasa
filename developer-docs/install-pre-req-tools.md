# Installing required tools in order to build locally

Before you go very far, you will need tools to develop things.

These are the versions of software tools which are known to work:
- Python v3.11.0
- Gradle 8.9
- Apache Maven 3.9.0
- openjdk 17.0.12
- go 1.23.5
- node v20.10.0
- nvm 0.39.7
- sdkman script: 5.18.2 native: 0.4.6
  
## Python 

### Install pyenv
Use pyenv so you can install multiple versions of python and switch between them easily.

- You might find [this article](https://realpython.com/intro-to-pyenv/) about pyenv useful
- Installation instructions are on [the pyenv github site](https://github.com/pyenv/pyenv)

Summary for Mac:
```shell
brew update
brew install pyenv
```

Add this to your `~/.zprofile` file:
```shell
export PYENV_ROOT="$HOME/.pyenv"
export PATH="$PYENV_ROOT/shims:$PATH"
if command -v pyenv 1>/dev/null 2>&1; then
  eval "$(pyenv init -)"
fi
```

Useful commands:

- `pyenv install --list` - lists the versions of python which are available
- `pyenv install 3.11.0` - installs python version 3.10.4

### Install python 3.9 or above

```shell
pyenv install 3.11.0
pyenv global 3.11.0
```

Before using pip to install Python modules, make sure it is at the most current version.


```shell
pip install --upgrade pip
```

## SDKman

[SDKman](https://sdkman.io/) is a platform-independent tool which installs other tools.

Install `SDKman`:
```shell
curl -s "https://get.sdkman.io" | bash   
```

Execute this on the current command-line session:
```shell
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
```


## Java
### Install Java using SDKman

List the available java releases
```shell
sdk list java
```

Install the semeru java 11 release:
```shell
sdk install java 11.0.16.1-sem
```

Add this to your `~.zprofile` :
```shell
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
export SDKMAN_JAVA_VERSION="11.0.16.1-sem"
sdk default java ${SDKMAN_JAVA_VERSION}
sdk use java ${SDKMAN_JAVA_VERSION}
```


## Gradle

List the available gradle versions
```shell
sdk list gradle
```

Install one known to work
```shell
sdk install gradle 8.9
```

Set this version as the default
```shell
sdk default gradle 8.9
```

You need to put gradle on your path. Add this to your `~/.zprofile`:
```shell
export PATH="/opt/homebrew/opt/gradle@6/bin:$PATH"
gradle --version | grep "Gradle" | cut -f2 -d' '
```

You also need to make sure that gradle tells the JVM where it should find certificate
authorities it can trust.

We recommend that you change your java SSL trust store password. 
Reflect that in an environment variable.
```shell
export JAVA_SSL_TRUST_STORE_PASSWORD="changeit"
```

```shell
cat << EOF > ~/.gradle/gradle.properties
# This file is created by ~/.zprofile whenever it runs.
org.gradle.java.home=${JAVA_HOME}
systemProp.javax.net.ssl.trustStore=${JAVA_HOME}/lib/security/cacerts
systemProp.javax.net.ssl.trustStorePassword=${JAVA_SSL_TRUST_STORE_PASSWORD}
EOF
```
> Put the above code in my .zprofile so that even if I change/upgrade JDKs, it should still work.

## Maven
List the available versions of maven
```shell
sdk list maven
```

Install a version of maven which is known to work
```shell
sdk install maven 3.9.0
```

Add this to your `~/.m2/settings.xml` file:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
          
  <pluginGroups>
    <pluginGroup>dev.galasa</pluginGroup>
  </pluginGroups>

    <profiles>
        <profile>
            <id>galasa</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <repositories>
                <repository>
                    <id>maven.central</id>
                    <url>https://repo.maven.apache.org/maven2/</url>
                </repository>
                <!-- To use the bleeding edge version of galasa, use the development obr -->
                <repository>
                    <id>galasa.repo</id>
                    <url>https://development.galasa.dev/main/maven-repo/obr</url> 
                    <releases>
						<enabled>true</enabled>
					</releases>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
                </repository> 
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>maven.central</id>
                    <url>https://repo.maven.apache.org/maven2/</url>
                </pluginRepository>
                <!-- To use the bleeding edge version of galasa, use the development obr -->
                <pluginRepository>
                    <id>galasa.repo</id>    
                    <url>https://development.galasa.dev/main/maven-repo/obr</url>
                    <releases>
						<enabled>true</enabled>
					</releases>
					<snapshots>
						<enabled>true</enabled>
					</snapshots>
                </pluginRepository> 
             </pluginRepositories>
         </profile>
     </profiles>

</settings>
```
That tells maven where it can find some custom maven plugin tools used by the build.



## Node
First install the Node.js Version Manager (nvm) like this:
```shell
brew install nvm
```

Create a directory for nvm:
```shell
mkdir ~/.nvm
```

Then edit your ~/.zprofile file to add:
``` shell
export NVM_DIR="$HOME/.nvm"
[ -s "/opt/homebrew/opt/nvm/nvm.sh" ] && \. "/opt/homebrew/opt/nvm/nvm.sh"  # This loads nvm
[ -s "/opt/homebrew/opt/nvm/etc/bash_completion.d/nvm" ] && \. "/opt/homebrew/opt/nvm/etc/bash_completion.d/nvm"  # This loads nvm bash_completion
```

Verify nvm is installed:
```shell
nvm --version
```

Install the latest long term support (LTS) version of Node.js:
```shell
nvm install --lts
```

Check the version installed:
```shell
nvm ls
```

Configure your environment to use this version:
```shell
nvm use default 
```

Try running Node.js itself:
```shell
node -v
```

You should see output like this:
```
v14.17.6
```

## Go
v1.23.5 or higher from [go.dev](https://go.dev/doc/install).

## mkdocs
mkdocs is a tool for rendering `.md` files into html pages.
We use it to generate this `developer-docs` site from [the repository on github enterprise](https://github.ibm.com/galasa/developer-docs)

As it says in the [mkdocs documentation](https://www.mkdocs.org/getting-started/), install the tool like this:
```shell
pip install mkdoc
```

# Extra helpful tools
These tools are not 'needed' but you might find them helpful at some point:

- `wget` - This is useful if you want to download a file from a URL
- `tree` - Gives a pretty-print of folders and the files within them
- `draw.io` - A drawing tool which can render to png/jpg
