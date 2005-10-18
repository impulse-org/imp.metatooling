package org.eclipse.uide.wizards;

import java.util.List;

import org.eclipse.ui.INewWizard;
import org.eclipse.uide.runtime.RuntimePlugin;

public class Wizards {
    public static class GenericServiceWizard extends ExtensionPointWizard {
	final static String UIDE_RUNTIME= RuntimePlugin.UIDE_RUNTIME;

	protected void addPages(ExtensionPointWizardPage[] pages) {
	    super.addPages(pages);
	    for(int n= 0; n < pages.length; n++) {
		List requires= pages[n].getRequires();
		requires.add(UIDE_RUNTIME);
		// TODO RMF 10/18/2005 -- This dependency info belongs in the extension point wizard page.
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

    public static class NewLanguage extends GenericServiceWizard implements INewWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "languageDescription") });
	}
    }

    public static class NewBuilder extends GenericServiceWizard implements INewWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, "org.eclipse.core.resources", "builders") });
	}
    }

    public static class NewIndex extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "index"), });
	}
    }

    public static class NewParser extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "parser"), });
	}
    }

    public static class NewModelListener extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "modelListener"), });
	}
    }

    public static class NewOutliner extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "outliner"), });
	}
    }

    public static class NewContentProposer extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "contentProposer"), });
	}
    }

    public static class NewHoverHelper extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "hoverHelper"), });
	}
    }

    public static class NewTokenColorer extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "tokenColorer"), });
	}
    }
}
