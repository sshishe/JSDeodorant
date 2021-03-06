package ca.concordia.jsdeodorant.analysis.decomposition;

import java.util.EnumSet;

import com.google.javascript.jscomp.parsing.parser.trees.FunctionDeclarationTree;

public class Method extends TypeMember{
	
	private EnumSet<MethodType> kinds;
	
	public Method(String name, TypeDeclaration owner,FunctionDeclarationTree functionDeclarationTree,//FunctionDeclaration functionDeclaration,
			EnumSet<MethodType> kinds) {

		super(name, owner, functionDeclarationTree);
		this.kinds = kinds;
	}

	public EnumSet<MethodType> getKinds() {
		return kinds;
	}
}
