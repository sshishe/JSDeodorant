package ca.concordia.jsdeodorant.eclipseplugin.views.ModulesView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ca.concordia.jsdeodorant.analysis.AnalysisObserver;
import ca.concordia.jsdeodorant.analysis.AnalysisOptions;
import ca.concordia.jsdeodorant.analysis.AnalysisStep;
import ca.concordia.jsdeodorant.analysis.ClassAnalysisMode;
import ca.concordia.jsdeodorant.analysis.abstraction.Dependency;
import ca.concordia.jsdeodorant.analysis.abstraction.Module;
import ca.concordia.jsdeodorant.analysis.decomposition.TypeDeclaration;
import ca.concordia.jsdeodorant.analysis.module.PackageSystem;
import ca.concordia.jsdeodorant.eclipseplugin.listeners.JSDeodorantPartListener;
import ca.concordia.jsdeodorant.eclipseplugin.listeners.JSDeodorantSelectionListener;
import ca.concordia.jsdeodorant.eclipseplugin.util.Constants;
import ca.concordia.jsdeodorant.eclipseplugin.util.ImagesHelper;
import ca.concordia.jsdeodorant.eclipseplugin.util.ModulesInfo;
import ca.concordia.jsdeodorant.eclipseplugin.util.OpenAndAnnotateHelper;
import ca.concordia.jsdeodorant.eclipseplugin.views.InstantiationsView.JSDeodorantClassInstantiationsView;
import ca.concordia.jsdeodorant.eclipseplugin.views.VisualizationView.JSDeodorantVisualizationView;
import ca.concordia.jsdeodorant.eclipseplugin.views.wizard.AnalysisOptionsWizard;
import ca.concordia.jsdeodorant.launcher.Runner;

public class JSDeodorantModulesView extends ViewPart {

	public static final String ID = "jsdeodorant-eclipse-plugin.JSDeodorantModulesView";

	private TreeViewer typeTreeViewer;
	private Label projectNameLabel;

	private IAction clearAnnotationsAction;
	private IAction clearResultsAction;
	private IAction showWizardAction;
	private IAction analyzeAction;
	private IAction showDependenciesAction;
	private IAction showClassVisualizationAction;
	private IAction typeHierarchyModeAction;
	private IAction modulesViewModeAction;
	private IAction findInstantiationsAction;
	private IAction showTypeHierarchyForClassAction;
	
	private ISelectionListener selectionListener;
	private IPartListener2 partListener; 
	
	private AnalysisOptions analysisOptions;
	private ModuleViewMode viewMode = ModuleViewMode.MODULE_EXPLORER;
	
	@Override
	public void createPartControl(Composite parent) {
		
		getDefaultAnalysisOptions();
		
		GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 1;		
	    parent.setLayout(gridLayout);
		
	    hookListeners();
	    createTopBar(parent);
	    createTreeViewer(parent);
	    makeActions(parent);
	    addActionBarButtons();
	}

	private void getDefaultAnalysisOptions() {
		analysisOptions = new AnalysisOptions();
		analysisOptions.setPackageSystem(PackageSystem.ClosureLibrary.name());
		analysisOptions.setModuleAnlysis(true);
		analysisOptions.setClassAnalysis(true);
		analysisOptions.setClassAnalysisMode(ClassAnalysisMode.STRICT.toString());
		analysisOptions.setAnalyzeLibrariesForClasses(true);
	}
	
	public AnalysisOptions getAnalysisOptions() {
		return analysisOptions;
	}

	private void hookListeners() {
		selectionListener = new JSDeodorantSelectionListener(this);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionListener);
		
