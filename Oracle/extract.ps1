# Oracle Data Extraction script
# 
# @author: Jonathan Fonseca-Vallejos
# @company: Mobilize.net
# @date: May 24th, 2021
#
# This script is intended to be used as part of the Oracle Export Tool. 
# This script receives Oracle connection parameters as host, port, SID, user, password, SQL query script filename, and a CSV output filename.
# A connection to the Oracle database is created using SQL*Plus. Some preprocessing is done to the SQL query script file by adding some information required by the SQL*Plus tool.
# The modified SQL query script is run against that DB and the data resultset is stored in the CSV output file.

$oracleHost = $args[0]
$oraclePort = $args[1] 
$oracleSid = $args[2]
$oracleUser = $args[3] 
$oraclePassword = $args[4] 
$oracleScriptFilename = $args[5]
$oracleOutfilename = $args[6]

$newFilePath = Join-Path (Get-Item $PSCommandPath).DirectoryName "datadumps"

if (![System.IO.Directory]::Exists($newFilePath )) {
    New-Item -ItemType Directory -Force -Path $newFilePath
}

$newFileName = (Get-Item $oracleScriptFilename).Basename + ".sqlplus.sql"

$fullPath = Join-Path $newFilePath $newFileName

$isSqlplusGreaterThan = $false
$sqlplusVersion = (sqlplus -V)[2]

if($sqlplusVersion -match "Version (\d+)\.(\d+).") {
    if( (($Matches.1 -eq 12) -and ($Matches.2 -ge 2)) -or ($Matches.1 -gt 12) ) {
        $isSqlplusGreaterThan = $true
    } 
} 

if ($isSqlplusGreaterThan) {
    $prependText = "set markup csv on set feedback off `nspool `"$oracleOutfilename`""
} else {
    $prependText = "set colsep `",`"`nset headsep off`nSET SERVEROUTPUT ON`nset termout off`nset trimout on`nset pagesize 0`nset trimspool on`nset newpage NONE`nset feedback off`nspool `"$oracleOutfilename`"" 
}

New-Item -Path $newFilePath -Name $newFileName -ItemType File -Value $prependText -Force

$scriptContent = "`n`n" + (Get-Content $oracleScriptFilename)
Add-Content -Path $fullPath -Value $scriptContent
Add-Content -Path $fullPath -Value "spool off `nexit`n/"

if ($isSqlplusGreaterThan) {
    Write-Output exit | sqlplus -S -M "CSV ON" $oracleUser/$oraclePassword@${oracleHost}:$oraclePort/$oracleSid "@$fullPath"
} else {
    Write-Output exit | sqlplus -S $oracleUser/$oraclePassword@${oracleHost}:$oraclePort/$oracleSid "@$fullPath"
}

Remove-Item $fullPath

exit