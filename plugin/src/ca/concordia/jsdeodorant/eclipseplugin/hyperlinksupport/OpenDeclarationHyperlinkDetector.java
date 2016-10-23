package ca.concordia.jsdeodorant.eclipseplugin.hyperlinksupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.IEditorPart;

import com.google.javascript.jscomp.parsing.parser.util.SourceRange;

import ca.concordia.jsdeodorant.analysis.abstraction.Dependency;
import ca.concordia.jsdeodorant.analysis.abstraction.Module;
import ca.concordia.jsdeodorant.analysis.abstraction.ObjectCreation;
import ca.concordia.jsdeodorant.analysis.decomposition.ClassDeclaration;
import ca.concordia.jsdeodorant.eclipseplugin.util.ModulesInfo;
import ca.concordia.jsdeodorant.eclipseplugin.util.OpenAndAnnotateHelper;

public class OpenDeclarationHyperlinkDetector implements IHyperlinkDetector  {  

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViwer, IRegion region, boolean canHaveMultiple) {
		List<Module> modules = ModulesInfo.getModuleInfo();
		if (region != null && modules.size() > 0) {
			IEditorPart activeEditor = OpenAndAnnotateHelper.getActiveEditor();
				if (activeEditor != null) {
					ITextOperationTarget target = (ITextOperationTarget)activeEditor.getAdapter(ITextOperationTarget.class);
					if (target instanceof ITextViewer) {
						ITextViewer activeTextViewer = (ITextViewer)target;
						if (activeTextViewer == textViwer) {
							List<IHyperlink> hyperLinks = new ArrayList<>();
							for (Module module : modules) {
								IFile file = (IFile)activeEditor.getEditorInput().getAdapter(IFile.class);
								try {
									String absolutePath = new Path(file.getRawLocation().toFile().getCanonicalFile().getAbsolutePath()).toPortableString();
									if (module.getSourceFile().getName().equals(absolutePath)) {
										for (ObjectCreation objectCreation : module.getProgram().getObjectCreationList()) {
											ClassDeclaration classDeclaration = objectCreation.getClassDeclaration();
											if (classDeclaration != null) {
												SourceRange location = objectCreation.getNewExpressionTree().location;
												int start = location.start.offset;
												int end = location.end.offset;
												int length = end - start + 1;
												if (start <= region.getOffset() && end >= region.getOffset()) {
													IRegion newRegion = new Region(start, length);
													ClassInstantiationHyperlink classDeclarationHyperlink =
															new ClassInstantiationHyperlink(newRegion, classDeclaration);
													hyperLinks.add(classDeclarationHyperlink);
												}
											}
										}
										
										for (Dependency dependency : module.getDependencies()) {
											SourceRange location = dependency.getExpresion().getExpression().location;
											int start = location.start.offset;
											int end = location.end.offset;
											int length = end - start + 1;
											if (start <= region.getOffset() && end >= region.getOffset()) {
												IRegion newRegion = new Region(start, length);
												ModuleDeclarationHyperlink moduleDeclarationHyperlink =
														new ModuleDeclarationHyperlink(newRegion, dependency.getDependency());
												hyperLinks.add(moduleDeclarationHyperlink);
											}	
										}
										break;
									}
								} catch (IOException ioException) {
									ioException.printStackTrace();
								}
							}
							if (hyperLinks.size() > 0)
								return hyperLinks.toArray(new IHyperlink[]{});
							else 
								return null;
						}
					} 
				}
			
		}
		return null;
	}

}
