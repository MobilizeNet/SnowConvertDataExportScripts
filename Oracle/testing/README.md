# Testing Process

The purpose of this document is to go step by step on how we test the Oracle Export Tool to generate the query scripts and to get the CSV files with the datadump for a given Oracle Database.

This tool has been tested against two similar scenarios, one using Windows 10 and running the Powershell script as the extraction tool and the other one using Ubuntu 20.04 and running the Shell script as the extraction tool. For both scenarios we used the *SH (Sales History)* database with a local Oracle 19c installation in Windows.

# Software/Tools needed for testing purposes

* Oracle databases (11g and 19c)
* SQL*Plus (11.\* and 19.\*)
* ngrok
* Gitpod with Ubuntu 20.04

## YAML Configuration File

As expected by the tool, we created a YAML config file that was used for all the scenarios. The values of that file can be seen in the below code snippet:

```yml
description: "Testing the Oracle Extraction Tool"
extractor: "./extract.sh $host $port $sid $serviceName $user $password $filename $outfilename" # This is used in Ubuntu
#extractor: "./extract.ps1 $host $port $sid $serviceName $user $password $filename $outfilename" # This is used in Windows
exportDir: "./exportQueries"
dumpDir: "./dataDumps"
processes: 4
schemas: 
  sh: ~ # We used ~ to retrieve every table from that schema.
```

## Oracle Database

For the query generation and data extraction, we used the Oracle sample database called *SH (Sales History)* which consists of about 17 to 18 tables with around **992K~1.99M** rows in total (for Oracle 11g and Oracle 19c, respectively). In bold, you can see the differences between this sample database for both versions of the Oracle installation.

### SH tables
|          Table Name        |           # of rows - Oracle 11g         |           # of rows - Oracle 19c         |
|:---------------------------|-----------------------------------------:|-----------------------------------------:|
| CAL_MONTH_SALES_MV         |                                  48 rows |                                  48 rows |
| CHANNELS                   |                                   5 rows |                                   5 rows |
| COSTS                      |                          **82,112 rows** |                               **0 rows** |
| COUNTRIES                  |                                  23 rows |                                  23 rows |
| CUSTOMERS                  |                              55,500 rows |                              55,500 rows |
| DIMENSIONS                 |                               **0 rows** |                                  **N/A** |
| DR\$SUP_TEXT_IDX\$I        |                                   0 rows |                                   0 rows |
| DR\$SUP_TEXT_IDX\$K        |                                   0 rows |                                   0 rows |
| DR\$SUP_TEXT_IDX\$N        |                                   0 rows |                                   0 rows |
| DR\$SUP_TEXT_IDX\$R        |                               **0 rows** |                                  **N/A** |
| DR\$SUP_TEXT_IDX\$U        |                                  **N/A** |                               **0 rows** |
| FWEEK_PSCAT_SALES_MV       |                              11,266 rows |                              11,266 rows |
| PROFITS                    |                         **916,039 rows** |                               **0 rows** |
| PRODUCTS                   |                                  72 rows |                                  72 rows |
| PROMOTIONS                 |                                 503 rows |                                 503 rows |
| SALES                      |                             918,843 rows |                             918,843 rows |
| SALES_TRANSACTIONS_EXT     |                                   0 rows |             *error when querying table.* |
| SUPPLEMENTARY_DEMOGRAPHICS |                               4,500 rows |                               4,500 rows |
| TIMES                      |                               1,826 rows |                               1,826 rows |

