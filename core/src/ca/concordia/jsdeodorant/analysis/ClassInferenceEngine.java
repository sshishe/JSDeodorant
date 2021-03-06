package ca.concordia.jsdeodorant.analysis;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.javascript.jscomp.parsing.parser.Token;
import com.google.javascript.jscomp.parsing.parser.trees.BinaryOperatorTree;
import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ObjectLiteralExpressionTree;
import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

import ca.concordia.jsdeodorant.analysis.abstraction.AbstractIdentifier;
import ca.concordia.jsdeodorant.analysis.abstraction.CompositeIdentifier;
import ca.concordia.jsdeodorant.analysis.abstraction.Module;
import ca.concordia.jsdeodorant.analysis.abstraction.ObjectCreation;
import ca.concordia.jsdeodorant.analysis.abstraction.Program;
import ca.concordia.jsdeodorant.analysis.abstraction.SourceContainer;
import ca.concordia.jsdeodorant.analysis.decomposition.AbstractExpression;
import ca.concordia.jsdeodorant.analysis.decomposition.AbstractFunctionFragment;
import ca.concordia.jsdeodorant.analysis.decomposition.AbstractStatement;
import ca.concordia.jsdeodorant.analysis.decomposition.TypeDeclaration;
import ca.concordia.jsdeodorant.analysis.decomposition.CompositeStatement;
import ca.concordia.jsdeodorant.analysis.decomposition.FunctionDeclaration;
import ca.concordia.jsdeodorant.analysis.decomposition.FunctionDeclarationExpression;
import ca.concordia.jsdeodorant.analysis.decomposition.FunctionDeclarationExpressionNature;
import ca.concordia.jsdeodorant.analysis.decomposition.FunctionDeclarationStatement;
import ca.concordia.jsdeodorant.analysis.decomposition.ObjectLiteralExpression;
import ca.concordia.jsdeodorant.analysis.util.IdentifierHelper;
import ca.concordia.jsdeodorant.analysis.util.StringUtil;

public class ClassInferenceEngine {
	static Logger log = Logger.getLogger(ClassInferenceEngine.class.getName());

	public static void run(Module module) {
		for (FunctionDeclaration functionDeclaration : module.getProgram().getFunctionDeclarationList()) {
			if (functionDeclaration.isTypeDeclaration())
				continue;

			if (functionDeclaration instanceof FunctionDeclarationExpression) {
				FunctionDeclarationExpression functionDeclarationExpression = (FunctionDeclarationExpression) functionDeclaration;
				if (functionDeclarationExpression.getFunctionDeclarationExpressionNature() == FunctionDeclarationExpressionNature.IIFE)
					continue;
			}

			assignedMethodsOrAttributesInsideClassBody(module, functionDeclaration);

			// angular.scenario.MyClass = ...
			assignedClassToACompositeNameWithPropsAndMethods(module, functionDeclaration);

			// function MockSpecRunner() {}
			// MockSpecRunner.prototype.run = function(spec, specDone) { ... }
			assignedMethodToProto(module, functionDeclaration);

			assignedMethodToCompositeNameOutsideBody(module, functionDeclaration);

			assignObjectLiteralToPrototype(module, functionDeclaration);
		}

		nowSetClassesToNotFoundByObjectCreations(module);
	}

