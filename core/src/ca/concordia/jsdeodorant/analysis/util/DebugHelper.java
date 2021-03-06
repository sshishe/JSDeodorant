package ca.concordia.jsdeodorant.analysis.util;

import com.google.javascript.jscomp.parsing.parser.trees.ParseTree;
import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;

public class DebugHelper {
	public static String extract(ParseTree node) {
		SourcePosition startPosition = node.location.start;
		SourcePosition endPosition = node.location.end;

		return node.location.start.source.contents.substring(
				startPosition.offset, endPosition.offset);
	}
}
