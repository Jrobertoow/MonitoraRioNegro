#!/usr/bin/env bash
set -e
mkdir -p bin
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
rm -f sources.txt
java -cp bin br.com.monitorarionegro.main.Main
