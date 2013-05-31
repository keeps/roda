@echo off
rem OS/2 script for RODA-in, requires JAVA 1.4.1 installed

set JAVA_EXE="$JAVA_HOME\bin\javaw"

%JAVA_EXE% -Xmx256m -jar "$INSTALL_PATH\roda-in.jar"

