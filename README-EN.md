[中文说明](README.md)

# DBeaver Agent for 25.x

This branch targets the `25.x` version. If you need to reference the `24.x` version, please see the [v24.0](https://github.com/wgzhao/dbeaver-agent/tree/v24.0). For other older
versions, please refer to the `master` branch.

## Supported Versions

- **Supported DBeaver Version**: `25.0`

## Dependencies

To run unit tests, you need to prepare the following DBeaver packages and place them in the `libs` folder:

- `com.dbeaver.ee.runtime`: The core runtime that contains components required for obtaining keys and other information.
- `com.dbeaver.lm.api`: The core library for license management.
- `org.jkiss.utils`: Provides utility components for license generation.
- For DBeaver Ultimate, the public key is located in `com.dbeaver.app.ultimate`.
- For CloudBeaver, the public key is located in `io.cloudbeaver.product.ee`.

## Usage Instructions

### 1. Build the Project

First, use Maven to build the project, which will generate a jar file containing all dependencies:

```bash
mvn package
```

The generated file can be found at `target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar`.

### 2. Install DBeaver Agent

Move the generated jar file to the DBeaver installation path (recommended):

```shell
cp target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar /usr/share/dbeaver/dbeaver-agent.jar
```

### 3. Configure DBeaver

Modify the `dbeaver.ini` file in the DBeaver installation directory and add the following parameters:

```ini
-vmargs
-javaagent:/usr/share/dbeaver/dbeaver-agent.jar
-Xbootclasspath/a: /usr/share/dbeaver/dbeaver-agent.jar
```

Make sure these parameters are placed below `-vmargs`.

### 4. Handle JRE Dependencies

If you are using JRE 21 and it can be found in the system path, no additional operations are required. If the default JRE version is not 21, copy the installed JRE to the DBeaver
installation directory so that it is at the same level as the built-in `jre` folder. Non-Windows systems can also use symbolic links.

### 5. Block the stats.dbeaver.com Domain

To prevent DBeaver from sending data to stats.dbeaver.com, you can block the domain by modifying the hosts file. Add the following entry to the hosts file:

```shell
127.0.0.1 stats.dbeaver.com
```

## Generate License Key

Now, you can generate a license key via the command line by running the following command:

```bash
java -cp libs/\*:./target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar \
    com.dbeaver.agent.License
```

If you are a Windows user, the command might look like this:

```shell
java -cp libs\*;.\target\dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar \
    com.dbeaver.agent.License
```

This command will generate a license key for the DBeaver Enterprise Edition 25.0 by default. If you need to generate keys for other types, you can specify using the following
parameters:

```shell
java -cp libs/\*:./target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar \
com.dbeaver.agent.License -h

Usage: gen-license [-h] [-p=<productName>] [-t=<licenseType>] [-v=<productVersion>]
Generate DBeaver license
-h, --help                   Show help information
-p, --product=<productName>  Optional: Specify product name, options include dbeaver or cloudbeaver, default is dbeaver
-t, --type=<licenseType>     Optional: Specify license type, options include Lite version(le), Enterprise version(ee) or Ultimate version(ue), default is ue
-v, --version=<productVersion> Optional: Specify product version, default is 25
```

## First-time License Registration

When registering the license for the first time, it is recommended to start DBeaver from the command line to observe detailed log messages, which can help in troubleshooting.

```shell
# Example command to start DBeaver
/path/to/dbeaver/dbeaver
```
