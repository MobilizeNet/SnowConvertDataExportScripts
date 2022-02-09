# Test Plan Exporting Microsoft SQL Server Tables to CSV files using Windows PowerShell

**5/25/2021**

*Marco Carrillo*

This Document will explain step by step how we did the test of the export script to generate the scripts and CSV file for each table of a given *SQL Server Database*

## Inicial Conditions

We installed a local MS SQL Server 2019 Database and SQL Server Management Studio, then we proceed with the restoration of the AdventureWorks2019 sample database as it's explained on this link https://docs.microsoft.com/en-us/sql/samples/adventureworks-install-configure?view=sql-server-ver15&tabs=ssms

Also, we used another database that uses the MS SQL Server 2012 version with 350 tables placed in 2 schemas, a couple of them with more than a million rows and we ensured the grants needed to access this database with Windows Authentication.

Then, we started with the creation of the path that will store the generated files. In this case, we created the folder ExportFiles in the C drive.

![enter image description here](/img/img1.png)


##  Beginning with the test

- We can place the *script_bcp.ps1* and *Parameters.conf* files anywhere; for this example, we placed them in Documents
![enter image description here](/img/img2.png)
- We proceeded to add the parameters in the *Parameters.conf*, we can do it by opening it whit Notepad and putting the parameters in order we specified in the ReadMe file. For this example, we defined as it's shown below

![enter image description here](/img/img3.png)

- Then, we right-click on *script_bcp.ps1* and click on **Run with PowerShell**

![enter image description here](/img/img4.png)

- As we specified in *Parameters.conf* at the fourth line (**N**), we just created the extraction scripts, we won't run them, for the tables in the dbo, Person, and Production schemas. PowerShell indicates 41 scripts are being generated.

![enter image description here](/img/img5.png)


-If we go to the path specified in *Parameters.conf*, we can see we generated 42 files: 41 PowerShell scripts and the CreateScriptLog.log where we can find the events logged during the run of *script_bcp.ps1*

![enter image description here](/img/img6.png)


- If we change the fourth line of *Parameters.conf* to **S** after run *script_bcp.ps1* and proceed to run it as we saw before, we can see in the path specified in *Parameters.conf* 85 files: 

41 PowerShell scripts for each table, the CreateScriptLog.log, the ScriptLog.csv, and 42 CSV files for the tables we extracted, but... 

Why 42 and not 41? That's because there is a table, TransactionHistory, that has more than 100 000 rows (the limit of rows per file we specified in *Parameters.conf* seventh line) and it created the file0 and file1 for this table.

![enter image description here](/img/img7.png)
