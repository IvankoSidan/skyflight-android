@if "%DEBUG%" == "" @echo off
setlocal
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%
set JAVA_EXE=java.exe
if defined JAVA_HOME set JAVA_EXE=%JAVA_HOME%/bin/java.exe
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
