[English](README-EN.md)

# DBeaver Agent for 25.x

该分支针对 `25.x` 版本，若需参考 `24.x`版本，请查看 [v24.0](https://github.com/wgzhao/dbeaver-agent/tree/v24.0) 分支，其他低版本则请参考 `master` 分支。

## 支持的版本

- `25.0`

## 依赖

为了运行单元测试，需要准备 DBeaver 的以下包，并将其放置于 `libs` 文件夹：

- `com.dbeaver.ee.runtime`: 基础运行时，包含获取密钥等信息所需的组件
- `com.dbeaver.lm.api`: 用于许可管理的核心库
- `org.jkiss.utils`: 提供一些用于许可生成的工具组件
- 对于 DBeaver Ultimate，公钥位于 `com.dbeaver.app.ultimate`
- 对于 CloudBeaver，公钥位于 `io.cloudbeaver.product.ee`

## 使用说明

### 1. 构建项目

首先，使用 Maven 构建项目，生成包含所有依赖的 jar 文件：

```bash
mvn package
```

生成的文件路径为 `target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar`。

### 2. 安装 DBeaver Agent

将生成的 jar 文件移动到 DBeaver 的安装路径下（推荐）：

```shell
cp target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar /usr/share/dbeaver/dbeaver-agent.jar
```

### 3. 配置 DBeaver

修改 DBeaver 安装目录下的 `dbeaver.ini` 文件，添加以下参数：

```ini
-vmargs
-javaagent:/usr/share/dbeaver/dbeaver-agent.jar
-Xbootclasspath/a: /usr/share/dbeaver/dbeaver-agent.jar
```

请确保这些参数放置在 `-vmargs` 下方。

### 4. 处理 JRE 依赖

如果您使用的是 JRE 21，且该 JRE 可以在系统路径中找到，那么不需要额外操作。如果默认 JRE 版本不为 21，请将安装的 JRE 拷贝到 DBeaver 安装目录，使其与自带的 jre 文件夹同级。非 Windows
系统也可以使用软链接。

### 5. 屏蔽 `stats.dbeaver.com` 域名

为了避免 DBeaver 向 `stats.dbeaver.com` 发送数据，可以通过修改 hosts 文件的方式屏蔽该域名。在 hosts 文件中添加以下内容：

```shell
127.0.0.1 stats.dbeaver.com
```

## 生成许可证密钥

现在，您可以通过命令行生成许可证密钥，运行以下命令：

```shell
java -cp libs/\*:./target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar \
    com.dbeaver.agent.License
```

此命令默认生成针对 `DBeaver Enterprise Edition 25.0` 的密钥。
如果需要生成其他类型的密钥，可以通过以下参数进行指定：

```shell
java -cp libs/\*:./target/dbeaver-agent-25.0-SNAPSHOT-jar-with-dependencies.jar \
    com.dbeaver.agent.License -h

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
                             Product version, default is 25
```

## 初次导入注册码

首次导入注册码时，建议从命令行启动 DBeaver，以便于观察详细的日志信息，并帮助排查问题。

```shell
# 示例命令启动 DBeaver
/path/to/dbeaver/dbeaver
```

如需更多帮助，请寻求社区支持或参考官方文档。