		partListener = new JSDeodorantPartListener(this);
		getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	private void makeActions(Composite parent) {
		clearAnnotationsAction = new Action() {
			@Override
			public void run() {
				clearAnnotations();
			}
		};
		clearAnnotationsAction.setText("Clear annotations");
		clearAnnotationsAction.setToolTipText("Clear annotations");
		clearAnnotationsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_REMOVEALL));
		clearAnnotationsAction.setEnabled(false);
		
		clearResultsAction = new Action() {
			@Override
			public void run() {
				clearResults();
			}
		};
		clearResultsAction.setText("Clear results");
		clearResultsAction.setToolTipText("Clear results");
		clearResultsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		clearResultsAction.setEnabled(false);
		
		showWizardAction = new Action() {
			@Override
			public void run() {
				showWizard(parent);
			}

		};
		showWizardAction.setText("Show analysis wizard");
		showWizardAction.setToolTipText("Show analysis wizard");
		showWizardAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		showWizardAction.setEnabled(false);
		
		analyzeAction = new Action() {
			@Override
			public void run() {	
				analyze();
			}
		};
		analyzeAction.setText("Start analysis");
		analyzeAction.setToolTipText("Start analysis");
		analyzeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		analyzeAction.setEnabled(false);
		
		showDependenciesAction = new Action() {
			@Override
			public void run() {
				showDependencies();
			}
		};
		showDependenciesAction.setText("Show module dependencies");
		showDependenciesAction.setToolTipText("Show module dependencies");
		showDependenciesAction.setImageDescriptor(ImagesHelper.getImageDescriptor(Constants.DEPENDENCIES_ICON_IMAGE));
		
		showClassVisualizationAction = new Action() {
			@Override
			public void run() {
				showClassUMLDiagram();
			}		
		};
		showClassVisualizationAction.setText("Show class diagram");
		showClassVisualizationAction.setToolTipText("Show class diagram");
		showClassVisualizationAction.setImageDescriptor(ImagesHelper.getImageDescriptor(Constants.DEPENDENCIES_ICON_IMAGE));
		modulesViewModeAction = new Action() {
			@Override
			public void run() {
				viewMode = ModuleViewMode.MODULE_EXPLORER;
				setTreeViewerContentProviderBasedOnViewMode(new NullProgressMonitor());
			}
		};
		modulesViewModeAction.setText("Show modules");
		modulesViewModeAction.setToolTipText("Show modules");
		modulesViewModeAction.setImageDescriptor(ImagesHelper.getImageDescriptor(Constants.MODULES_VIEW_ICON));
		modulesViewModeAction.setChecked(true);
		
		typeHierarchyModeAction = new Action() {
			@Override
			public void run() {
				viewMode = ModuleViewMode.TYPE_HIERARCHY;
				setTreeViewerContentProviderBasedOnViewMode(new NullProgressMonitor());
			}
		};
		typeHierarchyModeAction.setText("Show type hierarchies");
		typeHierarchyModeAction.setToolTipText("Show type hierarchies");
		typeHierarchyModeAction.setImageDescriptor(ImagesHelper.getImageDescriptor(Constants.TYPE_HIERARCHY_VIEW_ICON));
		typeHierarchyModeAction.setChecked(false);
		
		findInstantiationsAction = new Action() {
			@Override
			public void run() {
				findInstantiations();
			}
		};
		findInstantiationsAction.setText("Find instantiations");
		findInstantiationsAction.setToolTipText("Find instantiations");
		findInstantiationsAction.setImageDescriptor(ImagesHelper.getImageDescriptor(Constants.SEARCH_FOR_REFERENCES_ICON));
		
		showTypeHierarchyForClassAction = new Action() {
			@Override
			public void run() {
				showTypeHierarchyForSelectedClass();
			}
		};
		showTypeHierarchyForClassAction.setText("Show type hierarchy");
		showTypeHierarchyForClassAction.setToolTipText("Show type hierarchy");
		showTypeHierarchyForClassAction.setImageDescriptor(ImagesHelper.getImageDescriptor(Constants.TYPE_HIERARCHY_VIEW_ICON));

	}

	private void addActionBarButtons() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(analyzeAction);
		manager.add(showWizardAction);
		manager.add(new Separator());
		manager.add(clearAnnotationsAction);
		manager.add(clearResultsAction);
		manager.add(new Separator());
		manager.add(typeHierarchyModeAction);
		manager.add(modulesViewModeAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(typeHierarchyModeAction);
		manager.add(modulesViewModeAction);
		manager.add(new Separator());
		manager.add(clearAnnotationsAction);
		manager.add(clearResultsAction);
		manager.add(showWizardAction);
		manager.add(analyzeAction);
	}
	
	private void createTopBar(Composite parent) {
		projectNameLabel = new Label(parent, SWT.NONE);
		projectNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}

	private void createTreeViewer(Composite parent) {
		typeTreeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		typeTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		typeTreeViewer.setContentProvider(new ClassesTreeViewerContentProvider(null));
		typeTreeViewer.setLabelProvider(new ClassesTreeViewerLabelProvider());
		typeTreeViewer.addDoubleClickListener(new ClassesTreeViewerDoubleClickListener());
		typeTreeViewer.setInput(getViewSite());
		typeTreeViewer.setComparator(new ClassesTreeViewerComparator());
		
		typeTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				getRightClickMenu();
			}
		});
	}

	private void getRightClickMenu() {
		
		List<IAction> actionsToAdd = new ArrayList<>();
		
		ISelection selection = typeTreeViewer.getSelection();
		if (!selection.isEmpty()) {
			Object firstElement = ((IStructuredSelection)selection).getFirstElement();
			if (firstElement instanceof Module) {
				Module selectedModule = (Module)firstElement;
				if (!selectedModule.getDependencies().isEmpty()) {
					actionsToAdd.add(showDependenciesAction);
				}
			} else if (firstElement instanceof TypeDeclaration) {
				actionsToAdd.add(showClassVisualizationAction);
				actionsToAdd.add(findInstantiationsAction);
				actionsToAdd.add(showTypeHierarchyForClassAction);
			}
		}
		
		typeTreeViewer.getTree().setMenu(null);
		if (!actionsToAdd.isEmpty()) {
			MenuManager menuMgr = new MenuManager("#PopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					for (IAction action : actionsToAdd) {
						manager.add(action);
					}
				}
			});
			Menu menu = menuMgr.createContextMenu(typeTreeViewer.getControl());
			typeTreeViewer.getControl().setMenu(menu);
		}
	}
	
	public Module getSelectedModule() {
		ISelection selection = typeTreeViewer.getSelection();
		if (!selection.isEmpty()) {
			Object firstElement = ((IStructuredSelection)selection).getFirstElement();
			if (firstElement instanceof Module)
				return (Module)firstElement;
		}
		return null;
	}
	
	public TypeDeclaration getSelectedClass() {
		ISelection selection = typeTreeViewer.getSelection();
		if (!selection.isEmpty()) {
			Object firstElement = ((IStructuredSelection)selection).getFirstElement();
			if (firstElement instanceof TypeDeclaration) {
				return (TypeDeclaration)firstElement;
			}
		}
		return null;
	}

	private void analyze() {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					analysisOptions.getAnalysisObservers().clear();
					analysisOptions.addAnalysisObserver(new AnalysisObserver() {
						@Override
						public void progressed(AnalysisStep step) {
							monitor.worked(1);
							monitor.setTaskName(step.toString());
						}
					});
					monitor.beginTask("JSDeodorant analysis", AnalysisStep.values().length);
					Runner runner = new Runner(new String[0]) {
						@Override
						public AnalysisOptions createAnalysisOptions() {
							setAnalysisOptions(analysisOptions);
							return analysisOptions;
						}
					};
					runner.createAnalysisOptions();
					try {
						Set<Module> modules = runner.performActions();
						ModulesInfo.setModuleInfo(modules, analysisOptions.getDirectoryPath());
						if (modules != null && !monitor.isCanceled()) {
							setTreeViewerContentProviderBasedOnViewMode(monitor);
							clearResultsAction.setEnabled(true);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void clearResults() {
		typeTreeViewer.setContentProvider(new ClassesTreeViewerContentProvider(null));
		clearResultsAction.setEnabled(false);
	}
	
	private void clearAnnotations() {
		IEditorPart activeEditor = OpenAndAnnotateHelper.getActiveEditor();
		if (activeEditor != null) {
			OpenAndAnnotateHelper.clearAnnotations(activeEditor);
		}
		clearAnnotationsAction.setEnabled(false);
	}
	
	private void showWizard(Composite parent) {
		AnalysisOptionsWizard analysisOptionsWizard = new AnalysisOptionsWizard(analysisOptions);
		WizardDialog wizardDialog = new WizardDialog(parent.getShell(), analysisOptionsWizard);
		if (wizardDialog.open() == Window.OK) {
			this.analysisOptions = analysisOptionsWizard.getAnalysisOptions();
		}
	}
	
	private void showDependencies() {
		Module selectedModule = getSelectedModule();
		Set<Dependency> dependencies = selectedModule.getDependencies();
		if (!dependencies.isEmpty()) {
			IViewPart dependenciesView = OpenAndAnnotateHelper.openView(JSDeodorantVisualizationView.ID);
			if (dependenciesView != null) {
				((JSDeodorantVisualizationView)dependenciesView).showDependenciesGraph(selectedModule);
			}
		}
	}
	
	private void showClassUMLDiagram() {
		TypeDeclaration selectedType = getSelectedClass();
		if (selectedType != null) {
			IViewPart dependenciesView = OpenAndAnnotateHelper.openView(JSDeodorantVisualizationView.ID);
			if (dependenciesView != null) {
				((JSDeodorantVisualizationView)dependenciesView).showUMLClassDiagram(selectedType);
			}
		}
	}
	
	private void findInstantiations() {
		TypeDeclaration selectedType = getSelectedClass();
		if (selectedType != null) {
			JSDeodorantClassInstantiationsView instantiationsView = ((JSDeodorantClassInstantiationsView)OpenAndAnnotateHelper.openView(JSDeodorantClassInstantiationsView.ID));
			instantiationsView.showInstantiationsFor(selectedType);
		}
	}
	
	protected void showTypeHierarchyForSelectedClass() {
		TypeDeclaration selectedClass = getSelectedClass();
		if (selectedClass != null) {
			JSDeodorantModulesView modulesView = ((JSDeodorantModulesView)OpenAndAnnotateHelper.openView(JSDeodorantModulesView.ID));
			modulesView.showTypeHierarchyForClassDeclaration(selectedClass);
		}
	}

	protected void setTreeViewerContentProviderBasedOnViewMode(IProgressMonitor monitor) {
		monitor.setTaskName("Populating view");
		Set<Module> modules = ModulesInfo.getModuleInfo();
		if (modules != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					switch(viewMode) {
					case MODULE_EXPLORER:
						modulesViewModeAction.setChecked(true);
						typeHierarchyModeAction.setChecked(false);
						typeTreeViewer.setContentProvider(new ClassesTreeViewerContentProvider(modules));
						break;
					case TYPE_HIERARCHY:
						modulesViewModeAction.setChecked(false);
						typeHierarchyModeAction.setChecked(true);
						typeTreeViewer.setContentProvider(new ClassHierarchiesTreeViewerContentProvider(modules));
						break;
					default:
						break;
					}
				}
			});
		}
		monitor.done();
	}
	
	public void showTypeHierarchyForClassDeclaration(TypeDeclaration classDeclaration) {
		viewMode = ModuleViewMode.TYPE_HIERARCHY;
		setTreeViewerContentProviderBasedOnViewMode(new NullProgressMonitor());
		typeTreeViewer.setSelection(new StructuredSelection(classDeclaration), true);
	}
	
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(selectionListener);
		getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
	}

	@Override
	public void setFocus() {}

	public void setAnalysisButtonsEnabled(boolean enabled) {
		showWizardAction.setEnabled(enabled);
		analyzeAction.setEnabled(enabled);
	}

	public void setClearAnnotationsButtonEnabled(boolean enabled) {
		clearAnnotationsAction.setEnabled(enabled);		
	}

	public void getSelectedProject(ISelection selection) {
		selectionListener.selectionChanged(this, selection);
	}

	public void setSelectedProjectName(String name) {
		projectNameLabel.setText("Selected JavaScript project: " + name);
	}
	
}
