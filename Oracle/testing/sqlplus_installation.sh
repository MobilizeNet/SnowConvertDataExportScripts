#!/bin/bash

# Script to install Oracle Client and SQL*Plus
# Expected parameter: Oracle version to be installed.
# Possible values: 19 or 21

versionToInstall=$1

if [ $versionToInstall = 19 ]
then 
    basicUrl="https://download.oracle.com/otn_software/linux/instantclient/1911000/oracle-instantclient19.11-basic-19.11.0.0.0-1.x86_64.rpm"
    sqlplusUrl="https://download.oracle.com/otn_software/linux/instantclient/1911000/oracle-instantclient19.11-sqlplus-19.11.0.0.0-1.x86_64.rpm"
    versionFolder="19.11"
elif [ $versionToInstall = 21 ]
then
    basicUrl="https://download.oracle.com/otn_software/linux/instantclient/211000/oracle-instantclient-basic-21.1.0.0.0-1.x86_64.rpm"
    sqlplusUrl="https://download.oracle.com/otn_software/linux/instantclient/211000/oracle-instantclient-sqlplus-21.1.0.0.0-1.x86_64.rpm"
    versionFolder="21.1"
fi

basicOutputName="oracle-basic-$versionFolder.rpm"
sqlplusOutputName="oracle-sqlplus-$versionFolder.rpm" 

curl $basicUrl -o $basicOutputName
curl $sqlplusUrl -o $sqlplusOutputName

apt-get install alien --yes

alien -i $basicOutputName
alien -i $sqlplusOutputName

apt-get install libaio1

mkdir -p /etc/ld.conf.d/
echo "/usr/lib/oracle/$versionFolder/client64/lib/" > /etc/ld.so.conf.d/oracle.conf

ldconfig

rm $basicOutputName
rm $sqlplusOutputName