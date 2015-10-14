package ca.concordia.javascript.launcher;

import java.io.IOException;
import java.util.ArrayList;

import ca.concordia.javascript.analysis.AnalysisOptions;
import ca.concordia.javascript.analysis.abstraction.Package;

public class TestRunner extends Runner {
	public TestRunner() {
		this(new String[0]);
	}

	protected TestRunner(String[] args) {
		super(args);
		createOptions();
		createAnalysisOptions();
		getAnalysisOptions().setExterns(new ArrayList<String>());
	}

	public void setAdvancedAnalysis(boolean state) {
		getAnalysisOptions().setAdvancedAnalysis(state);
	}

	public void setJsFile(String jsFilej) {
		getAnalysisOptions().setJsFile(jsFilej);
	}

	@Override
	public AnalysisOptions createAnalysisOptions() {
		setAnalysisOptions(new AnalysisOptions());
		getAnalysisOptions().setAdvancedAnalysis(true);
		getAnalysisOptions().setCalculateCyclomatic(false);
		getAnalysisOptions().setOutputToCSV(false);
		getAnalysisOptions().setLogDisabled(true);
		return getAnalysisOptions();
	}

	public Package performActionsForTest() {
		try {
			return super.performActions().get(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
