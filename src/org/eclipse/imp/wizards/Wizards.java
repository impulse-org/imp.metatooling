package org.eclipse.uide.wizards;

import java.util.List;

import org.eclipse.ui.INewWizard;

public class Wizards {

	public static class NewLanguage extends GenericServiceWizard implements INewWizard {		
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, 0, 8, UIDE, 	"languageDescription",	false),
				new ExtensionPointWizardPage(this, 1, 8, UIDE, 	"parser", 				true),
				new ExtensionPointWizardPage(this, 2, 8, UIDE, 	"modelListener", 		true),
				new ExtensionPointWizardPage(this, 3, 8, UIDE, 	"index", 				true),
				new ExtensionPointWizardPage(this, 4, 8, UIDE, 	"outliner", 			true),
				new ExtensionPointWizardPage(this, 5, 8, UIDE, 	"contentProposer", 		true),
				new ExtensionPointWizardPage(this, 6, 8, UIDE, 	"hoverHelper", 			true),
				new ExtensionPointWizardPage(this, 7, 8, UIDE, 	"tokenColorer", 		true),
			});
		}
	}
	
	public static class NewIndex extends GenericServiceWizard {
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, UIDE, "index"),
			});
		}
	}
	
	public static class NewParser extends GenericServiceWizard {
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, UIDE, "parser"),
			});
		}
	}
	
	public static class NewModelListener extends GenericServiceWizard {
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, UIDE, "modelListener"),
			});
		}
	}
	
	public static class NewOutliner extends GenericServiceWizard {
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, UIDE, "outliner"),
			});
		}
	}
	
	public static class NewContentProposer extends GenericServiceWizard {
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, UIDE, "contentProposer"),
			});
		}
	}
	
	public static class NewHoverHelper extends GenericServiceWizard {
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, UIDE, "hoverHelper"),
			});
		}
	}
	
	public static class NewTokenColorer extends GenericServiceWizard {
		public void addPages() {		
			addPages(new ExtensionPointWizardPage[] {
				new ExtensionPointWizardPage(this, UIDE, "tokenColorer"),
			});
		}
	}
		
	public static class GenericServiceWizard extends ExtensionPointWizard {
		final static String UIDE = "org.eclipse.uide";

        protected void addPages(ExtensionPointWizardPage[] pages) {
			super.addPages(pages);
			for (int n = 0; n < pages.length; n++) {
				List requires = pages[n].getRequires();
				requires.add(UIDE);
				requires.add("org.eclipse.core.runtime");
				requires.add("org.eclipse.core.resources");
				requires.add("org.eclipse.jface.text");
				requires.add("org.eclipse.ui");
				requires.add("org.eclipse.ui.ide");
				requires.add("org.eclipse.ui.editors");
				requires.add("org.eclipse.ui.views");
				requires.add("org.eclipse.ui.workbench.texteditor");
			}
		}
	}
}
