package ca.concordia.jsdeodorant.analysis;

import java.io.PrintStream;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.parsing.Config;
import com.google.javascript.rhino.ErrorReporter;

import ca.concordia.jsdeodorant.analysis.util.RhinoErrorReporter;

public class ExtendedCompiler extends Compiler {

	public ExtendedCompiler(PrintStream stream) {
		super(stream);
	}

	public Config getConfig(Config.LanguageMode mode) {
		return super.createConfig(mode);
	}

	public ErrorReporter getDefaultErrorReporter() {
		return RhinoErrorReporter.forOldRhino(this);
	}

}