	private static void assignedMethodToCompositeNameOutsideBody(Module module, FunctionDeclaration functionDeclaration) {
		String functionName = null;
		if (functionDeclaration.getRawIdentifier() != null)
			//			if (functionDeclaration.getRawIdentifier() instanceof CompositeIdentifier)
			//				/functionName = functionDeclaration.getRawIdentifier().asCompositeIdentifier().getMostLeftPart().toString();
			//			else
			functionName = functionDeclaration.getRawIdentifier().toString();
		else
			functionName = functionDeclaration.getName();

		List<AbstractExpression> assignments = null;
		CompositeStatement parent = null;
		if (functionDeclaration instanceof FunctionDeclarationStatement) {
			AbstractStatement composite = (CompositeStatement) functionDeclaration;
			if (composite.getParent() instanceof Program) {
				Program program = (Program) composite.getParent();
				assignments = program.getAssignmentExpressionList();
			} else if (composite.getParent() instanceof CompositeStatement)
				parent = (CompositeStatement) composite.getParent();
			else
				return;
		} else if (functionDeclaration instanceof FunctionDeclarationExpression) {
			AbstractExpression abstractExpression = (AbstractExpression) functionDeclaration;
			if (abstractExpression.getParent() instanceof Program) {
				Program program = (Program) abstractExpression.getParent();
				assignments = program.getAssignmentExpressionList();
			} else if (abstractExpression.getParent() instanceof CompositeStatement)
				parent = (CompositeStatement) abstractExpression.getParent();
			else
				return;
		}
		if (assignments == null)
			assignments = parent.getAssignmentExpressionList();

		for (AbstractExpression assignmentExpression : assignments) {
			if (assignmentExpression.getExpression() instanceof BinaryOperatorTree) {
				BinaryOperatorTree binaryOperatorTree = assignmentExpression.getExpression().asBinaryOperator();
				AbstractIdentifier left = IdentifierHelper.getIdentifier(binaryOperatorTree.left);

				if (left instanceof CompositeIdentifier)
					if (functionDeclaration instanceof AbstractFunctionFragment) {
						if (binaryOperatorTree.right instanceof FunctionDeclarationTree)
							if (left.toString().contains(functionName + ".prototype")) {
								module.createTypeDeclaration(functionDeclaration.getRawIdentifier(), functionDeclaration, true, false);
								break;
							} else if (left.asCompositeIdentifier().getMostLeftPart().toString().contains(functionName)) {
								module.createTypeDeclaration(functionDeclaration.getRawIdentifier(), functionDeclaration, true, false);
								break;
							}

					}
			}
		}
	}

	private static void nowSetClassesToNotFoundByObjectCreations(Module module) {
		for (ObjectCreation objectCreation : module.getProgram().getObjectCreationList()) {
			if (objectCreation.getClassDeclaration() != null)
				for (TypeDeclaration classDeclaration : module.getTypes()) {
					if (objectCreation.getIdentifier().equals(classDeclaration.getName())) {
						objectCreation.setClassDeclaration(classDeclaration, module);
						classDeclaration.setMatchedAfterInference(true);
					}
				}
		}
	}

	private static void assignObjectLiteralToPrototype(Module module, FunctionDeclaration functionDeclaration) {
		AbstractFunctionFragment abstractFunctionFragment = (AbstractFunctionFragment) functionDeclaration;
		SourceContainer parent = abstractFunctionFragment.getParent();
		List<AbstractExpression> assignmentList = null;
		if (parent instanceof Program) {
			Program program = (Program) parent;
			assignmentList = program.getAssignmentExpressionList();
		} else if (parent instanceof CompositeStatement) {
			CompositeStatement compositeParent = (CompositeStatement) parent;
			assignmentList = compositeParent.getAssignmentExpressionList();
		}
		if (assignmentList == null)
			return;

		for (AbstractExpression assignmentExpression : assignmentList) {
			if (assignmentExpression.getExpression() instanceof BinaryOperatorTree) {
				BinaryOperatorTree binaryOperatorTree = assignmentExpression.getExpression().asBinaryOperator();
				AbstractIdentifier left = IdentifierHelper.getIdentifier(binaryOperatorTree.left);
				if (left instanceof CompositeIdentifier) {
					if (functionDeclaration.getName().equals(left.asCompositeIdentifier().getLeftPart().toString()))
						if (((CompositeIdentifier) left).getMostRightPart().toString().contains("prototype")) {
							module.createTypeDeclaration(functionDeclaration.getRawIdentifier(), functionDeclaration, true, false);
						}
				}
			}
		}
	}

