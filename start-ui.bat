@echo off
setlocal

REM DBeaver License Generator GUI Launcher for Windows

REM 定义变量 jar_file
set jar_file=dbeaver-agent-25.1-jar-with-dependencies.jar

REM 获取脚本所在的目录并切换到该目录
cd /d "%~dp0" || exit /b 1

REM 检查目标文件是否存在
if not exist "target\%jar_file%" (
    echo Jar file not found, building...
    REM 调用 Maven 执行构建
    mvn clean package || exit /b 1
)

REM 启动GUI程序
java -cp "libs\*;target\%jar_file%" com.dbeaver.agent.ui.LicenseGeneratorUI %*