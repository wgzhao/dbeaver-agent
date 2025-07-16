#!/bin/bash
# DBeaver License Generator GUI Launcher for Unix-like systems

jar_file="dbeaver-agent-25.1-jar-with-dependencies.jar"
cd "$(dirname "$0")" || exit 1

# Check if jar file exists, build if necessary
if [ ! -f target/${jar_file} ]; then
  echo "Jar file not found, building..."
  mvn clean package || exit 1
fi

# Launch the GUI
java -cp "libs/*:./target/${jar_file}" com.dbeaver.agent.ui.LicenseGeneratorUI "$@"