	private static void assignedMethodToProto(Module module, FunctionDeclaration functionDeclaration) {
		for (AbstractExpression assignmentExpression : functionDeclaration.getAssignments()) {
			if (assignmentExpression.getExpression() instanceof BinaryOperatorTree) {
				BinaryOperatorTree binaryOperatorTree = assignmentExpression.getExpression().asBinaryOperator();
				AbstractIdentifier left = IdentifierHelper.getIdentifier(binaryOperatorTree.left);
				if (left instanceof CompositeIdentifier)
					if (left.asCompositeIdentifier().toString().contains("this.")) {
						if (functionDeclaration instanceof AbstractFunctionFragment) {
							if (functionDeclaration.getRawIdentifier() instanceof CompositeIdentifier) {
								CompositeIdentifier compositeIdentifier = functionDeclaration.getRawIdentifier().asCompositeIdentifier();
								if (Character.isUpperCase(compositeIdentifier.getMostRightPart().toString().charAt(0))) {
									module.createTypeDeclaration(functionDeclaration.getRawIdentifier(), functionDeclaration, true, false);
									break;
								}
							}
							AbstractFunctionFragment abstractFunctionFragment = (AbstractFunctionFragment) functionDeclaration;
							if (abstractFunctionFragment.getParent() instanceof ObjectLiteralExpression) {
								ObjectLiteralExpression objectLiteral = (ObjectLiteralExpression) abstractFunctionFragment.getParent();
								AbstractIdentifier identifier = objectLiteral.getIdentifier();
								if (identifier instanceof CompositeIdentifier) {
									if (identifier.asCompositeIdentifier().getMostRightPart().toString().equals("prototype")) {
										for (FunctionDeclaration functionToBeMatched : module.getProgram().getFunctionDeclarationList()) {
											if (functionToBeMatched.getIdentifier() != null)
												if (functionToBeMatched.getIdentifier().toString().equals(((CompositeIdentifier) identifier).getMostLeftPart().toString())) {
													module.createTypeDeclaration(functionToBeMatched.getRawIdentifier(), functionToBeMatched, true, false);
													break;
												}
										}
									}
								}
							}
						}
					}
			}
		}
	}

	private static void assignedMethodsOrAttributesInsideClassBody(Module module, FunctionDeclaration functionDeclaration) {
		for (AbstractExpression assignmentExpression : functionDeclaration.getAssignments()) {
			if (assignmentExpression.getExpression() instanceof BinaryOperatorTree) {
				BinaryOperatorTree binaryOperatorTree = assignmentExpression.getExpression().asBinaryOperator();
				AbstractIdentifier left = IdentifierHelper.getIdentifier(binaryOperatorTree.left);
				if (left instanceof CompositeIdentifier) {
					if (left.asCompositeIdentifier().toString().contains("this.")) {
						if (checkIfFunctionNameIsCapitalize(functionDeclaration)) {
							//log.warn(functionDeclaration.getIdentifier());
							module.createTypeDeclaration(functionDeclaration.getRawIdentifier(), functionDeclaration, true, false);
						}
					}
				}
			}
		}

	}

	private static void assignedClassToACompositeNameWithPropsAndMethods(Module module, FunctionDeclaration functionDeclaration) {
		if (functionDeclaration.getRawIdentifier() != null)
			if (functionDeclaration.getRawIdentifier().toString().contains("prototype")) {
				//	log.warn(functionDeclaration.getRawIdentifier().toString());
				if (functionDeclaration.getRawIdentifier() instanceof CompositeIdentifier) {
					FunctionDeclaration closestFunctionToBeMatched = null;
					for (FunctionDeclaration functionToBeMatched : module.getProgram().getFunctionDeclarationList()) {
						if (functionToBeMatched.getIdentifier() != null) {
							if (functionToBeMatched.getIdentifier().toString().equals(functionDeclaration.getRawIdentifier().asCompositeIdentifier().getMostLeftPart().toString())) {
								//	if (checkIfFunctionNameIsCapitalize(functionDeclaration)) {
								closestFunctionToBeMatched = functionToBeMatched;
								//	}
							}
						}
					}
					if (closestFunctionToBeMatched != null)
						module.createTypeDeclaration(closestFunctionToBeMatched.getRawIdentifier(), closestFunctionToBeMatched, true, false);
				}
			}
	}

