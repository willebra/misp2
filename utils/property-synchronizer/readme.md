# Property Synchronizer

This is the source code for the utility jar `propertySynchronizer.jar` that is located in the directory `/usr/xtee/app`
in a MISP2 installation.

The utility is used to synchronize `.properties` files for MISP2 during installations and upgrades. For more
information on the utility and how to use it, please refer to the [user manual](./manual.md).

## Building the package

The package can be built from the root directory by running the following command:

```bash
./gradlew :property-synchronizer:shadowJar
```
