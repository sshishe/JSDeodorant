package ca.concordia.jsdeodorant.analysis.abstraction;

import com.google.javascript.jscomp.parsing.parser.Token;
import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;

public class CompositeIdentifier extends AbstractIdentifier {
	private volatile int hashCode = 0;
	private AbstractIdentifier rightPart;

	public CompositeIdentifier(Token token) {
		super(token);
	}

	public CompositeIdentifier(ParseTree node, AbstractIdentifier rightPart) {
		super(node);
		this.rightPart = rightPart;
	}

	public CompositeIdentifier(Token token, AbstractIdentifier rightPart) {
		super(token);
		this.rightPart = rightPart;
	}

	public CompositeIdentifier(AbstractIdentifier part, AbstractIdentifier rightPart) {
		super(part);
		this.rightPart = rightPart;
	}

	// if composite identifier is "one.two.three" then right part is "two.three"
	public AbstractIdentifier getRightPart() {
		return rightPart;
	}

	public void setRightPart(AbstractIdentifier rightPart) {
		this.rightPart = rightPart;
	}

	// if composite identifier is "one.two.three" then left part is "one.two"
	public AbstractIdentifier getLeftPart() {
		return getLeftPart(rightPart);
	}

	private AbstractIdentifier getLeftPart(AbstractIdentifier rightPartParameter) {
		if (rightPartParameter instanceof PlainIdentifier) {
			return new PlainIdentifier(getNode());
		} else {
			CompositeIdentifier compositeVariable = (CompositeIdentifier) rightPartParameter;
			return new CompositeIdentifier(getNode(), compositeVariable.getLeftPart());
		}
	}

	public PlainIdentifier getMostLeftPart() {
		if (rightPart instanceof PlainIdentifier) {
			return new PlainIdentifier(getNode());
		} else {
			AbstractIdentifier left = getLeftPart();
			while (!(left instanceof PlainIdentifier)) {
				left = getLeftPart(((CompositeIdentifier) left).rightPart);
				if (left instanceof PlainIdentifier)
					return new PlainIdentifier(getNode());
			}
		}
		return null;
	}

	public PlainIdentifier getMostRightPart() {
		if (rightPart instanceof PlainIdentifier) {
			return new PlainIdentifier(rightPart.getNode());
		} else {
			AbstractIdentifier right = getRightPart();
			while (!(right instanceof PlainIdentifier)) {
				right = ((CompositeIdentifier) right).rightPart;
				if (right instanceof PlainIdentifier)
					return new PlainIdentifier(right.getNode());
			}
		}
		return null;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof CompositeIdentifier) {
			CompositeIdentifier composite = (CompositeIdentifier) o;
			return this.identifierName.equals(composite.identifierName) && this.rightPart.toString().equals(composite.rightPart.toString());
		}
		return false;
	}

	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			if (getNode() != null)
				result = 31 * result + getNode().hashCode();
			else
				result = 31 * result + token.hashCode();
			hashCode = result;
		}
		return hashCode;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(identifierName);
		sb.append(".");
		sb.append(rightPart.toString());
		return sb.toString();
	}
}
