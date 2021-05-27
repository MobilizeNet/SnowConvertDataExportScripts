# Testing Process

The purpose of this document is to go step by step on how we test the Oracle Export Tool to generate the query scripts and to get the CSV files with the datadump for a given Oracle Database

This tool has been tested against two similar scenarios, one using Windows 10 and running the Powershell script as the extraction tool and the other one using Ubuntu 20.04 and running the Shell script as the extraction tool. For both scenarios we used the *SH (Sales History)* database with a local Oracle 19c installation in Windows.

# Software/Tools needed for testing purposes

* Oracle 19c
* SQL*Plus (version 19.\*)
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

For the query generation and data extraction, we used the Oracle sample database called *SH (Sales History)* which consists of about 17 tables with around **992K** rows in total.

### SH tables
|          Table Name        |           # of rows          |
|:---------------------------|-----------------------------:|
| CAL_MONTH_SALES_MV         |                      48 rows |
| CHANNELS                   |                       5 rows |
| COSTS                      |                       0 rows |
| COUNTRIES                  |                      23 rows |
| CUSTOMERS                  |                  55,500 rows |
| DR$SUP_TEXT_IDX$I          |                       0 rows |
| DR$SUP_TEXT_IDX$K          |                       0 rows |
| DR$SUP_TEXT_IDX$N          |                       0 rows |
| DR$SUP_TEXT_IDX$U          |                       0 rows |
| FWEEK_PSCAT_SALES_MV       |                  11,266 rows |
| PROFITS                    |                       0 rows |
| PRODUCTS                   |                      72 rows |
| PROMOTIONS                 |                     503 rows |
| SALES                      |                 918,843 rows |
| SALES_TRANSACTIONS_EXT     | *error when querying count.* |
| SUPPLEMENTARY_DEMOGRAPHICS |                   4,500 rows |
| TIMES                      |                   1,826 rows |

For the installation of the SH schema, we followed the official tutorials on the Oracle Documentation website, as well as, some YouTube tutorials since there were some unclear steps.

## Scenario 1: Windows

For this scenario we used a local Windows 10 installation. The Oracle 19c database was also local to the Windows 10 setup. Also, we used SQL\*Plus tool (19.3). 

## Scenario 2: Ubuntu

For this scenario we used a Ubuntu 20.04 installation on a cloud tool called Gitpod. The Oracle 19c database used was the same one installed in Windows 10, but tunneled using *ngrok*. Also, we used SQL\*Plus tool (19.11). 

## Results

#### Average time per table (in seconds)

|            Table Name           |           Number of rows          | Execution time (Scenario 1 - Windows)     | Execution time (Scenario 2 - Ubuntu)     |
|:--------------------------------|----------------------------------:|------------------------------------------:|-----------------------------------------:|
| CAL_MONTH_SALES_MV              |                           48 rows |                                    2 secs |                                   3 secs |
| CHANNELS                        |                            5 rows |                                    2 secs |                                   3 secs |
| COSTS                           |                            0 rows |                                    2 secs |                                   3 secs |
| COUNTRIES                       |                           23 rows |                                    2 secs |                                   3 secs |
| CUSTOMERS                       |                       55,500 rows |                                    2 secs |                                  12 secs |
| DR$SUP_TEXT_IDX$I               |                            0 rows |                                    2 secs |                                   3 secs |
| DR$SUP_TEXT_IDX$K               |                            0 rows |                                    2 secs |                                   3 secs |
| DR$SUP_TEXT_IDX$N               |                            0 rows |                                    2 secs |                                   3 secs |
| DR$SUP_TEXT_IDX$U               |                            0 rows |                                    2 secs |                                   3 secs |
| FWEEK_PSCAT_SALES_MV            |                       11,266 rows |                                    2 secs |                                   4 secs |
| PROFITS                         |                            0 rows |                                    2 secs |                                   3 secs |
| PRODUCTS                        |                           72 rows |                                    2 secs |                                   3 secs |
| PROMOTIONS                      |                          503 rows |                                    2 secs |                                   3 secs |
| SALES                           |                      918,843 rows |                                    5 secs |                                  95 secs |
| SALES_TRANSACTIONS_EXT          |      *error when querying count.* |                                    2 secs |                                   3 secs |
| SUPPLEMENTARY_DEMOGRAPHICS      |                        4,500 rows |                                    2 secs |                                   4 secs |
| TIMES                           |                        1,826 rows |                                    2 secs |                                   3 secs |

To be fair, the execution time for the Scenario 1 (in Windows) is  lower since the Oracle setup is local to the Windows installation, so there are no external connections. Whilst, the execution time for the Scenario 2 (in Ubuntu using Gitpod) is higher since we are tunneling the connection to the local Oracle setup to expose it and use it in Gitpod.

