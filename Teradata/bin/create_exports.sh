##VERSION 20201105##

##### Modify the connection information
connection_string="127.0.0.1/dbc,dbc"


##### Modify the condition for the databases and/or objects to include.  
##### You can change the operator 'LIKE ANY' to 'IN' or '=' 
##### Use uppercase names.
include_databases="(UPPER(T1.DATABASENAME) LIKE ANY ('%'))"

##### Modify the condition for the databases to exclude.  
##### Do not use the LIKE ANY in this condition if already used in the previous condition for include_databases
##### Use uppercase names.
exclude_databases="(UPPER(T1.DATABASENAME) NOT IN ('SYS_CALENDAR','ALL','CONSOLE','CRASHDUMPS','DBC','DBCMANAGER','DBCMNGR','DEFAULT','EXTERNAL_AP','EXTUSER','LOCKLOGSHREDDER','PDCRADM','PDCRDATA','PDCRINFO','PUBLIC','SQLJ','SYSADMIN','SYSBAR','SYSJDBC','SYSLIB','SYSSPATIAL','SYSTEMFE','SYSUDTLIB','SYSUIF','SYSDBA','TD_SERVER_DB','TD_SYSFNLIB','TD_SYSFNLIB','TD_SYSGPL','TD_SYSXML','TDMAPS', 'TDPUSER','TDQCD','TDSTATS','TDWM','VIEWPOINT'))"

##### Modify the condition to include specific object names (tables/views/procedures.  
##### You can change the operator 'LIKE ANY' to 'IN' or '=' 
##### Use uppercase names.
include_objects="(UPPER(T1.TABLENAME) LIKE ANY ('%'))"

##### TPT Script Parameters
##### file_size_split_GB parameter tells TPT script at what size to begin separating a table's data into multiple files
##### There is no need to update any of these parameters unless you are planning to extract data from Teradata for loading to Snowflake.
file_size_split_GB="0.2"
tpt_delimiter="\|"  #Certain characters must be escaped with a backslash, such as pipe in order for the sed replace to work properly below
conn_str="127.0.0.1"
conn_usr="dbc"
conn_pwd="dbc"

##### Creates directory for output and log files.
mkdir -p ../log
mkdir -p ../temp
mkdir -p ../output
mkdir -p ../output/object_extracts
mkdir -p ../output/object_extracts/Exports
mkdir -p ../output/object_extracts/Exports/scripts 


##### Updates BTEQ files with the correct list of databases and connection info.



sed -i "s|include_databases|$include_databases|g" ../scripts/create_tpt_script.btq 
sed -i "s|exclude_databases|$exclude_databases|g" ../scripts/create_tpt_script.btq
sed -i "s|include_objects|$include_objects|g" ../scripts/create_tpt_script.btq
sed -i "s|connection_string|$connection_string|g" ../scripts/create_tpt_script.btq
sed -i "s|file_size_split_GB|$file_size_split_GB|g" ../scripts/create_tpt_script.btq
sed -i "s|conn_str|$conn_str|g" ../scripts/create_tpt_script.btq
sed -i "s|conn_usr|$conn_usr|g" ../scripts/create_tpt_script.btq
sed -i "s|conn_pwd|$conn_pwd|g" ../scripts/create_tpt_script.btq
sed -i "s|tpt_delimiter|$tpt_delimiter|g" ../scripts/create_tpt_script.btq


##### Executes Creation of TPT Scripts
echo "Creating TPT Scripts..."
bteq <../scripts/create_tpt_script.btq >../log/create_tpt_script.log 2>&1
sed -i "s|--------------.*--------------||g" ../output/object_extracts/Exports/tpt_export_single_script.tpt
sed -i "s|--------------.*--------------||g" ../output/object_extracts/Exports/tpt_export_multiple_scripts.tpt
sed -i "s|    |\n|g" ../output/object_extracts/Exports/tpt_export_single_script.tpt
sed -i "s|    |\n|g" ../output/object_extracts/Exports/tpt_export_multiple_scripts.tpt
csplit -n 3  -s -f outfile -z ../output/object_extracts/Exports/tpt_export_multiple_scripts.tpt "/**** END JOB ****/+1" "{*}"
mv outfile* ../output/object_extracts/Exports/scripts
sed -i "s|\/\* Begin Script \*\/||g" ../output/object_extracts/Exports/scripts/outfile000;

for file in ../output/object_extracts/Exports/scripts/*
do
  sed -i "s|\/\*\*\*\* BEGIN JOB \*\*\*\*\/||g" $file;
  sed -i '/^[[:space:]]*$/d' $file;
  line=$(head -n 1 $file);
  fname=${line:26};
  mv "$file" "../output/object_extracts/Exports/scripts/${fname}.tpt";  
done
echo "...TPT Script Creation Completed"


##### Commands in the section below will run the consolidated single TPT script generated to 
##### export all table data. Uncomment with caution!!
#mkdir -p ../output/data_extracts
#mkdir -p ../output/data_extracts/lob_files
#echo "Creating Data Files..."
#tbuild -f ../output/object_extracts/Exports/tpt_export_single_script.tpt -C >../log/tpt_export_script.log
#for file in ../output/data_extracts/*
#do 
#	sed -i "s|XZX_EMPTY_XZX *|''|g" $file;
#	sed -i "s|XZX_CHARS_XZX||g" $file;
#done
#echo "...Data File Creation Complete"


##### Commands in the section below will run the individual TPT scripts generated in the section above to 
##### export all table data. Uncomment with caution!!
#mkdir -p ../output/data_extracts
#mkdir -p ../output/data_extracts/lob_files
#echo "Creating Data Files..."
#for file in ../output/object_extracts/Exports/scripts/*
#do
#  line=$(head -n 1 $file);
#  fname=${line:26};
#  tbuild -f $file -C >../log/${fname}.log
#done
#for file in ../output/data_extracts/*
#do 
#	sed -i "s|XZX_EMPTY_XZX *|''|g" $file;
#	sed -i "s|XZX_CHARS_XZX||g" $file;
#done
#echo "...Data File Creation Complete"