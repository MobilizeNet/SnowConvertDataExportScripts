package mobilize.snowconvert.oracle_export;

public class SchemaImportInfo 
{
    public String SchemaName;
    public String SchemaFilter;
    public String TableFilter;

    public SchemaImportInfo(String schemaName, String schemaFilter, String tableFilter) {
        this.SchemaName = schemaName;
        this.SchemaFilter = schemaFilter;
        this.TableFilter = tableFilter;
    }
}