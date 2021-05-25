@echo off
set currdir=%~dp0
set oracleHost=%1
set oraclePort=%2
set oracleSid=%3
set oracleUser=%4
set oraclePassword=%5
set oracleScriptFilename=%6
set oracleOutfilename=%7

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%currdir%extract.ps1" %oracleHost% %oraclePort% %oracleSid% %oracleUser% %oraclePassword% %oracleScriptFilename% %oracleOutfilename%