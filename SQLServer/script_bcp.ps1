
#Load Configurations
$ConfigList = @()
$newstreamreader = New-Object System.IO.StreamReader(".\Parameters.conf")
$eachlinenumber = 1
while (($readeachline =$newstreamreader.ReadLine()) -ne $null)
{
	$ConfigList += $readeachline
    $eachlinenumber++
}
$newstreamreader.Dispose()
$server             = $ConfigList[0]
$database           = $ConfigList[1]
$separated_by       = $ConfigList[2]
$run_once_generated = $ConfigList[3]
$SavePath           = $ConfigList[4]
$SchemasSelected    = $ConfigList[5]
$RowsPerFile        = $ConfigList[6]


#Functions
$Logfile = "$($SavePath)\CreateScriptLog.log"
Function LogWrite
{
   Param ([string]$logstring)
   Add-content $Logfile -value $logstring
}

$ExtractScript = "$($SavePath)\Extract.ps1"
Function Extract
{
   Param ([string]$Extracttring)
   Add-content $ExtractScript -value $Extracttring
}

#Parameters
if ($SchemasSelected -eq "") {
	$tablequery = "SELECT schemas.name as schemaName, tables.name as tableName from sys.tables inner join sys.schemas ON tables.schema_id = schemas.schema_id"
} else {
	$SchemasSelected = $SchemasSelected -replace ",", "','"
	$tablequery = "SELECT schemas.name as schemaName, tables.name as tableName from sys.tables inner join sys.schemas ON tables.schema_id = schemas.schema_id where schemas.name in ('$($SchemasSelected)')"
}

#Delcare Connection Variables
$connectionTemplate = "Data Source={0};Integrated Security=SSPI;Initial Catalog={1};"
$connectionString = [string]::Format($connectionTemplate, $server, $database)
$connection = New-Object System.Data.SqlClient.SqlConnection
$connection.ConnectionString = $connectionString

#Declare Command
$command = New-Object System.Data.SqlClient.SqlCommand
$command.CommandTimeout=0
$command.CommandText = $tablequery
$command.Connection = $connection

#Load up the Tables in a dataset
$SqlAdapter = New-Object System.Data.SqlClient.SqlDataAdapter
$SqlAdapter.SelectCommand = $command
$DataSet = New-Object System.Data.DataSet
$SqlAdapter.Fill($DataSet)
$connection.Close()


