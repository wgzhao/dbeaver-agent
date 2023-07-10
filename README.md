# DBeaver Agent

针对老版本的破解研究在 [老版本](/README-63-70.md)

新版本在获取密钥上没什么变化,~~老版本 agent 仍然可用~~ 已发布新版 Agent

但是许可生成上有变化,这里我们讨论 Ultimate 版  
产品 ID 叫做 `dbeaver-ue`  
通过反射操纵 `LMLicense.yearsNumber` 可以实现修改支持年数,最大为 127  
如果不使用到期日期,那么就等于永久许可

剩下的内容看看单元测试 `UltimateLicense` 就知道了

## 依赖

由于单元测试使用了来自 DBeaver 的代码,所以你需要准备好 DBeaver 的一些包  
把下列的包放入到 libs 文件夹

- `com.dbeaver.ee.runtime` 基础运行时,获取密钥等信息在里面
- `com.dbeaver.lm.core` 许可核心
- `org.jkiss.lm` 还是许可核心
- `org.jkiss.utils` 提供一些组件供许可生成
- 对于 DbeaverUltimate 其公钥位于 `com.dbeaver.app.ultimate`
- 对于 Cloudeaver 其公钥位于 `io.cloudbeaver.product.ee`

## 怎么用?

直接 `mvn package assembly:single` 构建就可以了.  
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

> 对于 DBeaver >= 20 需要自备 Java11

## 支持版本

下列是已经测试过的版本:

- 23.0.0