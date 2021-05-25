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

oracleHost=$1
oraclePort=$2
oracleSid=$3
oracleUser=$4
oraclePassword=$5
oracleScriptFilename=$6
oracleOutfilename=$7

base=$(dirname "$0")
subdir="/datadumps"
newFilePath=$base$subdir

mkdir -p $newFilePath

filename=$(basename -- "$oracleScriptFilename")
newFileName="${filename%.*}.sqlplus.sql"

fullPath="$newFilePath/$newFileName"

query=`cat $oracleScriptFilename`
printf "set colsep \",\"\nset headsep off\nSET SERVEROUTPUT ON\nset termout off\nset trimout on\nset pagesize 0\nset trimspool on\nset newpage NONE\nset feedback off\nspool $oracleOutfilename\n\n$query \nspool off \nexit\n/" > $fullPath

sqlplus -S $oracleUser/$oraclePassword@$oracleHost:$oraclePort/$oracleSid "@$$fullPath"

rm $fullPath

exit