# Loop through all tables and export a CSV of the Table Data
foreach ($Row in $DataSet.Tables[0].Rows)
{
	LogWrite "********************************************************************"
	$StartMs = (Get-Date)
	$ExtractScript = "$($SavePath)\Extract_$($Row[0])_$($Row[1]).ps1"
	LogWrite "Creating file for [$($Row[0])].[$($Row[1])]..."
	try {
		#Specify the output location of your dump file
		#----------------------------------------------------------------------------------------------------
		Extract "$([char]36)RowsPerFile = $($RowsPerFile)"
		Extract "$([char]36)Logfile = ""$($SavePath)\ScriptLog.csv"""
		Extract "Function LogWrite"
		Extract "{"
		Extract "   Param ([string]$([char]36)logstring)"
		Extract "   Add-content $([char]36)Logfile -value $([char]36)logstring"
		Extract "}"
		Extract "$([char]36)ErrorFile = ""$($SavePath)\ErrorLog.csv"""
		Extract "Function LogError"
		Extract "{"
		Extract "   Param ([string]$([char]36)errorstring)"
		Extract "   Add-content $([char]36)ErrorFile -value $([char]36)errorstring"
		Extract "}"
		Extract "#Query"
		Extract "$([char]36)server     = ""$($server)"""
		Extract "$([char]36)database   = ""$($database)"""
		Extract "$([char]36)tablequery = ""SELECT count(*) from $($database).$($Row[0]).$($Row[1])"""
		Extract "#Delcare Connection Variables"
		Extract "$([char]36)connectionTemplate = ""Data Source={0};Integrated Security=SSPI;Initial Catalog={1};"""
		Extract "$([char]36)connectionString = [string]::Format($([char]36)connectionTemplate, $([char]36)server, $([char]36)database)"
		Extract "$([char]36)connection = New-Object System.Data.SqlClient.SqlConnection"
		Extract "$([char]36)connection.ConnectionString = $([char]36)connectionString"
		Extract "#Declare Command"
		Extract "$([char]36)command = New-Object System.Data.SqlClient.SqlCommand"
		Extract "$([char]36)command.CommandTimeout=0"
		Extract "$([char]36)command.CommandText = $([char]36)tablequery"
		Extract "$([char]36)command.Connection = $([char]36)connection"
		Extract "#Load up the Tables in a dataset"
		Extract "$([char]36)SqlAdapter = New-Object System.Data.SqlClient.SqlDataAdapter"
		Extract "$([char]36)SqlAdapter.SelectCommand = $([char]36)command"
		Extract "$([char]36)DataSet = New-Object System.Data.DataSet"
		Extract "$([char]36)SqlAdapter.Fill($([char]36)DataSet)"
		Extract "$([char]36)connection.Close()"
		Extract "$([char]36)Rows = $([char]36)DataSet.Tables[0].Rows[0][0]"
		Extract "if ($([char]36)Rows -gt $([char]36)RowsPerFile) {"
		Extract "	$([char]36)NumFiles = $([char]36)Rows/$([char]36)RowsPerFile"
		Extract "	$([char]36)i = 0"
		Extract "	DO {"
		Extract "		$([char]36)First = $([char]36)i*$([char]36)RowsPerFile+1"
		Extract "		$([char]36)Last = ($([char]36)i+1)*$([char]36)RowsPerFile"
		Extract "		$([char]36)CommandCP = ""/c """"bcp $($database).$($Row[0]).$($Row[1]) out $($SavePath)\$($Row[0])_$($Row[1])_file$([char]36)($([char]36)i).csv -F $([char]36)($([char]36)First) -L $([char]36)($([char]36)Last) -S$($server) -k -c  -t""""$($separated_by)"""" -T"""""""
		Extract "		try {"
		Extract "			$([char]36)StartMs = (Get-Date)"
		Extract "			Start-Process -NoNewWindow -Wait -FilePath ""cmd.exe"" -ArgumentList $([char]36)CommandCP"
		Extract "			$([char]36)EndMs = (Get-Date)"
		Extract "			LogWrite ""$($database),$($Row[0]),$($Row[1]),$([char]36)($([char]36)EndMs - $([char]36)StartMs),file$([char]36)($([char]36)i)"""
		Extract "		} catch {"
		Extract "			LogError ""$($database),$($Row[0]),$($Row[1]),$([char]36)($([char]36)_.Exception.Message),file$([char]36)($([char]36)i)"""
		Extract "		}"
		Extract "		$([char]36)i=$([char]36)i+1"
		Extract "	} While ($([char]36)i -lt $([char]36)NumFiles)"
		Extract "} else {"
		Extract "	try {"
		Extract "		$([char]36)StartMs = (Get-Date)"
		Extract "		Start-Process -NoNewWindow -Wait -FilePath ""cmd.exe"" -ArgumentList '/c ""bcp $($database).$($Row[0]).$($Row[1]) out $($SavePath)\$($Row[0])_$($Row[1]).csv -S$($server) -k -c  -t""""$($separated_by)"""" -T""'"
		Extract "		$([char]36)EndMs = (Get-Date)"
		Extract "		LogWrite ""$($database),$($Row[0]),$($Row[1]),$([char]36)($([char]36)EndMs - $([char]36)StartMs)"""
		Extract "	} catch {"
		Extract "		LogError ""$($database),$($Row[0]),$($Row[1]),$([char]36)($([char]36)_.Exception.Message)"""
		Extract "	}"
		Extract "}"
		#----------------------------------------------------------------------------------------------------
		
		if ($run_once_generated -eq "S") {
			try {
				Start-Process powershell.exe "& '$($ExtractScript)'" 
			} catch {
				LogWrite $_.Exception.Message
			}
		}
		
	} catch {
		LogWrite $_.Exception.Message
	}
	$EndMs = (Get-Date)
	LogWrite "It took $($EndMs - $StartMs) to run"
	LogWrite "********************************************************************"
	LogWrite ""
}