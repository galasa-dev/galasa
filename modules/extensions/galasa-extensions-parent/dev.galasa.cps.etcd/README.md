# The cps etcd extension

This extension provides access to an implementation of a CPS store based based on remotely connecting to an etcd server.


## How the packaging works

This extension is perhaps slightly strange in that it doesn't every want to go out to maven central, so any dependencies it needs to run
must be included in the osgi bundle.

The extension therefore needs to get hold of the transitive dependencies and include them in a 'fat jar' bundle.

If you look at the built bundle (in the `galasa-extensions-parent/dev.galasa.cps.etcd/build/libs` folder) and unpack the jar, you will see that it packs up the dependndency jars in the root of the bundle jar.

There are 2 parts to packaging this extension:
- The `build.gradle` file downloads everything that it needed to compile, but it also downloads all the transitive dependencies
- The `bnd.bnd` file which controls which jars are included in the 'fat jar'.

The gradle plugin `biz.aQute.bnd.builder` is used to do the packing-up of the dependency jars, based on the content of the `bnd.bnd` file.

## How to upgrade the dependencies

- Remove the dependencies from the `build.gradle` file which are only there to make sure things are downloaded which can be bundled into the fat jar.
- Upgrade what you need to in the `build.gradle`. For example, bump the value for the `io.etcd:jetcd-core` bundle might be what you are trying to do.
- Run the `calculate-transitive-dependencies.sh` script (in the root of this module). This script calculates the transitive depdendencies of everything which is in the build.gradle file. That's why we need to clean the `build.gradle` to contain only the minimum to start with.
- Move the contents of `temp/dependencies_gradle_imports.txt` to the `build.gradle` file.
- Move the contents of `temp/dependencies_gradle.txt` into the `bnd.bnd` file.

This should result in a long list of jars which are first downloaded, then packaged into the extension bundle jar.
