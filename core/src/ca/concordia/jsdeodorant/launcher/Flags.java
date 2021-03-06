package ca.concordia.jsdeodorant.launcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import ca.concordia.jsdeodorant.analysis.util.FileUtil;

public class Flags {
	static Logger logger = Logger.getLogger(Flags.class.getName());
	private CmdLineParser parser = new CmdLineParser(this);

	@Option(name = "-class-analysis", usage = "Advanceed static analysis")
	private boolean classAnalysis = false;

	@Option(name = "-function-analysis", usage = "Advanceed function analysis")
	private boolean functionAnalysis = false;

	@Option(name = "-output-csv", usage = "Generate a CSV file containing analysis info")
	private boolean outputToCSV = false;

	@Option(name = "-output-db", usage = "Put analysis info into a Postgres DB")
	private boolean outputToDB = false;

	@Option(name = "-calculate-cyclomatic", usage = "Enable calculation of cyclomatic complexity")
	private boolean calculateCyclomatic = false;

	@Option(name = "-module-analysis", usage = "Enable module analysis for Node style packaging")
	private boolean moduleAnalysis = false;

	@Option(name = "-directory-path", usage = "Directory path for javascript project")
	private String directoryPath;

	@Option(name = "-package-system", usage = "Package system including CommonJS and ClosureLibrary")
	private String packageSystem;
	
	@Option(name = "-class-analysis-mode", usage = "Class analysis mode including strict and nonStrict")
	private String classAnalysisMode;

	@Option(name = "-disable-log", usage = "Enable logging mechanism")
	private boolean disableLog = false;

	@Option(name = "-js", usage = "The JavaScript filenames, From Google Closure Flags class", handler = StringArrayOptionHandler.class)
	private List<String> js = new ArrayList<>();

	@Option(name = "-externs", usage = "List of externs files to use in the compilation.", handler = StringArrayOptionHandler.class)
	private List<String> externs = new ArrayList<>();

	@Option(name = "-libraries", usage = "List of libraries to distinguish between production/test codes.", handler = StringArrayOptionHandler.class)
	private List<String> libraries = new ArrayList<>();

	@Option(name = "-analyze-lbClasses", usage = "Analyze libraries to find class usage in them")
	private boolean analyzeLibraryClasses = false;

	//	@Option(name = "-libraries-with-path", usage = "List of libraries with path such as Node's global LB path", handler = StringArrayOptionHandler.class)
	//	private List<String> librariesWithPath = new ArrayList<>();

	@Option(name = "-builtin-libraries", usage = "List of Node's built-in libraries such as Error or Util", handler = StringArrayOptionHandler.class)
	private List<String> builtInLibraries = new ArrayList<>();

	@Option(name = "-psqlServer", usage = "Postgres password")
	private String psqlServer;

	@Option(name = "-psqlPort", usage = "Postgres port")
	private String psqlPort;

	@Option(name = "-psqlDbName", usage = "Postgres database name")
	private String psqlDBName;

	@Option(name = "-psqlUser", usage = "Postgres user")
	private String psqlUser;

	@Option(name = "-psqlPassword", usage = "Postgres password")
	private String psqlPassword;

	@Option(name = "-name", usage = "Project name")
	private String name;

	@Option(name = "-version", usage = "Project version")
	private String version;
	
	@Option(name = "-help", usage = "Print usages")
	private boolean help;

	public boolean classAnalysis() {
		return classAnalysis;
	}

	public boolean functionAnalysis() {
		return functionAnalysis;
	}

	public boolean moduleAnalysis() {
		return moduleAnalysis;
	}

	public boolean outputToCSV() {
		return outputToCSV;
	}

	public boolean outputToDB() {
		return outputToDB;
	}

	public boolean disableLog() {
		return disableLog;
	}

	public String directoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public boolean calculateCyclomatic() {
		return calculateCyclomatic;
	}

	public void clearJS() {
		js.clear();
	}

	public List<String> getJS() throws IOException {
		List<String> filesInDirectory = getFilesInDirectory();
		if (filesInDirectory != null)
			js.addAll(filesInDirectory);
		return js;
	}

	private List<String> getFilesInDirectory() throws FileNotFoundException {
		return FileUtil.getFilesInDirectory(directoryPath, "js");
	}

	public List<String> getExterns() {
		return externs;
	}

	public List<String> getLibraries() {
		return libraries;
	}

	public void setLibraries(List<String> libraries) {
		this.libraries = libraries;
	}

	public boolean analyzeLibraryClasses() {
		return analyzeLibraryClasses;
	}

	//	public List<String> getLibrariesWithPath() {
	//		return librariesWithPath;
	//	}

	public List<String> getBuiltinLibraries() {
		return builtInLibraries;
	}

	public void parse(String[] args) throws CmdLineException {
		getParser().parseArgument(args);
	}

	public String getPsqlServerName() {
		return psqlServer;
	}

	public String getPsqlUser() {
		return psqlUser;
	}

	public String getPsqlPassword() {
		return psqlPassword;
	}

	public String getPsqlDbName() {
		return psqlDBName;
	}

	public String getPsqlPort() {
		return psqlPort;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getPackageSystem() {
		return packageSystem;
	}
	
	public String getClassAnalysisMode() {
		return classAnalysisMode;
	}

	public CmdLineParser getParser() {
		return parser;
	}

	public void setParser(CmdLineParser parser) {
		this.parser = parser;
	}

	public boolean getHelp() {
		return help;
	}
}