	private static boolean checkIfFunctionNameIsCapitalize(FunctionDeclaration function) {
		if (function.getIdentifier() != null)
			if (!StringUtil.isNullOrEmpty(function.getIdentifier().toString()))
				if (function.getIdentifier() instanceof CompositeIdentifier) {
					if (Character.isUpperCase(function.getIdentifier().asCompositeIdentifier().getMostRightPart().toString().charAt(0)))
						return true;
				} else if (Character.isUpperCase(function.getIdentifier().toString().charAt(0)))
					return true;
		return false;
	}

	public static void analyzeMethodsAndAttributes(Module module) {
		for (TypeDeclaration classDeclaration : module.getTypes()) {
			analyzeMethodsAndAttributes(classDeclaration, module);
		}
	}

	private static void analyzeMethodsAndAttributes(TypeDeclaration classDeclaration, Module module) {
		FunctionDeclaration functionDeclaration = classDeclaration.getFunctionDeclaration();
		// Lookup for attributes and methods inside function
		for (AbstractExpression assignmentExpression : functionDeclaration.getAssignments()) {
			if (assignmentExpression.getExpression() instanceof BinaryOperatorTree) {
				BinaryOperatorTree binaryOperatorTree = assignmentExpression.getExpression().asBinaryOperator();
				AbstractIdentifier left = IdentifierHelper.getIdentifier(binaryOperatorTree.left);
				if (left instanceof CompositeIdentifier) {
					if (left.asCompositeIdentifier().toString().contains("this.")) {
						if (binaryOperatorTree.right instanceof FunctionDeclarationTree) {
							// Then, it's method
							// since we have a collection of ClassMember now
							//classDeclaration.addToAllMethod(left.asCompositeIdentifier().getRightPart().toString(), assignmentExpression, 0);
						} else {
							// It's attribute
							// since we have a collection of ClassMember now
							//classDeclaration.addAttribtue(left.asCompositeIdentifier().getRightPart().toString(), assignmentExpression);
						}
					}
				}
			}
		}

		AbstractFunctionFragment abstractFunctionFragment = (AbstractFunctionFragment) functionDeclaration;
		SourceContainer parent = abstractFunctionFragment.getParent();
		List<AbstractExpression> assignmentList = null;
		List<ObjectLiteralExpression> objectLiteralExpressionList = null;
		if (parent instanceof Program) {
			Program program = (Program) parent;
			assignmentList = program.getAssignmentExpressionList();
			objectLiteralExpressionList = program.getObjectLiteralList();
		} else if (parent instanceof CompositeStatement) {
			CompositeStatement compositeParent = (CompositeStatement) parent;
			assignmentList = compositeParent.getAssignmentExpressionList();
			objectLiteralExpressionList = compositeParent.getObjectLiteralList();
		}

		if (assignmentList == null)
			return;

		for (AbstractExpression assignmentExpression : assignmentList) {
			if (assignmentExpression.getExpression() instanceof BinaryOperatorTree) {
				BinaryOperatorTree binaryOperatorTree = assignmentExpression.getExpression().asBinaryOperator();
				AbstractIdentifier left = IdentifierHelper.getIdentifier(binaryOperatorTree.left);
				if (left instanceof CompositeIdentifier) {
					if (left.asCompositeIdentifier().toString().contains(functionDeclaration.getName()) && left.asCompositeIdentifier().toString().contains(".prototype")) {
						if (binaryOperatorTree.right instanceof ObjectLiteralExpressionTree) {
							ObjectLiteralExpressionTree objectLiteralExpression = binaryOperatorTree.right.asObjectLiteralExpression();
							for (ObjectLiteralExpression objExpression : objectLiteralExpressionList) {
								if (objExpression.getExpression().equals(objectLiteralExpression)) {
									extractMethodsFromObjectLiteral(objExpression, classDeclaration, module);
								}
							}
						} else if (binaryOperatorTree.right instanceof FunctionDeclarationTree) {
							// Then, it's method
							// since we have a collection of ClassMember now
							//classDeclaration.addToAllMethod(left.asCompositeIdentifier().getMostRightPart().toString(), assignmentExpression, calculateLinesOfCodes(binaryOperatorTree.right.asFunctionDeclaration().functionBody.location));
						} else {
							// It's attribute
							//classDeclaration.addAttribtue(left.asCompositeIdentifier().getRightPart().toString(), assignmentExpression);
						}
					}
				}
			}
		}

		if (abstractFunctionFragment.getParent() instanceof ObjectLiteralExpression) {
			ObjectLiteralExpression objectLiteral = (ObjectLiteralExpression) abstractFunctionFragment.getParent();
			AbstractIdentifier identifier = objectLiteral.getIdentifier();
			if (identifier instanceof CompositeIdentifier) {
				if (identifier.asCompositeIdentifier().getMostRightPart().toString().equals("prototype")) {
					for (FunctionDeclaration functionToBeMatched : module.getProgram().getFunctionDeclarationList()) {
						if (functionToBeMatched.getIdentifier() != null)
							if (functionToBeMatched.getIdentifier().toString().equals(((CompositeIdentifier) identifier).getMostLeftPart().toString())) {
								functionToBeMatched.setClassDeclaration(true);
								break;
							}
					}
				}
			}
		}

		// Lookup for attributes and methods outside function but not assigned to prototype

		List<AbstractExpression> assignments = null;
		CompositeStatement parentContainer = null;
		if (functionDeclaration instanceof FunctionDeclarationStatement) {
			AbstractStatement composite = (CompositeStatement) functionDeclaration;
			if (composite.getParent() instanceof Program) {
				Program program = (Program) composite.getParent();
				assignments = program.getAssignmentExpressionList();
			} else if (composite.getParent() instanceof CompositeStatement)
				parentContainer = (CompositeStatement) composite.getParent();
			else
				return;
		} else if (functionDeclaration instanceof FunctionDeclarationExpression) {
			AbstractExpression abstractExpression = (AbstractExpression) functionDeclaration;
			if (abstractExpression.getParent() instanceof Program) {
				Program program = (Program) abstractExpression.getParent();
				assignments = program.getAssignmentExpressionList();
			} else if (abstractExpression.getParent() instanceof CompositeStatement)
				parentContainer = (CompositeStatement) abstractExpression.getParent();
			else
				return;
		}
		if (assignments == null)
			assignments = parentContainer.getAssignmentExpressionList();

		for (AbstractExpression assignmentExpression : assignments) {
			if (assignmentExpression.getExpression() instanceof BinaryOperatorTree) {
				BinaryOperatorTree binaryOperatorTree = assignmentExpression.getExpression().asBinaryOperator();
				AbstractIdentifier left = IdentifierHelper.getIdentifier(binaryOperatorTree.left);
				if (left instanceof CompositeIdentifier) {
					if (left.asCompositeIdentifier().getMostLeftPart().toString().equals(functionDeclaration.getName())) {
						if (binaryOperatorTree.right instanceof FunctionDeclarationTree) {
							// Then, it's method
							// since we have a collection of ClassMember now
							//classDeclaration.addToAllMethod(left.asCompositeIdentifier().getRightPart().toString(), assignmentExpression, 0);
						} else {
							// It's attribute
							// since we have a collection of ClassMember now
							//classDeclaration.addAttribtue(left.asCompositeIdentifier().getRightPart().toString(), assignmentExpression);
						}
					}
				}
			}
		}
	}

	private static int calculateLinesOfCodes(SourceRange sourceRange) {
		if (sourceRange.start.line == sourceRange.end.line)
			return 1;
		return sourceRange.end.line - sourceRange.start.line - 1;
	}

	private static void extractMethodsFromObjectLiteral(ObjectLiteralExpression objExpression, TypeDeclaration classDeclaration, Module module) {
		Map<Token, AbstractExpression> propertyMap = objExpression.getPropertyMap();
		for (Token key : propertyMap.keySet()) {
			AbstractExpression value = propertyMap.get(key);
			if (value.getExpression() instanceof FunctionDeclarationTree) {
				// since we have a collection of ClassMember now
				//classDeclaration.addToAllMethod(key.toString(), value, 0);
			}
		}

	}
}
