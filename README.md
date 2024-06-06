# DBeaver Agent for 24.0

该说明针对 `24.0` 版本，其他低版本可以参考 `master` 分支


## 依赖

由于单元测试使用了来自 DBeaver 的代码,所以你需要准备好 DBeaver 的一些包  
把下列的包放入到 libs 文件夹

- `com.dbeaver.ee.runtime` 基础运行时,获取密钥等信息在里面
- `com.dbeaver.lm.api` 许可核心
- `org.jkiss.utils` 提供一些组件供许可生成
- 对于 DbeaverUltimate 其公钥位于 `com.dbeaver.app.ultimate`
- 对于 Cloudeaver 其公钥位于 `io.cloudbeaver.product.ee`

## 怎么用?

直接 `mvn package` 构建就可以了.  
生成的 `dbeaver-agent.jar` 放到任何你喜欢的地方

> 但还是推荐放到安装目录

修改 DBeaver 安装目录的 `dbeaver.ini` 给他加点参数  
在 `-vmargs` 下面一行加 `-javaagent:{你的jar路径}`  
就像这样

```ini
-startup
plugins/org.eclipse.equinox.launcher_1.6.100.v20201223-0822.jar
--launcher.library
plugins/org.eclipse.equinox.launcher.gtk.linux.x86_64_1.2.100.v20210209-1541
-vmargs
-javaagent:/usr/share/dbeaver/dbeaver-agent.jar
-XX:+IgnoreUnrecognizedVMOptions
--add-modules=ALL-SYSTEM
-Dosgi.requiredJavaVersion=11
-Xms128m
-Xmx2048m
```

然后呢,需要删掉 DBeaver 自带的 jre 就好了

> 对于 DBeaver >= 24 需要自备 Java17

## 支持版本

下列是已经测试过的版本:

- 24.0.0