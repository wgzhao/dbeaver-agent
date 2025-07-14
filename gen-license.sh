#!/bin/bash
jar_file="dbeaver-agent-25.1-jar-with-dependencies.jar"
cd "$(dirname "$0")" || exit 1
if [ ! -f target/${jar_file} ]; then
  echo "Jar file not found, building..."
  mvn clean package || exit 1
fi

java -cp "libs/*:./target/${jar_file}"  com.dbeaver.agent.License "$@"