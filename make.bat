cd /D %~dp0

if exist "%PROGRAMFILES%\Docker Toolbox"  (
	echo "Run with docker toolbox"
	"%PROGRAMFILES%\Git\bin\bash.exe" --login -i "%PROGRAMFILES%\Docker Toolbox\start.sh" "make.sh" %*
) else (
	echo "Run with docker for windows"
    "%PROGRAMFILES%\Git\bin\bash.exe" --login -i "make.sh" %*
)

PAUSE