[English](README-EN.md)

# DBeaver Agent for updated DBeaver License Key

<img width="853" alt="v25.2" src="images/v25.3.jpg" />
<img width="853" alt="v25.2" src="images/v25.2.jpg" />
<img width="439" alt="v25.1" src="images/v25.1.jpg" />
<img width="600" alt="license generator ui" src="images/license-ui.jpg" />

## 分支与版本说明

- `master` 分支是当前活跃开发主分支，始终保持最新功能和修复。
- 每个 tag 名称对应 DBeaver 的具体版本，用户可根据 DBeaver 版本选择对应的 tag。
- 历史分支和 tag 可用于参考旧版本实现。

若需要参考 `25.0` 版本，请查看 [v25.0](https://github.com/wgzhao/dbeaver-agent/tree/v25.0) 分支。  
若需参考 `24.x`版本，请查看 [v24.0](https://github.com/wgzhao/dbeaver-agent/tree/v24.0) 分支，  
其他低版本则请参考 [v23.0](https://github.com/wgzhao/dbeaver-agent/tree/v23.0)  分支。

## 当前支持的版本

- `25.3`
- `25.2`
- `25.1`

## 使用说明

### 1. 构建项目

首先，使用 Maven 构建项目，生成包含所有依赖的 jar 文件：

```bash
mvn clean package
```

生成的文件路径为 `target/dbeaver-agent-25.1-jar-with-dependencies.jar`。

### 2. 安装 DBeaver Agent

将生成的 jar 文件移动到 DBeaver 的安装路径下（推荐）：

```shell
cp target/dbeaver-agent-25.3-jar-with-dependencies.jar /usr/share/dbeaver/dbeaver-agent.jar
```

### 3. 配置 DBeaver

修改 DBeaver 安装目录下的 `dbeaver.ini` 文件，添加以下参数：

```ini
-vmargs
-javaagent:/usr/local/share/dbeaver/dbeaver-agent.jar
-Xbootclasspath/a:/usr/local/share/dbeaver/dbeaver-agent.jar
```

请确保这些参数放置在 `-vmargs` 下方。

### 4. 处理 JRE 依赖

如果您使用的是 JRE 23，且该 JRE 可以在系统路径中找到，那么不需要额外操作。如果默认 JRE 版本不为 23，请将安装的 JRE 拷贝到 DBeaver 安装目录，使其与自带的 jre 文件夹同级。非 Windows
系统也可以使用软链接。

### 5. 屏蔽 `stats.dbeaver.com` 域名

为了避免 DBeaver 向 `stats.dbeaver.com` 发送数据，可以通过修改 hosts 文件的方式屏蔽该域名。在 hosts 文件中添加以下内容：

```shell
127.0.0.1 stats.dbeaver.com
```

## 生成许可证密钥

### 命令行界面 (CLI)

现在，您可以通过命令行生成许可证密钥，运行以下命令：

```shell
bash ./gen-license.sh
```

如果是 Windows 用户，则命令可能如下：

```shell
gen-license.bat
```

此命令默认生成针对 `DBeaver Enterprise Edition 25.1` 的密钥。
如果需要生成其他类型的密钥，可以通过以下参数进行指定：

```shell
sh gen-license.sh -h

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

### 图形用户界面 (GUI)

为了更方便的使用，现在提供了跨平台的图形用户界面：

#### 启动 GUI

**在 Windows 上：**
```cmd
start-ui.bat
```

**在 Linux/macOS 上：**
```bash
./start-ui.sh
```

#### GUI 特性

- **平台原生外观**：在不同操作系统上显示相应的原生界面风格
- **简单易用**：下拉菜单选择产品和许可类型，文本框输入版本号
- **即时反馈**：状态栏显示操作进度和结果
- **一键复制**：生成许可证后可直接复制到剪贴板
- **错误处理**：输入验证和友好的错误提示

更多关于 GUI 的详细信息，请参阅 [UI-DOCUMENTATION.md](UI-DOCUMENTATION.md)。

## 初次导入注册码

首次导入注册码时，建议从命令行启动 DBeaver，以便于观察详细的日志信息，并帮助排查问题。

```shell
# 示例命令启动 DBeaver
/path/to/dbeaver/dbeaver
```

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=wgzhao/dbeaver-agent&type=Date)](https://www.star-history.com/#wgzhao/dbeaver-agent&Date)
