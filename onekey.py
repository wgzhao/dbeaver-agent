#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
DBeaver Agent 自动部署工具

这个脚本提供了一键式的 DBeaver Agent 部署解决方案，自动完成以下任务：
1. 从 DBeaver 安装目录中提取版本信息和产品 ID
2. 查找并复制必需的依赖 jar 文件到项目 libs 目录
3. 自动更新 pom.xml 文件中的版本号和依赖配置
4. 使用 Maven 编译项目，生成包含所有依赖的 jar 文件
5. 将编译产物部署到 DBeaver 的 plugins 目录
6. 自动生成许可证密钥并复制到剪贴板
7. 更新 dbeaver.ini 配置文件（添加 javaagent 和调试参数）
8. 处理 JRE 依赖（重命名 jre 目录，强制使用系统 JDK）
9. macOS 特殊处理：删除 -vm 参数，确保使用系统 JDK
10. 启动 DBeaver 应用程序

支持的平台：Windows、macOS、Linux
支持的版本：DBeaver 25.1、25.2

使用方法：
    python onekey.py [DBeaver安装路径]

示例：
    python onekey.py "C:\\Program Files\\DBeaver"                   # Windows
    python onekey.py "/Applications/DBeaver.app"                    # macOS
    python onekey.py "/usr/share/dbeaver"                           # Linux
    python onekey.py                                                # 交互式输入路径

