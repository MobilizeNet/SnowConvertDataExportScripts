# Exporting Microsoft SQL Server Tables to CSV files using Windows PowerShell

**5/21/2021**

*Marco Carrillo*

This Document will explain the script developed to export all tables of a given *SQL Server Database*

## Parameter.conf

It's necessary the **Parameter.conf** file that contains five lines to set the parameters of the extraction file.

|Line  |Description  |
|---------------|-----------------------------|
|`First line`  |Server Name and Instance Name if it's necessary (My_DBServer or My_DBServer\MSSQLSERVER)|
|`Second line`  |Database Name  (AdventureWorks2019)|
|`Third line`  |Delimiter between each column (*Comma* is recommended) |
|`Fourth line`  |**(S/N)** '*S*' indicates if you want to run the generated PowerShell scripts to generate the CSV files, '*N*' if you just want to generate the PowerShell scripts|
|`Fifth line`  |The path where the generated files will be stored (C:\ExtractionFiles)| 

> **Notes**: The path must be created before running the Extraction Script.

## Script_bcp.ps1

This file will create the extraction scripts and will run them if it's indicated in the Parameter.conf as it was described before. If is indicated run after creating the extraction script, they will be run in a parallel process.

Place this file and Parameter.conf file in the same path before running it.

> **Notes**:  
> - Windows Authentication will be used as the connection method with the Database.
> - The PowerShell scripts will use BCP command to generate the csv files for each table. 
> - More details about BCP Utility  in https://docs.microsoft.com/en-us/sql/tools/bcp-utility?view=sql-server-ver15

# Generated files

## CreateScriptLog.log

This file will show the tracking of the generated scripts by Script_bcp.ps1. It shows one after one the tables used to generate the extraction file and the duration of each transaction.

## Generated PowerShell Scripts for each table

These scripts will be used for the extraction of the data of one table individually. The format of each script is "Extract_**SchemaName**_**TableName**.ps1"

## Generated CSV files for each table

Once you run each extraction script, it will create the CSV file that will store the data of the respective table.  The format of each script is "**SchemaName**_**TableName**.csv"

## ScriptLog.csv

This file will keep the tracking of each extraction, the information will be stored like this.

|Database Name|Schema Name|Table Name|Duration|
|--|--|--|--|

## ErrorLog.csv

This file will keep the tracking of each exception caught in the extraction; the information of the errors will be stored like this.

|Database Name|Schema Name|Table Name|Error Message|
|--|--|--|--|