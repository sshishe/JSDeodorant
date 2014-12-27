package ca.concordia.javascript.analysis.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.concordia.javascript.analysis.abstraction.AnonymousFunctionDeclaration;
import ca.concordia.javascript.analysis.abstraction.Function;
import ca.concordia.javascript.analysis.abstraction.ObjectCreation;
import ca.concordia.javascript.analysis.abstraction.Program;

public class CompositePostProcessor {
	static Logger log = Logger
			.getLogger(CompositePostProcessor.class.getName());
	static Set<String> allClassNames = new HashSet<>();
	static Set<String> matchedClassNames = new HashSet<>();

	public static void processFunctionDeclarations(Program program) {
		String fileHeader = "Invocation Type, DeclarationType, Number of Params, FunctionType";
		CSVFileWriter.writeToFile("log.csv", fileHeader.split(","));
		for (ObjectCreation objectCreation : program.getObjectCreations()) {
			allClassNames.add(objectCreation.getClassName());
			if (!findPredefinedClasses(program, objectCreation))
				if (!findFunctionDeclaration(program, objectCreation))
					if (!findAnonymousFunctionDeclaration(program, objectCreation))
					{
						StringBuilder unmatchedLog = new StringBuilder(
								objectCreation.getClassName()).append(",")
								.append(objectCreation.getClassName()).append(",")
								.append(objectCreation.getArguments().size()).append(",")
								.append("MATCHNOTFOUND");
						CSVFileWriter.writeToFile("log.csv",
								unmatchedLog.toString().split(","));
					}
		}
	}

	private static boolean findPredefinedClasses(Program program,
			ObjectCreation objectCreation) {
		if (PredefinedJSClasses.contains(objectCreation.getClassName())) {
			matchedClassNames.add(objectCreation.getClassName());
			StringBuilder matchedLog = new StringBuilder(
					objectCreation.getClassName()).append(",")
					.append(objectCreation.getClassName()).append(",")
					.append(objectCreation.getArguments().size()).append(",")
					.append("PREDEFINED");
			CSVFileWriter.writeToFile("log.csv",
					matchedLog.toString().split(","));
			return true;

		}
		return false;
	}

	private static boolean findAnonymousFunctionDeclaration(Program program,
			ObjectCreation objectCreation) {
		for (AnonymousFunctionDeclaration anonymousFunctionDeclaration : program
				.getAnonymousFunctionDeclarations()) {
			if (objectCreation.getClassName() != null)
				if (objectCreation.getClassName().equals(
						anonymousFunctionDeclaration.getName()))
					if (objectCreation.getArguments().size() == anonymousFunctionDeclaration
							.getParameters().size()) {
						objectCreation
								.setFunctionDeclaration(anonymousFunctionDeclaration);
						matchedClassNames.add(objectCreation.getClassName());
						StringBuilder matchedLog = new StringBuilder(
								objectCreation.getClassName())
								.append(",")
								.append(anonymousFunctionDeclaration.getName())
								.append(",")
								.append(anonymousFunctionDeclaration
										.getParameters().size()).append(",")
								.append("EXPRESSION");
						CSVFileWriter.writeToFile("log.csv", matchedLog
								.toString().split(","));
						return true;
					}
		}
		return false;
	}

	private static boolean findFunctionDeclaration(Program program,
			ObjectCreation objectCreation) {
		for (Function functionDeclaration : program.getFunctionDeclarations()) {
			if (objectCreation.getClassName() != null)
				if (objectCreation.getClassName().equals(
						functionDeclaration.getName()))
					if (objectCreation.getArguments().size() == functionDeclaration
							.getParameters().size()) {
						objectCreation
								.setFunctionDeclaration(functionDeclaration);
						matchedClassNames.add(objectCreation.getClassName());
						StringBuilder matchedLog = new StringBuilder(
								objectCreation.getClassName())
								.append(",")
								.append(functionDeclaration.getName())
								.append(",")
								.append(functionDeclaration.getParameters()
										.size()).append(",")
								.append("DECLARATION");

						CSVFileWriter.writeToFile("log.csv", matchedLog
								.toString().split(","));
						return true;
					}
		}
		return false;
	}
}