"""

import os
import sys
import time
import re
import shutil
import subprocess
import platform
from pathlib import Path


class ProgressTracker:
    """
    进度跟踪器类

    用于显示多步骤操作的进度信息，帮助用户了解当前执行状态。
    """

    def __init__(self, total_steps):
        """
        初始化进度跟踪器

        Args:
            total_steps (int): 总步骤数
        """
        self.total_steps = total_steps
        self.current_step = 0

    def next_step(self, description):
        """
        进入下一步并显示进度信息

        Args:
            description (str): 当前步骤的描述信息
        """
        self.current_step += 1
        print(f"\n[{self.current_step}/{self.total_steps}] {description}")

    def reset(self):
        """重置进度计数器到初始状态"""
        self.current_step = 0


# 全局进度跟踪器实例（将在 main 函数中初始化）
progress = None

# 全局变量：许可证密钥和剪贴板工具（用于在完成提示中显示）
_license_key = None
_clipboard_tool = None


def get_real_user():
    """
    获取真实用户信息（当使用 sudo 运行时）

    Returns:
        tuple: (username, uid, gid, home_dir) 或 (None, None, None, None)

    Note:
        - 如果脚本通过 sudo 运行，返回 SUDO_USER 的信息
        - 否则返回当前用户信息
        - Windows 和 macOS 返回 None
    """
    system = platform.system()

    if system != 'Linux':
        return None, None, None, None

    # 检查是否通过 sudo 运行
    sudo_user = os.environ.get('SUDO_USER')
    sudo_uid = os.environ.get('SUDO_UID')
    sudo_gid = os.environ.get('SUDO_GID')

    if sudo_user and sudo_uid and sudo_gid:
        # 获取真实用户的 HOME 目录
        import pwd
        try:
            pw_record = pwd.getpwnam(sudo_user)
            return sudo_user, int(sudo_uid), int(sudo_gid), pw_record.pw_dir
        except KeyError:
            return sudo_user, int(sudo_uid), int(sudo_gid), f"/home/{sudo_user}"

    return None, None, None, None


def check_maven_available():
    """
    检查 Maven 是否在系统中可用

    该函数会尝试运行不同的 Maven 命令（根据操作系统），以检测 Maven 是否已安装
    并可在系统 PATH 中访问。Windows 系统会尝试 mvn.cmd、mvn.bat 和 mvn，
    而 Unix 系统（macOS/Linux）只尝试 mvn。

    Returns:
        str: 可用的 Maven 命令字符串（如 'mvn'、'mvn.cmd'），如果不可用则返回 None

    Note:
        - Windows 系统需要使用 shell=True 来执行命令
        - 超时设置为 5 秒以避免长时间等待
        - 返回第一个可用的 Maven 命令
    """
    system = platform.system()

    # 根据操作系统选择要测试的 Maven 命令变体
    if system == 'Windows':
        maven_commands = ['mvn.cmd', 'mvn.bat', 'mvn']
    else:  # macOS 和 Linux
        maven_commands = ['mvn']

    for cmd in maven_commands:
        try:
            # Unix 系统上不使用 shell=True 以提高安全性
            use_shell = (system == 'Windows')

            result = subprocess.run(
                [cmd, '-version'],
                capture_output=True,
                text=True,
                shell=use_shell,
                timeout=5,
                env=os.environ.copy()
            )
            if result.returncode == 0:
                print(f"  检测到 Maven: {cmd}")
                # 打印 Maven 版本信息的第一行
                first_line = result.stdout.split('\n')[0] if result.stdout else ''
                if first_line:
                    print(f"  版本: {first_line}")
                return cmd
        except FileNotFoundError:
            # 命令不存在，继续尝试下一个
            continue
        except subprocess.TimeoutExpired:
            print(f"  警告: {cmd} 命令超时")
            continue
        except Exception as e:
            # 捕获其他异常，继续尝试下一个命令
            continue

    return None


def find_dbeaver_dir(input_path):
    """
    智能识别并确定 DBeaver 的安装目录

    该函数能够处理多种输入格式：
    - 直接的安装目录路径
    - 可执行文件路径（dbeaver.exe、dbeaver）
    - macOS 的 .app 包路径
    - .app 包内部的文件路径

    Args:
        input_path (str): 用户输入的路径（可以是目录、可执行文件或 .app 包）

    Returns:
        Path: DBeaver 安装目录的 Path 对象

    Raises:
        FileNotFoundError: 路径不存在或找不到必需的可执行文件
        ValueError: 提供的路径无效或不是 DBeaver 安装目录

    Note:
        - Windows: 查找 dbeaver.exe
        - macOS: 查找 .app 包（DBeaver.app 或 DBeaverUltimate.app）
        - Linux: 查找 dbeaver 可执行文件
    """
    path = Path(input_path.strip().strip('"\''))

    if not path.exists():
        raise FileNotFoundError(f"路径不存在: {path}")

    system = platform.system()

    # macOS 特殊处理：.app 包是一个特殊的目录结构
    if system == 'Darwin':
        # 如果直接指向 .app 目录
        if path.suffix == '.app' and path.is_dir():
            return path
        # 如果指向 .app 内部的文件，向上查找 .app 目录
        if '.app/' in str(path) or '.app\\' in str(path):
            current = path
            while current.parent != current:
                if current.suffix == '.app':
                    return current
                current = current.parent
        # 检查是否在目录中存在 DBeaver*.app
        if path.is_dir():
            app_files = list(path.glob('DBeaver*.app'))
            if app_files:
                return app_files[0]
        raise ValueError(f"未找到 DBeaver.app: {path}")

    # Windows 和 Linux 的处理逻辑
    if path.is_file():
        # 如果提供的是可执行文件路径，返回其父目录
        if system == 'Windows':
            if path.name.lower() == 'dbeaver.exe':
                return path.parent
            else:
                raise ValueError(f"不是 dbeaver.exe 文件: {path}")
        else:  # Linux
            if 'dbeaver' in path.name.lower():
                return path.parent
            else:
                raise ValueError(f"不是 dbeaver 可执行文件: {path}")

    # 如果提供的是目录，验证目录中是否包含可执行文件
    if path.is_dir():
        if system == 'Windows':
            dbeaver_exe = path / 'dbeaver.exe'
            if dbeaver_exe.exists():
                return path
            else:
                raise FileNotFoundError(f"目录中未找到 dbeaver.exe: {path}")
        else:  # Linux
            dbeaver_bin = path / 'dbeaver'
            if dbeaver_bin.exists():
                return path
            else:
                raise FileNotFoundError(f"目录中未找到 dbeaver 可执行文件: {path}")

    raise ValueError(f"无效的路径: {path}")


def read_version_from_eclipseproduct(dbeaver_dir):
    """
    从 .eclipseproduct 文件中读取版本号和产品 ID

    .eclipseproduct 是 Eclipse 应用程序的标准配置文件，包含产品的基本信息。
    DBeaver 基于 Eclipse 平台构建，因此也使用这个文件来存储版本信息。

    文件格式示例：
        name=DBeaver Ultimate
        id=com.dbeaver.ultimate
        version=25.2.0

    Args:
        dbeaver_dir (Path): DBeaver 安装目录

    Returns:
        tuple: (版本号字符串, 产品ID字符串)
               例如 ("25.2.0", "com.dbeaver.ultimate")

    Raises:
        FileNotFoundError: .eclipseproduct 文件不存在
        ValueError: 文件中未找到版本号或产品ID

    Note:
        - macOS: 文件位于 DBeaver.app/Contents/Eclipse/.eclipseproduct
        - Windows/Linux: 文件位于安装目录根目录下
    """
    system = platform.system()

    # 根据操作系统确定 .eclipseproduct 文件位置
    if system == 'Darwin':  # macOS
        # macOS 的 .app 包内部结构
        eclipseproduct_file = dbeaver_dir / 'Contents' / 'Eclipse' / '.eclipseproduct'
    else:  # Windows 和 Linux
        eclipseproduct_file = dbeaver_dir / '.eclipseproduct'

    if not eclipseproduct_file.exists():
        raise FileNotFoundError(f".eclipseproduct 文件不存在: {eclipseproduct_file}")

    with open(eclipseproduct_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # 使用正则表达式查找 version=25.2.0 格式的版本号
    version_match = re.search(r'version\s*=\s*([0-9]+\.[0-9]+\.[0-9]+)', content)
    if not version_match:
        raise ValueError(f".eclipseproduct 文件中未找到版本号")

    # 使用正则表达式查找 id=com.dbeaver.ultimate 格式的产品ID
    id_match = re.search(r'id\s*=\s*([^\s]+)', content)
    if not id_match:
        raise ValueError(f".eclipseproduct 文件中未找到产品ID")

    return version_match.group(1), id_match.group(1)


def find_and_copy_jars(dbeaver_dir, libs_dir):
    """
    从 DBeaver plugins 目录中查找并复制必需的依赖 jar 文件

    该函数会搜索以下必需的依赖库：
    1. com.dbeaver.lm.api_*.jar - 许可证管理 API，用于生成和验证许可证
    2. org.jkiss.utils_*.jar - 工具类库，提供加密和其他实用功能

    文件名格式示例：
        com.dbeaver.lm.api_3.0.9.202506090822.jar
        org.jkiss.utils_3.1.0.202506090822.jar

    Args:
        dbeaver_dir (Path): DBeaver 安装目录
        libs_dir (Path): 目标 libs 目录（项目中的依赖目录）

    Returns:
        list: jar 信息的字典列表，每个字典包含：
              - artifactId: Maven artifact ID（'api' 或 'utils'）
              - version: 主版本号（如 '3.0.9'）
              - filename: 完整的 jar 文件名

    Raises:
        FileNotFoundError: plugins 目录不存在

    Note:
        - 版本号会自动提取：3.0.9.202506090822 -> 3.0.9
        - 如果目标文件已存在，会先删除再复制
        - 未找到的依赖会显示警告但不会中断执行
    """
    # 根据操作系统确定 plugins 目录位置
    system = platform.system()

    if system == 'Darwin':  # macOS
        # macOS 的 plugins 目录在 .app 包内
        plugins_dir = dbeaver_dir / 'Contents' / 'Eclipse' / 'plugins'
    else:  # Windows 和 Linux
        plugins_dir = dbeaver_dir / 'plugins'

    if not plugins_dir.exists():
        raise FileNotFoundError(f"plugins 目录不存在: {plugins_dir}")

    # 定义需要查找的 jar 文件模式
    # 格式：(正则表达式模式, Maven artifactId)
    patterns = [
        (r'com\.dbeaver\.lm\.api_(.+?)\.jar', 'api'),      # 许可证管理 API
        (r'org\.jkiss\.utils_(.+?)\.jar', 'utils'),        # 工具类库
    ]

    jar_info_list = []

    for pattern, artifact_id in patterns:
        found = False
        for jar_file in plugins_dir.glob('*.jar'):
            match = re.match(pattern, jar_file.name)
            if match:
                # 提取完整版本号，例如 3.0.9.202506090822
                full_version = match.group(1)
                # 提取主版本号（前三位），例如 3.0.9
                version_match = re.match(r'(\d+\.\d+\.\d+)', full_version)
                if version_match:
                    version = version_match.group(1)
                else:
                    version = full_version

                # 确保 libs 目录存在
                libs_dir.mkdir(parents=True, exist_ok=True)
                target_file = libs_dir / jar_file.name

                # 如果目标文件已存在，先删除
                if target_file.exists():
                    print(f"  删除旧文件: {target_file.name}")
                    target_file.unlink()

                # 复制 jar 文件到 libs 目录
                print(f"  复制: {jar_file.name} -> libs/")
                shutil.copy2(jar_file, target_file)

                jar_info_list.append({
                    'artifactId': artifact_id,
                    'version': version,
                    'filename': jar_file.name
                })

                found = True
                break

        if not found:
            print(f"  警告: 未找到匹配的 jar 文件: {pattern}")

    return jar_info_list


def update_pom_xml(pom_file, main_version, jar_info_list):
    """
    更新 pom.xml 文件中的版本号和依赖配置

    该函数使用文本替换的方式（而非 XML 解析）来更新 pom.xml，
    这样可以保持文件的原有格式、注释和缩进。

    更新内容包括：
    1. 主版本号：<artifactId>dbeaver-agent</artifactId> 后的 <version> 标签
    2. 依赖版本：每个依赖的 <version> 标签
    3. 依赖路径：每个依赖的 <systemPath> 标签中的文件名

    Args:
        pom_file (Path): pom.xml 文件路径
        main_version (str): 主版本号（从 .eclipseproduct 读取，如 '25.2.0'）
        jar_info_list (list): jar 文件信息列表，包含 artifactId、version 和 filename

    Note:
        - 使用正则表达式进行精确匹配和替换
        - 保持原有的 XML 格式和缩进
        - 会打印每一项更新的详细信息
    """
    # 读取原始文件内容
    with open(pom_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. 更新主版本号（dbeaver-agent 的 <version> 标签）
    # 匹配：<artifactId>dbeaver-agent</artifactId> 后面的 <version>...</version>
    version_pattern = r'(<artifactId>dbeaver-agent</artifactId>\s*\n\s*<version>)([^<]+)(</version>)'
    match = re.search(version_pattern, content)
    if match:
        old_version = match.group(2)
        content = re.sub(version_pattern, r'\g<1>' + main_version + r'\g<3>', content, count=1)
        print(f"\n更新主版本号: {old_version} -> {main_version}")

    # 2. 更新每个依赖的版本号和文件路径
    for jar_info in jar_info_list:
        artifact_id = jar_info['artifactId']
        new_version = jar_info['version']
        new_filename = jar_info['filename']

        # 构建依赖块的正则表达式
        # 匹配整个 <dependency> 块，包含指定的 artifactId
        dependency_pattern = (
            r'(<dependency>\s*\n'
            r'\s*<groupId>[^<]+</groupId>\s*\n'
            r'\s*<artifactId>' + re.escape(artifact_id) + r'</artifactId>\s*\n'
            r'\s*<version>)([^<]+)(</version>\s*\n'
            r'\s*<scope>system</scope>\s*\n'
            r'\s*<systemPath>\$\{project\.basedir\}/libs/)([^<]+)(</systemPath>\s*\n'
            r'\s*</dependency>)'
        )

        match = re.search(dependency_pattern, content)
        if match:
            old_version = match.group(2)
            old_filename = match.group(4)

            # 同时替换版本号和文件路径
            content = re.sub(
                dependency_pattern,
                r'\g<1>' + new_version + r'\g<3>' + new_filename + r'\g<5>',
                content
            )

            print(f"更新 {artifact_id} 版本: {old_version} -> {new_version}")
            print(f"更新 {artifact_id} 路径: {old_filename} -> {new_filename}")

    # 写回文件，保持原有格式
    with open(pom_file, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"\npom.xml 更新完成！")


def compile_project(script_dir):
    """
    使用 Maven 编译项目并生成包含所有依赖的 jar 文件

    该函数执行 Maven 的 clean package 命令，跳过测试以加快编译速度。
    编译成功后会自动查找生成的 jar 文件（*-jar-with-dependencies.jar）。

    如果 Maven 不可用，会根据操作系统显示详细的安装指导。

    Args:
        script_dir (Path): 项目根目录（包含 pom.xml 的目录）

    Returns:
        Path: 编译后的 jar 文件路径

    Raises:
        RuntimeError: Maven 未安装或编译失败
        FileNotFoundError: 编译成功但未找到输出的 jar 文件

    Note:
        - 执行命令：mvn clean package -DskipTests
        - Windows 需要使用 shell=True
        - 编译输出会被捕获并在失败时显示
        - 超时时间为默认值（无限制）
    """
    print("\n" + "=" * 60)
    print("开始编译项目...")
    print("=" * 60)

    # 检查 Maven 是否可用
    maven_cmd = check_maven_available()

    if not maven_cmd:
        print("✗ 未找到 Maven 命令")
        system = platform.system()
        print("\n请确保 Maven 已正确安装并配置：")
        print("  1. 下载 Maven: https://maven.apache.org/download.cgi")

        # 根据操作系统提供不同的安装指导
        if system == 'Darwin':  # macOS
            print("  2. macOS 推荐使用 Homebrew 安装:")
            print("     brew install maven")
            print("  3. 或手动安装后添加到 PATH:")
            print("     export PATH=/path/to/maven/bin:$PATH")
            print("  4. 验证安装: mvn -version")
        elif system == 'Linux':
            print("  2. Linux 使用包管理器安装:")
            print("     Ubuntu/Debian: sudo apt-get install maven")
            print("     CentOS/RHEL: sudo yum install maven")
            print("     Fedora: sudo dnf install maven")
            print("  3. 或手动安装后添加到 PATH:")
            print("     export PATH=/path/to/maven/bin:$PATH")
            print("  4. 验证安装: mvn -version")
        else:  # Windows
            print("  2. 解压到某个目录，例如: C:\\Program Files\\Apache\\maven")
            print("  3. 添加环境变量:")
            print("     - MAVEN_HOME = C:\\Program Files\\Apache\\maven")
            print("     - 在 PATH 中添加: %MAVEN_HOME%\\bin")
            print("  4. 打开新的命令行窗口，执行: mvn -version")

        print("  5. 重新运行此脚本")
        raise RuntimeError("未找到 Maven 命令")

    print(f"✓ 使用 Maven 命令: {maven_cmd}")

    # 执行 mvn clean package -DskipTests
    try:
        system = platform.system()
        use_shell = (system == 'Windows')

        result = subprocess.run(
            [maven_cmd, 'clean', 'package', '-DskipTests'],
            cwd=script_dir,
            capture_output=True,
            text=True,
            encoding='utf-8',
            shell=use_shell,
            env=os.environ.copy()
        )

        if result.returncode == 0:
            print("✓ 编译成功！")
            # 查找编译产物（包含所有依赖的 jar 文件）
            target_dir = script_dir / 'target'
            jar_pattern = '*-jar-with-dependencies.jar'
            jar_files = list(target_dir.glob(jar_pattern))

            if jar_files:
                jar_file = jar_files[0]
                print(f"✓ 找到产物: {jar_file.name}")
                return jar_file
            else:
                raise FileNotFoundError(f"未找到编译产物: {jar_pattern}")
        else:
            print("✗ 编译失败！")
            print("\n=== Maven 输出 ===")
            print(result.stdout)
            if result.stderr:
                print("\n=== 错误信息 ===")
                print(result.stderr)
            raise RuntimeError("Maven 编译失败")

    except Exception as e:
        print(f"✗ 编译过程出错: {e}")
        raise


def deploy_agent_to_dbeaver(jar_file, dbeaver_dir):
    """
    将编译好的 agent jar 文件部署到 DBeaver 的 plugins 目录

    该函数会将编译产物复制到 DBeaver 的 plugins 目录，并重命名为
    dbeaver-agent.jar。如果目标文件已存在，会先删除再复制。

    Args:
        jar_file (Path): 编译产物 jar 文件路径
        dbeaver_dir (Path): DBeaver 安装目录

    Returns:
        Path: 部署后的 jar 文件路径（在 plugins 目录中）

    Raises:
        FileNotFoundError: plugins 目录不存在

    Note:
        - 目标文件名固定为 dbeaver-agent.jar
        - macOS: plugins 目录位于 DBeaver.app/Contents/Eclipse/plugins
        - Windows/Linux: plugins 目录位于安装目录下
    """
    print("\n" + "=" * 60)
    print("部署 agent 到 DBeaver...")
    print("=" * 60)

    # 根据操作系统确定 plugins 目录位置
    system = platform.system()

    if system == 'Darwin':  # macOS
        plugins_dir = dbeaver_dir / 'Contents' / 'Eclipse' / 'plugins'
    else:  # Windows 和 Linux
        plugins_dir = dbeaver_dir / 'plugins'

    if not plugins_dir.exists():
        raise FileNotFoundError(f"plugins 目录不存在: {plugins_dir}")

    target_jar = plugins_dir / 'dbeaver-agent.jar'

    # 如果目标文件已存在，先删除
    if target_jar.exists():
        print(f"  删除旧文件: {target_jar}")
        target_jar.unlink()

    # 复制新文件
    print(f"  复制: {jar_file.name} -> {target_jar}")
    shutil.copy2(jar_file, target_jar)

    print(f"✓ 部署完成: {target_jar}")
    return target_jar


def update_dbeaver_ini(dbeaver_dir):
    """
    更新 dbeaver.ini 配置文件，添加 javaagent 和调试参数

    该函数会自动：
    1. 在 -vmargs 后添加 javaagent 参数（如果不存在）
    2. 添加调试模式参数 -Dlm.debug.mode=true（如果不存在）
    3. macOS 特殊处理：删除 -vm 参数及其路径，强制使用系统 JDK

    dbeaver.ini 是 Eclipse 应用程序的启动配置文件，类似于 JVM 参数配置。

    Args:
        dbeaver_dir (Path): DBeaver 安装目录

    Raises:
        FileNotFoundError: dbeaver.ini 文件不存在

    Note:
        - macOS: ini 文件位于 DBeaver.app/Contents/Eclipse/dbeaver.ini
        - Windows/Linux: ini 文件位于安装目录根目录下
        - javaagent 路径：
            * macOS: ../Eclipse/plugins/dbeaver-agent.jar（相对于 ini 文件位置）
            * Windows/Linux: plugins/dbeaver-agent.jar
        - macOS 会自动删除 -vm 参数以使用系统 JDK
    """
    print("\n" + "=" * 60)
    print("更新 dbeaver.ini...")
    print("=" * 60)

    system = platform.system()

    # 根据操作系统确定 ini 文件位置和 javaagent 路径
    if system == 'Darwin':  # macOS
        ini_file = dbeaver_dir / 'Contents' / 'Eclipse' / 'dbeaver.ini'
        javaagent_path = '../Eclipse/plugins/dbeaver-agent.jar'
    else:  # Windows 和 Linux
        ini_file = dbeaver_dir / 'dbeaver.ini'
        javaagent_path = 'plugins/dbeaver-agent.jar'

    if not ini_file.exists():
        raise FileNotFoundError(f"dbeaver.ini 文件不存在: {ini_file}")

    # 读取现有内容
    with open(ini_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    # macOS 特殊处理：删除 -vm 参数，强制使用系统 JDK
    # -vm 参数格式：
    #   -vm
    #   ../Eclipse/jre/Contents/Home/bin/java
    if system == 'Darwin':
        new_lines = []
        skip_next = False  # 标记是否跳过下一行（-vm 的路径行）
        removed_vm = False

        for i, line in enumerate(lines):
            if skip_next:
                # 跳过 -vm 后面的 JRE 路径行
                skip_next = False
                print(f"  ✓ 删除: {line.strip()}")
                removed_vm = True
                continue

            if line.strip() == '-vm':
                # 找到 -vm 参数，标记跳过下一行
                print(f"  ✓ 删除: -vm")
                skip_next = True
                removed_vm = True
                continue

            new_lines.append(line)

        lines = new_lines
        if removed_vm:
            print(f"  提示: 已删除 -vm 配置，DBeaver 将使用系统 JDK")

    # 准备要添加的配置行
    javaagent_line = f'-javaagent:{javaagent_path}\n'
    debug_line = '-Dlm.debug.mode=true\n'

    # 检查配置是否已存在
    has_javaagent = any(javaagent_path in line for line in lines)
    has_debug = any(line.strip() == debug_line.strip() for line in lines)

    # 查找 -vmargs 的位置（所有 JVM 参数都应该在 -vmargs 之后）
    vmargs_index = -1
    for i, line in enumerate(lines):
        if line.strip() == '-vmargs':
            vmargs_index = i
            break

    if vmargs_index == -1:
        # 如果没有 -vmargs 行，在文件末尾添加
        print("  警告: 未找到 -vmargs 行，将在文件末尾添加")
        lines.append('-vmargs\n')
        vmargs_index = len(lines) - 1

    # 在 -vmargs 后添加配置（如果不存在）
    insert_index = vmargs_index + 1
    modified = False

    if not has_javaagent:
        lines.insert(insert_index, javaagent_line)
        print(f"  ✓ 添加: {javaagent_line.strip()}")
        insert_index += 1
        modified = True
    else:
        print(f"  - 已存在: javaagent 配置")

    if not has_debug:
        lines.insert(insert_index, debug_line)
        print(f"  ✓ 添加: {debug_line.strip()}")
        modified = True
    else:
        print(f"  - 已存在: {debug_line.strip()}")

    # 写回文件（macOS 删除 -vm 也算修改）
    if modified or (system == 'Darwin' and removed_vm):
        with open(ini_file, 'w', encoding='utf-8') as f:
            f.writelines(lines)
        print(f"✓ dbeaver.ini 更新完成")
    else:
        print(f"✓ dbeaver.ini 无需更新")


def rename_jre_directory(dbeaver_dir):
    """
    重命名 jre 目录为 jr，强制 DBeaver 使用系统 JDK

    DBeaver 默认使用内置的 JRE（位于 jre 目录）。通过重命名该目录，
    可以强制 DBeaver 使用系统安装的 JDK，这样可以：
    1. 使用更新的 Java 版本
    2. 确保与 agent 的兼容性
    3. 避免内置 JRE 的限制

    Args:
        dbeaver_dir (Path): DBeaver 安装目录

    Note:
        - 重命名：jre -> jr（任何不是 "jre" 的名字都可以）
        - 如果 jr 目录已存在，跳过重命名
        - 如果 jre 目录不存在，说明可能已经使用系统 JDK
        - macOS: jre 位于 DBeaver.app/Contents/Eclipse/jre
        - Windows/Linux: jre 位于安装目录下
    """
    print("\n" + "=" * 60)
    print("处理 JRE 目录...")
    print("=" * 60)

    system = platform.system()

    # 根据操作系统确定 jre 目录位置
    if system == 'Darwin':  # macOS
        jre_dir = dbeaver_dir / 'Contents' / 'Eclipse' / 'jre'
        jr_dir = dbeaver_dir / 'Contents' / 'Eclipse' / 'jr'
    else:  # Windows 和 Linux
        jre_dir = dbeaver_dir / 'jre'
        jr_dir = dbeaver_dir / 'jr'

    # 检查并处理 jre 目录
    if jre_dir.exists():
        try:
            if jr_dir.exists():
                # jr 目录已存在，说明之前已经重命名过
                print(f"  - jr 目录已存在，跳过重命名")
                print(f"  提示: DBeaver 将使用系统 JDK")
            else:
                # 重命名 jre -> jr
                jre_dir.rename(jr_dir)
                print(f"  ✓ 已重命名: jre -> jr")
                print(f"  提示: DBeaver 将使用系统 JDK 而不是内置 JRE")
        except Exception as e:
            print(f"  ✗ 重命名失败: {e}")
            print(f"  提示: 将使用 DBeaver 内置的 JRE")
    else:
        # jre 目录不存在
        print(f"  - jre 目录不存在")
        if jr_dir.exists():
            print(f"  提示: DBeaver 将使用系统 JDK")
        else:
            print(f"  提示: DBeaver 将自动选择 Java 运行时")


def generate_license(dbeaver_dir, product_id, product_version):
    """
    自动生成 DBeaver 许可证密钥并复制到剪贴板

    该函数会：
    1. 根据产品 ID 确定许可证类型（UE/EE/LE）
    2. 在 plugins 目录中调用 License 类生成许可证
    3. 自动复制许可证到系统剪贴板

    支持的产品类型：
    - com.dbeaver.ultimate -> ue (Ultimate Edition)
    - com.dbeaver.enterprise -> ee (Enterprise Edition)
    - com.dbeaver.lite -> le (Lite Edition)

    Args:
        dbeaver_dir (Path): DBeaver 安装目录
        product_id (str): 产品 ID，如 'com.dbeaver.ultimate'
        product_version (str): 产品版本，如 '25.2.0'

    Note:
        - 需要在 plugins 目录中执行，因为需要访问所有依赖 jar
        - 使用 'java -cp * com.dbeaver.agent.License' 命令
        - 只传递主版本号（如 25.2.0 -> 25）
        - 超时时间设置为 10 秒
        - 如果产品 ID 不支持，会显示警告但不会中断流程
    """
    print("\n" + "=" * 60)
    print("生成许可证...")
    print("=" * 60)

    # 产品 ID 到许可证类型的映射
    license_type_map = {
        'com.dbeaver.ultimate': 'ue',      # Ultimate Edition
        'com.dbeaver.enterprise': 'ee',    # Enterprise Edition
        'com.dbeaver.lite': 'le',          # Lite Edition
    }

    # 获取对应的许可证类型
    license_type = license_type_map.get(product_id)
    if not license_type:
        print(f"  ✗ 未知的产品ID: {product_id}")
        print(f"  支持的产品: {', '.join(license_type_map.keys())}")
        return

    print(f"  产品ID: {product_id}")
    print(f"  许可类型: {license_type}")
    print(f"  产品版本: {product_version}")

    # 确定 plugins 目录位置
    system = platform.system()
    if system == 'Darwin':  # macOS
        plugins_dir = dbeaver_dir / 'Contents' / 'Eclipse' / 'plugins'
    else:  # Windows 和 Linux
        plugins_dir = dbeaver_dir / 'plugins'

    # 验证 agent jar 是否已部署
    agent_jar = plugins_dir / 'dbeaver-agent.jar'
    if not agent_jar.exists():
        print(f"  ✗ 未找到 agent jar: {agent_jar}")
        print(f"  请先部署 agent")
        return

    # 提取主版本号（如 25.2.0 -> 25）
    major_version = product_version.split('.')[0]

    try:
        # 在 plugins 目录中调用 License 生成许可证
        # 使用通配符 "*" 引用所有 jar，避免 Windows 路径过长问题
        result = subprocess.run(
            ['java', '-cp', '*', 'com.dbeaver.agent.License',
             '-t', license_type,      # 许可类型
             '-v', major_version],    # 主版本号
            capture_output=True,
            text=True,
            timeout=10,
            cwd=str(plugins_dir),     # 在 plugins 目录中执行
        )

        if result.returncode == 0:
            # 解析输出，提取许可证密钥
            output_lines = result.stdout.strip().split('\n')
            license_key = None

            for i, line in enumerate(output_lines):
                if 'LICENSE' in line and i + 1 < len(output_lines):
                    # LICENSE 标记的下一行就是许可证密钥
                    license_key = output_lines[i + 1].strip()
                    break

            if license_key:
                print(f"  ✓ 许可证生成成功！")

                # 复制到剪贴板（根据操作系统使用不同的命令）
                clipboard_tool = None
                try:
                    if system == 'Darwin':  # macOS
                        subprocess.run(['pbcopy'], input=license_key.encode(), check=True)
                        clipboard_tool = 'pbcopy'
                    elif system == 'Windows':
                        subprocess.run(['clip'], input=license_key.encode(), check=True, shell=True)
                        clipboard_tool = 'clip'
                    # Linux: 不使用剪贴板工具，让用户手动复制
                    # 因为 sudo 环境下的剪贴板工具存在环境变量问题

                    # 将许可证密钥和剪贴板状态存储到全局变量
                    global _license_key, _clipboard_tool
                    _license_key = license_key
                    _clipboard_tool = clipboard_tool

                except Exception as e:
                    # 复制失败，但仍保存密钥以便后续显示
                    _license_key = license_key
                    _clipboard_tool = None
            else:
                print(f"  ✗ 未能解析许可证")
                print(f"  输出: {result.stdout}")
        else:
            print(f"  ✗ 生成失败")
            print(f"  错误: {result.stderr}")

    except subprocess.TimeoutExpired:
        print(f"  ✗ 生成许可证超时")
    except Exception as e:
        print(f"  ✗ 生成许可证失败: {e}")






def start_dbeaver(dbeaver_dir):
    """
    启动 DBeaver 应用程序。

    根据操作系统类型，使用相应的启动方式：
    - Windows: 优先使用 dbeaver-cli.exe（显示详细日志），回退到 dbeaver.exe
    - macOS: 使用 `open` 命令启动应用程序包，并返回调试日志路径
    - Linux: 直接执行 dbeaver 二进制文件

    Args:
        dbeaver_dir (Path): DBeaver 安装目录的路径对象

    Returns:
        Path | None: macOS 平台返回调试日志路径，便于后续实时跟踪日志；
                    其他平台返回 None

    Raises:
        FileNotFoundError: 找不到 DBeaver 可执行文件

    Note:
        - Windows: 会优先尝试启动 dbeaver-cli.exe，该版本可在新命令行窗口
          显示详细日志，便于调试 javaagent 加载情况。启动前会自动创建
          dbeaver-cli.ini 配置文件（从 dbeaver.ini 复制）
        - macOS: 启动后会等待 2 秒，以便日志文件生成，然后返回日志路径
          供 stream_macos_debug_log 函数使用
        - Linux: 在 DBeaver 安装目录下启动二进制文件
        - 所有平台都会设置正确的工作目录，确保相对路径配置正常工作
    """
    print("\n" + "=" * 60)
    print("启动 DBeaver...")
    print("=" * 60)

    system = platform.system()

    if system == 'Windows':
        # Windows 平台：准备 CLI 配置文件
        ini_file = dbeaver_dir / 'dbeaver.ini'
        cli_ini_file = dbeaver_dir / 'dbeaver-cli.ini'

        # 如果 dbeaver.ini 存在但 dbeaver-cli.ini 不存在，则复制
        if ini_file.exists() and not cli_ini_file.exists():
            try:
                shutil.copy2(ini_file, cli_ini_file)
                print(f"  ✓ 已创建 CLI 配置: {cli_ini_file.name}")
            except Exception as e:
                print(f"  警告: 无法创建 CLI 配置文件: {e}")

        # 优先使用 CLI 版本
        cli_exe = dbeaver_dir / 'dbeaver-cli.exe'
        exe = dbeaver_dir / 'dbeaver.exe'

        if cli_exe.exists():
            print(f"  ✓ 使用 CLI 版本启动（可查看详细日志）")
            # 使用 CREATE_NEW_CONSOLE 标志在新的命令行窗口中启动
            create_flags = getattr(subprocess, 'CREATE_NEW_CONSOLE', 0)
            subprocess.Popen(
                [str(cli_exe)],
                cwd=str(dbeaver_dir),
                shell=False,
                creationflags=create_flags,
            )
        elif exe.exists():
            print(f"  ✓ 已启动 DBeaver")
            print(f"  提示: 若需查看详细日志，可使用 dbeaver-cli.exe")
            subprocess.Popen([str(exe)], cwd=str(dbeaver_dir), shell=False)
        else:
            raise FileNotFoundError(f"找不到 DBeaver 可执行文件: {exe}")

        return None

    elif system == 'Darwin':  # macOS
        print(f"  ✓ 已启动 DBeaver")
        subprocess.Popen(['open', str(dbeaver_dir)])

        # 等待应用启动，确保日志文件生成
        time.sleep(2)

        log_path = Path.home() / 'Library' / 'DBeaverData' / 'workspace6' / '.metadata' / 'dbeaver-debug.log'
        if log_path.exists():
            print(f"  提示: 日志文件已就绪，将在完成提示后输出日志")
        else:
            print(f"  提示: 日志文件尚未生成，稍后将尝试跟踪")
            print(f"        路径: {log_path}")

        return log_path

    elif system == 'Linux':
        dbeaver_bin = dbeaver_dir / 'dbeaver'
        if not dbeaver_bin.exists():
            raise FileNotFoundError(f"找不到 DBeaver 可执行文件: {dbeaver_bin}")

        print(f"  ✓ 已启动 DBeaver")

        # 获取真实用户信息
        real_user, real_uid, real_gid, real_home = get_real_user()

        if real_user:
            # 以真实用户身份启动 DBeaver
            env = os.environ.copy()
            env['HOME'] = real_home

            # 设置 Wayland 相关环境变量
            xdg_runtime_dir = f"/run/user/{real_uid}"
            env['XDG_RUNTIME_DIR'] = xdg_runtime_dir

            # 设置 WAYLAND_DISPLAY
            if 'WAYLAND_DISPLAY' not in env:
                env['WAYLAND_DISPLAY'] = 'wayland-0'

            # 保留 DISPLAY (X11)
            if 'DISPLAY' not in env:
                env['DISPLAY'] = ':0'

            # 使用 sudo -u 降权启动
            subprocess.Popen(
                ['sudo', '-u', real_user, str(dbeaver_bin)],
                cwd=str(dbeaver_dir),
                env=env,
                start_new_session=True  # 创建新会话，避免继承 sudo 权限
            )
            print(f"  提示: 以用户 {real_user} 身份启动")
        else:
            # 直接以当前用户启动
            subprocess.Popen(
                [str(dbeaver_bin)],
                cwd=str(dbeaver_dir),
                start_new_session=True
            )

        return None

    else:
        # 未知操作系统
        raise RuntimeError(f"不支持的操作系统: {system}")


def stream_macos_debug_log(log_path):
    """
    持续输出 macOS 平台的 DBeaver 调试日志。

    该函数使用 tail -f 命令实时跟踪日志文件，显示 javaagent 加载、
    许可证验证等调试信息。用户可以按 Ctrl+C 中断日志输出。

    Args:
        log_path (Path | None): 调试日志文件的路径对象

    Note:
        - 如果日志文件不存在，会显示手动执行命令的提示
        - 如果 tail 命令不可用（罕见情况），也会给出替代方案
        - 日志输出会持续到用户按 Ctrl+C 或关闭终端
    """
    if not log_path:
        print("\n⚠ macOS 日志: 未获取到日志路径")
        return

    print("\n" + "=" * 60)
    print("macOS 调试日志 (按 Ctrl+C 结束)")
    print("=" * 60)
    print()

    if not log_path.exists():
        print(f"⚠ 日志文件尚未创建: {log_path}")
        print("\n可能原因：")
        print("  1. DBeaver 尚未完全启动")
        print("  2. 调试模式未正确配置")
        print("\n稍后可手动执行以下命令查看日志：")
        print(f"  tail -f '{log_path}'")
        return

    try:
        # 使用 tail -f 实时跟踪日志文件
        subprocess.call(['tail', '-f', str(log_path)])
    except FileNotFoundError:
        print("⚠ 未找到 tail 命令")
        print("\n请手动执行以下命令查看日志：")
        print(f"  tail -f '{log_path}'")
    except KeyboardInterrupt:
        # 用户按 Ctrl+C 中断
        print("\n\n日志输出已停止")
    except Exception as e:
        print(f"\n⚠ 日志输出出错: {e}")
        print("\n请手动执行以下命令查看日志：")
        print(f"  tail -f '{log_path}'")


def main():
    """
    主函数：协调整个自动部署流程

    执行的步骤：
    1. 检测操作系统
    2. 获取 DBeaver 安装路径（命令行参数或交互式输入）
    3. 确定 DBeaver 安装目录
    4. 读取版本信息和产品 ID
    5. 查找并复制依赖 jar 文件
    6. 更新 pom.xml 配置
    7. 编译项目
    8. 部署 agent 到 DBeaver
    9. 生成许可证密钥
    10. 更新 dbeaver.ini 配置
    11. 处理 JRE 目录（强制使用系统 JDK）
    12. 启动 DBeaver

    命令行参数：
        python onekey.py [DBeaver安装路径]

    示例：
        python onekey.py "C:\\Program Files\\DBeaver"
        python onekey.py "/Applications/DBeaver.app"
        python onekey.py "/usr/share/dbeaver"
        python onekey.py  # 交互式输入

    Returns:
        退出码 0：成功
        退出码 1：失败
    """
    print("=" * 60)
    print("DBeaver Agent - 自动部署工具")
    print("=" * 60)

    system = platform.system()
    print(f"检测到操作系统: {system}")

    # 获取脚本所在目录（项目根目录）
    script_dir = Path(__file__).parent.absolute()
    pom_file = script_dir / 'pom.xml'
    libs_dir = script_dir / 'libs'

    # 验证项目结构：检查 pom.xml 是否存在
    if not pom_file.exists():
        print(f"错误: 找不到 pom.xml 文件: {pom_file}")
        sys.exit(1)

    # 获取 DBeaver 路径（命令行参数或交互式输入）
    dbeaver_path = None

    if len(sys.argv) > 1:
        # 从命令行参数获取路径
        dbeaver_path = sys.argv[1]
    else:
        # 交互式输入，提供不同平台的示例
        print("\n请输入 DBeaver 路径：")
        if system == 'Darwin':
            print("  - macOS: /Applications/DBeaver.app 或 /Applications/DBeaverUltimate.app")
        elif system == 'Windows':
            print("  - Windows: C:\\Program Files\\DBeaver 或 dbeaver.exe 路径")
        else:
            print("  - Linux: /usr/share/dbeaver 或 dbeaver 可执行文件路径")
        print()
        dbeaver_path = input("路径: ").strip()

    if not dbeaver_path:
        print("错误: 未提供路径")
        sys.exit(1)

    mac_log_path = None

    try:
        # 初始化进度跟踪器（总共 10 个步骤）
        global progress
        progress = ProgressTracker(10)

        # ===== 步骤 1: 确定 DBeaver 目录 =====
        progress.next_step(f"正在处理: {dbeaver_path}")
        dbeaver_dir = find_dbeaver_dir(dbeaver_path)
        print(f"✓ DBeaver 目录: {dbeaver_dir}")

        # ===== 步骤 2: 读取版本号和产品 ID =====
        progress.next_step("读取版本信息...")
        main_version, product_id = read_version_from_eclipseproduct(dbeaver_dir)
        print(f"✓ 检测到版本: {main_version}")
        print(f"✓ 产品ID: {product_id}")

        # ===== 步骤 3: 查找并复制依赖 jar 文件 =====
        progress.next_step("从 plugins 目录查找依赖...")
        jar_info_list = find_and_copy_jars(dbeaver_dir, libs_dir)

        if not jar_info_list:
            print("警告: 未找到任何依赖 jar 文件")

        # ===== 步骤 4: 更新 pom.xml =====
        progress.next_step("更新 pom.xml...")
        update_pom_xml(pom_file, main_version, jar_info_list)

        # ===== 步骤 5: 编译项目 =====
        progress.next_step("编译项目...")
        compiled_jar = compile_project(script_dir)

        # ===== 步骤 6: 部署到 DBeaver =====
        progress.next_step("部署到 DBeaver...")
        deploy_agent_to_dbeaver(compiled_jar, dbeaver_dir)

        # ===== 步骤 7: 生成许可证 =====
        progress.next_step("生成许可证...")
        generate_license(dbeaver_dir, product_id, main_version)

        # ===== 步骤 8: 更新配置文件 =====
        progress.next_step("更新配置文件...")
        update_dbeaver_ini(dbeaver_dir)

        # ===== 步骤 9: 处理 JRE 目录 =====
        progress.next_step("处理 JRE 目录...")
        rename_jre_directory(dbeaver_dir)

        # ===== 步骤 10: 启动 DBeaver =====
        progress.next_step("启动 DBeaver...")
        mac_log_path = start_dbeaver(dbeaver_dir)

        # 完成提示
        print("\n" + "=" * 60)
        print("✓ 所有步骤完成！DBeaver 已启动")
        print("=" * 60)
        print("\n下一步：")
        print("  1. 点击 'Import license'")

        # 显示许可证密钥和剪贴板状态
        global _license_key, _clipboard_tool
        if _clipboard_tool:
            print("  2. 粘贴下列许可证密钥（已复制到剪贴板）")
        else:
            print("  2. 粘贴下列许可证密钥（未找到剪贴板工具，请手动复制）")

        if _license_key:
            print(f"\n--- LICENSE ---")
            print(f"{_license_key}")
            print(f"--- END LICENSE ---\n")

        print("  3. 点击 'Import' 按钮")
        print("  4. 激活后勾选 'Do not share data'")

        system = platform.system()
        if system == 'Windows':
            print("\nWindows 提示：")
            print("  - 若需查看详细日志，可关闭当前窗口")
            print("  - 在命令行运行: dbeaver-cli.exe")
        elif system == 'Darwin':
            stream_macos_debug_log(mac_log_path)

    except Exception as e:
        # 捕获所有异常并显示错误信息
        print(f"\n✗ 错误: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
