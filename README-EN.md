[中文说明](README.md)
# DBeaver Agent for 24.x

This branch is specifically for version `24.x`. For other lower versions, please refer to the `master` branch.

## Supported Versions

- `24.3`
- `24.2`
- `24.1.x`
- `24.0.x`

## Dependencies

Since the unit tests utilize code from DBeaver, you need to prepare some DBeaver packages. Place the following packages into the `libs` folder:

- `com.dbeaver.ee.runtime`: Basic runtime, contains information for obtaining keys, etc.
- `com.dbeaver.lm.api`: License core
- `org.jkiss.utils`: Provides components for license generation
- For DBeaverUltimate, the public key is located in `com.dbeaver.app.ultimate`
- For Cloudeaver, the public key is located in `io.cloudbeaver.product.ee`

## Usage

Simply build the project using `mvn package`. The generated `dbeaver-agent.jar` can be placed anywhere you prefer.

> However, it is recommended to place it in the installation directory.

Modify the `dbeaver.ini` file in the DBeaver installation directory to add some parameters. Add `-javaagent:{your jar path}` and `-Xbootclasspath/a:{your jar path}` right below the `-vmargs` line, like this:

```ini
-startup
plugins/org.eclipse.equinox.launcher_1.6.100.v20201223-0822.jar
--launcher.library
plugins/org.eclipse.equinox.launcher.gtk.linux.x86_64_1.2.100.v20210209-1541
-vmargs
-javaagent:/usr/share/dbeaver/dbeaver-agent.jar
-Xbootclasspath/a:/usr/share/dbeaver/dbeaver-agent.jar
-XX:+IgnoreUnrecognizedVMOptions
--add-modules=ALL-SYSTEM
-Dosgi.requiredJavaVersion=11
-Xms128m
-Xmx2048m
```

Then, you need to remove the bundled JRE from DBeaver.

> For DBeaver >= 24, you need to provide Java 17 yourself.

## Generating License Keys

Command-line key generation is now supported. Run the following command:

```shell
java -cp libs/\*:./target/dbeaver-agent-1.0-SNAPSHOT-jar-with-dependencies.jar \
    dev.misakacloud.dbee.License
```

By default, the generated key is for `Dbeaver Enterprise Edition 24.0`. If you need keys for other types, you can specify them using the following parameters:

```shell
java -cp libs/\*:./target/dbeaver-agent-1.0-SNAPSHOT-jar-with-dependencies.jar \
    dev.misakacloud.dbee.License -h

Usage: gen-license [-h] [-p=<productName>] [-t=<licenseType>]
                   [-v=<productVersion>]
Generate DBeaver license
  -h, --help
  -p, --product=<productName>
                             Product name, you can choose dbeaver or
                               cloudbeaver, default is dbeaver
  -t, --type=<licenseType>   License type, you can choose Lite version(le),
                               Enterprise version(ee) or Ultimate version(ue)
                               default is ue
  -v, --version=<productVersion>
                             Product version, default is 24
```
