@REM Maven Wrapper startup script for Windows (simplified, self-bootstrapping)
@echo off
setlocal
set WRAPPER_DIR=%~dp0.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPS=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%WRAPPER_JAR%" (
  for /f "tokens=2 delims==" %%a in ('findstr /b wrapperUrl "%WRAPPER_PROPS%"') do set WRAPPER_URL=%%a
  echo Downloading Maven Wrapper...
  powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

java -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%~dp0" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
