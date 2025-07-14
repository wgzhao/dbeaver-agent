@echo off
setlocal

:: 定义变量 jar_file
set jar_file=dbeaver-agent-25.1-jar-with-dependencies.jar

:: 获取脚本所在的目录并切换到该目录
cd /d "%~dp0" || exit /b 1

:: 检查目标文件是否存在
if not exist "target\%jar_file%" (
    echo Jar file not found, building...
    :: 调用 Maven 执行构建
    mvn clean package || exit /b 1
)

:: 执行 Java 程序
java -cp "libs\*;target\%jar_file%" com.dbeaver.agent.License %*
