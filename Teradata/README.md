# Teradata Data Export Scripts

These scripts can be used to perform a Teradata Data Export.


## How does this work?

The script `bin/create_exports.sh` will connect to your database and create a collection of TPT files and then run them to generate a collection of `.CSV` files. 
You can also then use the `bin/create_load_to_sf.sh` to create a `.sql` file that you can run with snowsql to upload your files into snowflake.


## How are these scripts used ?

In order to use them.

1. Make a copy of the bin an scripts folder
2. Modify the `bin\create_exports.sh`

2.1 The connection setting will be at the top of this file:
Change `connection_string="127.0.0.1/dbc,dbc"` for the proper connect string for your environment.

2.2 If you want to filter which database to include adjust the `include_databases` using an expression that will only match the databases that you want considered.

2.3 If you want to filter which databases to exclude adjust the `exclude_databases`. Some Teradata system databases are already in that least, but if there are some databases that you do not want as part of this export add them here.

2.4 If you want to filter which tables to include adjust the `include_objects` you can change the conditions here, to just match a subset of tables.

3.0 Review the TPT Script Parameters

- `file_size_split_GB` parameter tells TPT script at what size to begin separating a table's data into multiple files
- `tpt_delimiter` certain characters must be escaped with a backslash, such as pipe in order for the sed replace to work properly below
- the `conn_str`,`conn_usr` and `conn_pwd` must be set to host, user and password.

## TPT Script Files 

These files contain auto-generated scripts which can later be used in the data migration process.   

* `tpt\_export\_single\_script.tpt`
* `tpt\_export\_multiple\_scripts.tpt`
* `tables\_not\_in\_tpt\_scripts.txt`

After a successful run, remove logon information from the top line of each of the files in the scripts folder.

Compress the entire ‘Teradata Source Extract’ and return to Snowflake.