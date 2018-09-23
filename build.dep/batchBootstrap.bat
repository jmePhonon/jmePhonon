for /f "tokens=4-5 delims=. " %%i in ('ver') do set WIN_VERSION=%%i.%%j

echo Running on Windows %WINDOWS_VERSION%

if "%WIN_VERSION" == "10.0"  (
	echo "Run with docker for windows"
    bash.exe "build.dep/bashBootstrap.sh" %*
) else (
	echo "Run with docker toolbox"
	"%PROGRAMFILES%\Git\bin\bash.exe" --login -i "%PROGRAMFILES%\Docker Toolbox\start.sh" "build.dep/bashBootstrap.sh" %*
)

