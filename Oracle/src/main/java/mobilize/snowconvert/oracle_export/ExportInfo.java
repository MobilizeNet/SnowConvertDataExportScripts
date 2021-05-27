package mobilize.snowconvert.oracle_export;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExportInfo {
    public String SID = null;
    public String serviceName;
    public String host;
    public String port;
    public String user;
    public String password;
    public String description;
    public String command;
    public Path   workDir;
    public Path   dumpDir;
    public List<SchemaImportInfo> Schemas;
    public int procceses;

    public ExportInfo()
    {
        this.Schemas = new ArrayList<SchemaImportInfo>();
    }
}