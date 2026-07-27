@echo off
chcp 65001 > nul

if not exist bin mkdir bin
if exist sources.tmp del sources.tmp

for /r src %%f in (*.java) do echo %%f>>sources.tmp

javac -encoding UTF-8 -d bin @sources.tmp

if errorlevel 1 (
    del sources.tmp
    echo Falha na compilacao.
    pause
    exit /b 1
)

del sources.tmp

java -cp bin br.com.monitorarionegro.main.Main

pause