For the installation of the SH schema, we followed the official tutorials on the Oracle Documentation website, as well as, some YouTube tutorials since there were some unclear steps. For more information on the installation process, you can find some links and resources at the end of this document in the [Resources](#resources) section.

## Scenario 1 - Oracle 11g

For this scenario we used a local Windows 10 installation and an Ubuntu 20.04 installation on a cloud tool called Gitpod. For both cases the Oracle database was hosted in the Windows 10 local setup. For accesing the database in Ubuntu (Gitpod), we used a tool named *ngrok* to tunned the connection to the local database in Windows. As per the SQL*Plus versions, we tested against version 19.11 (Windows) and 11.2 (Ubuntu). Unfortunately, there were some issues trying to access the local database using ngrok and SQL\*Plus in Ubuntu. The issue was not with the local database or the tunneled connection because using other tools (like SQL Developer or DBeaver from remote computers) gave no issues.

### Results

In the following table you can find the results for this scenario. For different reasons it was not possible to test in Ubuntu using SQL\*Plus 11.2, so there are no execution times.

#### Average time per table (in seconds)

|          Table Name        |    # of rows - Oracle 11g         | Execution time (Test case 1 - Windows)    | Execution time (Test case 2 - Ubuntu)    |
|:---------------------------|----------------------------------:|------------------------------------------:|-----------------------------------------:|
| CAL_MONTH_SALES_MV         |                           48 rows |                                    1 secs |                                   ? secs |
| CHANNELS                   |                            5 rows |                                    1 secs |                                   ? secs |
| COSTS                      |                       82,112 rows |                                   13 secs |                                   ? secs |
| COUNTRIES                  |                           23 rows |                                    1 secs |                                   ? secs |
| CUSTOMERS                  |                       55,500 rows |                                    9 secs |                                   ? secs |
| DIMENSIONS                 |                            0 rows |                                    1 secs |                                   ? secs |
| DR\$SUP_TEXT_IDX\$I        |                            0 rows |                                    2 secs |                                   ? secs |
| DR\$SUP_TEXT_IDX\$K        |                            0 rows |                                    0 secs |                                   ? secs |
| DR\$SUP_TEXT_IDX\$N        |                            0 rows |                                    2 secs |                                   ? secs |
| DR\$SUP_TEXT_IDX\$R        |                            0 rows |                                    0 secs |                                   ? secs |
| FWEEK_PSCAT_SALES_MV       |                       11,266 rows |                                    2 secs |                                   ? secs |
| PROFITS                    |                      916,039 rows |                                  139 secs |                                   ? secs |
| PRODUCTS                   |                           72 rows |                                    1 secs |                                   ? secs |
| PROMOTIONS                 |                          503 rows |                                    1 secs |                                   ? secs |
| SALES                      |                      918,843 rows |                                  137 secs |                                   ? secs |
| SALES_TRANSACTIONS_EXT     |                            0 rows |                                    1 secs |                                   ? secs |
| SUPPLEMENTARY_DEMOGRAPHICS |                        4,500 rows |                                    1 secs |                                   ? secs |
| TIMES                      |                        1,826 rows |                                    1 secs |                                   ? secs |


## Scenario 2 - Oracle 19g

For this scenario we used a local Windows 10 installation and an Ubuntu 20.04 installation on a cloud tool called Gitpod. For both cases the Oracle database was hosted in the Windows 10 local setup. For accesing the database in Ubuntu (Gitpod), we used a tool named *ngrok* to tunned the connection to the local database in Windows. As per the SQL*Plus versions, we tested against version 19.11 (Windows) and 11.2 (Ubuntu). 

### Results

In the following table you can find the results for this scenario. To be fair, the execution time when run in Windows is lower since the Oracle setup is local to the Windows installation, so there are no external connections. Whilst, the execution time when run in Ubuntu is higher since we are tunneling the connection to the local Oracle setup to expose it and use it in Gitpod.

#### Average time per table (in seconds)

|            Table Name           |           Number of rows          | Execution time (Test case 1 - Windows)     | Execution time (Test case 2 - Ubuntu)     |
|:--------------------------------|----------------------------------:|-------------------------------------------:|------------------------------------------:|
| CAL_MONTH_SALES_MV              |                           48 rows |                                     2 secs |                                    3 secs |
| CHANNELS                        |                            5 rows |                                     2 secs |                                    3 secs |
| COSTS                           |                            0 rows |                                     2 secs |                                    3 secs |
| COUNTRIES                       |                           23 rows |                                     2 secs |                                    3 secs |
| CUSTOMERS                       |                       55,500 rows |                                     2 secs |                                   12 secs |
| DR\$SUP_TEXT_IDX\$I             |                            0 rows |                                     2 secs |                                    3 secs |
| DR\$SUP_TEXT_IDX\$K             |                            0 rows |                                     2 secs |                                    3 secs |
| DR\$SUP_TEXT_IDX\$N             |                            0 rows |                                     2 secs |                                    3 secs |
| DR\$SUP_TEXT_IDX\$U             |                            0 rows |                                     2 secs |                                    3 secs |
| FWEEK_PSCAT_SALES_MV            |                       11,266 rows |                                     2 secs |                                    4 secs |
| PROFITS                         |                            0 rows |                                     2 secs |                                    3 secs |
| PRODUCTS                        |                           72 rows |                                     2 secs |                                    3 secs |
| PROMOTIONS                      |                          503 rows |                                     2 secs |                                    3 secs |
| SALES                           |                      918,843 rows |                                     5 secs |                                   95 secs |
| SALES_TRANSACTIONS_EXT          |      *error when querying count.* |                                     2 secs |                                    3 secs |
| SUPPLEMENTARY_DEMOGRAPHICS      |                        4,500 rows |                                     2 secs |                                    4 secs |
| TIMES                           |                        1,826 rows |                                     2 secs |                                    3 secs |


## Final Analysis and Recommendation

As part of the R&D we did for this tool, we found that using SQL\*Plus can return different results depending on the version we are using (independent of the Oracle version). For SQL\*Plus 12.2 and lower versions there is no easy-and-straightforward support for outputing the query results as CSV. When using that version, we need to modify the query itself to concat each column with commas and quotes (when needed). We also need to handle, by ourselves, the case when the column is of any string type and it has double quotes in it by using the REPLACE function inside the SELECT expression. E.g. 
```sql
SELECT Column1 || ',' || '"' || REPLACE(Column2, '"', '""' ) || '"' || ',' || '"' || REPLACE(Column3, '"', '""' ) || '"'
FROM TABLE_A;
```

Other issue we found was with the remote connection (explained earlier in the Scenario 1), for some reason we were not able to use the SQL\*Plus 11.2 in Ubuntu to connect remotely to the local Oracle installation. Also, if the resulting query exceeds more than 2499 characters then the SQL\*Plus will not be able to process the query and will return an error (*SP2-0027: Input is too long (> 2499 characters) - line ignored*). All that and possibly more, makes SQL\*Plus 12.2 and lower versions not a good option to be used as an extractor.

On the other hand, when using SQL\*Plus 12.2 and higher versions there is a native support to output results as CSV, even from the command line (just by adding the parameter *-M "CSV ON"*). Plus, we don't need to make any modifications to the query to get the result as CSV. The same query we use in any other tool can be used here. 

Because of all that, we recommend to download a newer version of SQL\*Plus even if the Oracle version is lower than Oracle 12c. Oracle database and SQL\*Plus are independent tools, so we can have Oracle 12c and use SQL\*Plus 19.

## Resources

Here there are some links and videos used as reference for the installation process of the sample datases for Oracle 19c and for the installation of SQL\*Plus 19.2 in Ubuntu. 

### For enabling Oracle 11g sample schemas
- [Oracle Database XE Prior Release Archive](https://www.oracle.com/database/technologies/xe-prior-releases.html). For getting the Oracle 11g XE installers.
- [Oracle Database Sample Schemas](https://github.com/oracle/db-sample-schemas). Here you can find the *SH* schema (as well as the other sample schemas) with instructions to install in Oracle 11g Express Edition.
- [How to Unlock Oracle Sample HR Schema Account](https://www.youtube.com/watch?v=gLnmvmJYH3s)

### For enabling Oracle 19c sample schemas
- [Oracle Database Software Downloads](https://www.oracle.com/database/technologies/oracle-database-software-downloads.html). For getting the Oracle 19c installers.
- [How to unlock SH sample schema by Manish Sharma](https://www.youtube.com/watch?v=HQfaOPtyrwo&t=814s)
- [Installing Oracle OE Sample Schema](https://www.youtube.com/watch?v=c8dxF-1rGZo)
- [How to Install Sample Schemas in Oracle 19C](https://www.youtube.com/watch?v=kc4ZcG7JRmg&t=180s)
- [Oracle 19c: How to unlock HR user in Oracle Database 19c by Manish Sharma](https://www.youtube.com/watch?v=7sPUqpY8WTo&t=189s)

### For installing SQL\*Plus in Ubuntu
- [Ubuntu: How to install SqlPlus? (3 Solutions!!)](https://www.youtube.com/watch?v=S7F19Zk8gm8&t=75s)
- There is also one shell script you can use called *sqlplus_installation.sh* in this project under the testing folder.