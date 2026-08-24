@echo off
setlocal

set "PROJECT_HOME=%~dp0"
for %%i in ("%PROJECT_HOME%") do set "PROJECT_HOME=%%~fi"

set "GRADLE_USER_HOME=%PROJECT_HOME%\.gradle-user-home"

rem Prefer the optional project-local JDK, but remain portable when it is absent.
if exist "%PROJECT_HOME%\work\jdk-25\bin\java.exe" (
    set "JAVA_HOME=%PROJECT_HOME%\work\jdk-25"
)

pushd "%PROJECT_HOME%"
set "GRADLE_COMMAND="
if defined GRADLE_HOME if exist "%GRADLE_HOME%\bin\gradle.bat" set "GRADLE_COMMAND=%GRADLE_HOME%\bin\gradle.bat"
if not defined GRADLE_COMMAND (
    where gradle.bat >nul 2>&1
    if not errorlevel 1 set "GRADLE_COMMAND=gradle.bat"
)
if defined GRADLE_COMMAND (
    call "%GRADLE_COMMAND%" %*
) else (
    call "%PROJECT_HOME%\gradlew.bat" %*
)
set "EXIT_CODE=%ERRORLEVEL%"
popd

exit /b %EXIT_CODE%
