@echo off
setlocal
cd /d "%~dp0.."

call gradlew.bat clean build --no-daemon --stacktrace
if errorlevel 1 exit /b %errorlevel%

echo Build complete. Release JARs:
for %%F in (build\libs\*.jar) do (
  echo %%~nxF | findstr /I /C:"-sources.jar" >nul || echo %%~fF
)
