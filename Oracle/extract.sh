#!/bin/bash

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
# This is a free tool, so please feel free to use it or modify it at your convenience. 

oracleHost=$1
oraclePort=$2
oracleSid=$3
oracleServiceName=$4
oracleUser=$5
oraclePassword=$6
oracleScriptFilename=$7
oracleOutfilename=$8

base=$(dirname "$0")
subdir="/dataDumps"
newFilePath=$base$subdir

mkdir -p $newFilePath

filename=$(basename -- "$oracleScriptFilename")
newFileName="${filename%.*}.sqlplus.sql"

fullPath="$newFilePath/$newFileName"

isSqlplusGreaterThan=false
sqlplusVersion=$(sqlplus -V)
FWK_REGEX=".Version ([0-9]+)\.([0-9]+)."

if [[ $(echo $sqlplusVersion) =~ $FWK_REGEX ]]
then
    if [[ ( ${BASH_REMATCH[1]} -gt 12 ) || ( ${BASH_REMATCH[1]} -eq 12 && ${BASH_REMATCH[2]} -ge 2 ) ]]
    then
        isSqlplusGreaterThan=true
    fi
fi

query=`cat $oracleScriptFilename`
if [[ $isSqlplusGreaterThan = true ]]
then
    printf "set arraysize 1000 \nset rowprefetch 1000\nset termout off\nspool \"$oracleOutfilename\" \n\n$query \nspool off \nexit\n/"  > $fullPath

    if [ $oracleSid != "null" ]
    then
        sqlplus -S -M "CSV ON" $oracleUser/$oraclePassword@$oracleHost:$oraclePort:$oracleSid "@$fullPath"
    elif [ $oracleServiceName != "null" ]
    then 
        sqlplus -S -M "CSV ON" $oracleUser/$oraclePassword@$oracleHost:$oraclePort/$oracleServiceName "@$fullPath"
    fi
else
    printf "set wrap off\nset linesize 32767\nset colsep \",\"\nset headsep off\nSET SERVEROUTPUT ON\nset termout off\nset trimout on\nset pagesize 0\nset trimspool on\nset newpage NONE\nset feedback off\nspool $oracleOutfilename\n\n$query \nspool off \nexit\n/" > $fullPath

    if [ $oracleSid != "null" ]
    then
        sqlplus -S $oracleUser/$oraclePassword@$oracleHost:$oraclePort:$oracleSid "@$fullPath"
    elif [ $oracleServiceName != "null" ]
    then 
        sqlplus -S $oracleUser/$oraclePassword@$oracleHost:$oraclePort/$oracleServiceName "@$fullPath"
    fi
fi

rm $fullPath

exit
