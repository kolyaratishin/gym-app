@echo off
chcp 65001 > nul

REM Переходимо в папку, де знаходиться цей update_.bat
cd /d "%~dp0"

echo ==============================
echo Оновлення Gym App...
echo ==============================
echo.

git reset --hard > update_log.txt 2>&1
git pull https://github.com/kolyaratishin/gym-app.git main >> update_log.txt 2>&1

IF %ERRORLEVEL% EQU 0 (
    echo ✅ Оновлення пройшло успішно!
    echo.
    echo Можете запускати програму через Gym_App.exe
) ELSE (
    echo ❌ Помилка при оновленні!
    echo.
    echo Покажіть розробнику файл:
    echo %~dp0update_log.txt
)

echo.
pause