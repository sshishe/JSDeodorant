package ca.concordia.javascript.analysis.decomposition;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.javascript.analysis.abstraction.SourceContainer;
import ca.concordia.javascript.analysis.abstraction.StatementProcessor;

import com.google.javascript.jscomp.parsing.parser.trees.FormalParameterListTree;
import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;

public class FunctionDeclarationStatement extends CompositeStatement implements FunctionDeclaration {
	private String name;
	private FunctionKind kind;
	private List<AbstractExpression> parameters;

	public FunctionDeclarationStatement(FunctionDeclarationTree functionDeclarationTree,
			SourceContainer parent) {
		super(functionDeclarationTree, StatementType.FUNCTION_DECLARATION, parent);
		this.parameters = new ArrayList<>();
		if (functionDeclarationTree.name != null)
			this.name = functionDeclarationTree.name.value;

		this.kind = FunctionKind.valueOf(functionDeclarationTree.kind
				.toString());

		if (functionDeclarationTree.formalParameterList != null) {
			FormalParameterListTree formalParametersList = functionDeclarationTree.formalParameterList
					.asFormalParameterList();
			for (ParseTree parameter : formalParametersList.parameters)
				this.addParameter(new AbstractExpression(parameter));
		}
		StatementProcessor.processStatement(functionDeclarationTree.functionBody, this);
	}

	public String getName() {
		return name;
	}

	public FunctionKind getKind() {
		return kind;
	}

	public List<AbstractExpression> getParameters() {
		return parameters;
	}

	private void addParameter(AbstractExpression parameter) {
		this.parameters.add(parameter);
	}

	public FunctionDeclarationTree getFunctionDeclarationTree() {
		return (FunctionDeclarationTree)getStatement();
	}

}
