#!/bin/bash
jar_file=$(ls target/dbeaver-agent-*-with-dependencies.jar 2>/dev/null | head -n 1)
if [ -z "$jar_file" ]; then
  echo "Jar file not found in target directory."
    cd "$(dirname "$0")" || exit 1
    if [ ! -f target/${jar_file} ]; then
      echo "Jar file not found, building..."
      mvn clean package || exit 1
    fi
fi

java -cp "libs/*:${jar_file}"  com.dbeaver.agent.License "$@"