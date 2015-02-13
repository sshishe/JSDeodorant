package ca.concordia.javascript.analysis.abstraction;

import java.util.List;

import com.google.javascript.jscomp.parsing.parser.trees.CallExpressionTree;

import ca.concordia.javascript.analysis.decomposition.AbstractExpression;
import ca.concordia.javascript.analysis.util.SourceHelper;

public class FunctionInvocation {
	private CallExpressionTree callExpressionTree;
	private String memberName;
	private AbstractExpression operand;
	private List<AbstractExpression> arguments;

	public FunctionInvocation(CallExpressionTree callExpressionTree,
			String memberName, AbstractExpression operand,
			List<AbstractExpression> arguments) {
		this.callExpressionTree = callExpressionTree;
		this.memberName = memberName;
		this.operand = operand;
		this.arguments = arguments;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public AbstractExpression getOperand() {
		return operand;
	}

	public void setOperand(AbstractExpression operand) {
		this.operand = operand;
	}

	public List<AbstractExpression> getArguments() {
		return arguments;
	}

	public void setArguments(List<AbstractExpression> arguments) {
		this.arguments = arguments;
	}

	public String toString() {
		return SourceHelper.extract(callExpressionTree);
	}
}
