/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Feb 27, 2007
 */
package org.eclipse.imp.perspective;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;

public class IMPPerspectiveFactory implements IPerspectiveFactory {
    public void createInitialLayout(IPageLayout layout) {
	String editorArea= layout.getEditorArea();
	IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$

	folder.addView(JavaUI.ID_PACKAGES);
	folder.addView(JavaUI.ID_TYPE_HIERARCHY);
	folder.addPlaceholder(IPageLayout.ID_RES_NAV);

	IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$

	outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
	outputfolder.addView(JavaUI.ID_JAVADOC_VIEW);
	outputfolder.addView(JavaUI.ID_SOURCE_VIEW);
	outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
	outputfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
	outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
	outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);

	layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float) 0.75, editorArea);
	layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
	layout.addActionSet(JavaUI.ID_ACTION_SET);
	layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
	layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

	// views - java
	layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
	layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
	layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
	layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);

	// views - search
	layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

	// views - debugging
	layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

	// views - standard workbench
	layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
	layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);

	// new actions - Java project creation wizard
	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$

	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
//	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
//	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard"); //$NON-NLS-1$
//	layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$


	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewLanguageWizard");
	layout.addNewWizardShortcut("org.eclipse.imp.lpg.NewLPGGrammarParserWrapperWizard");
	layout.addNewWizardShortcut("org.eclipse.imp.lpg.NewLPGGrammarWizard");
	layout.addNewWizardShortcut("org.eclipse.imp.lpg.NewParserWrapperWizard");	
	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewTokenColorer");
	// SMS 22 Jan 2008:  replaced NewOutliner with NewTreeModelBuilder
	// (which includes support for a new Label Provider)
//	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewOutliner");
	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewTreeModelBuilder");
	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewFoldingUpdater");
//	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewHoverHelper");
//	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewAnnotationHover");
	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewReferenceResolver");
	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewContentProposer");
//	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewAutoEditStrategy");
//	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewFormatter");
//	layout.addNewWizardShortcut("org.eclipse.imp.fancyTokenColorerWizard");
//	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewProjectWizard");
	layout.addNewWizardShortcut("org.eclipse.imp.newBuilder");
	layout.addNewWizardShortcut("org.eclipse.imp.natureEnablerWizard");
	layout.addNewWizardShortcut("org.eclipse.imp.newCompiler");
//	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewIndexer");
	layout.addNewWizardShortcut("org.eclipse.imp.wizards.NewPreferencesDialog");
	layout.addNewWizardShortcut("org.eclipse.imp.newRefactoring");

	layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
	layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
	layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
    }
}
