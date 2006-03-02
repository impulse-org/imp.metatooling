package org.eclipse.uide.wizards;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.uide.WizardPlugin;
import org.eclipse.uide.runtime.RuntimePlugin;

public class Wizards {
    public static abstract class GenericServiceWizard extends ExtensionPointWizard {
	final static String UIDE_RUNTIME= RuntimePlugin.UIDE_RUNTIME;

	protected void addPages(ExtensionPointWizardPage[] pages) {
	    super.addPages(pages);
	    List/*<String>*/extenRequires= getPluginDependencies();
	    for(Iterator/*<String>*/iter= extenRequires.iterator(); iter.hasNext();) {
		String requiredPlugin= (String) iter.next();
		for(int n= 0; n < pages.length; n++) {
		    List/*<String>*/pageRequires= pages[n].getRequires();
		    pageRequires.add(requiredPlugin);
		}
	    }
	    //	    for(int n= 0; n < pages.length; n++) {
	    //		List requires= pages[n].getRequires();
	    //		requires.add(UIDE_RUNTIME);
	    //		// TODO RMF 10/18/2005 -- This dependency info belongs in the extension point wizard page.
	    //		requires.add("org.eclipse.core.runtime");
	    //		requires.add("org.eclipse.core.resources");
	    //		requires.add("org.eclipse.jface.text");
	    //		requires.add("org.eclipse.ui");
	    //		requires.add("org.eclipse.ui.ide");
	    //		requires.add("org.eclipse.ui.editors");
	    //		requires.add("org.eclipse.ui.views");
	    //		requires.add("org.eclipse.ui.workbench.texteditor");
	    //	    }
	}

	/**
	 * @return the list of plugin dependencies for this wizard.<br>
	 * Really, this information is specific to the stubs generated for a given language
	 * service extension. So, at least it really belongs to a wizard page, not a wizard.
	 * <br><b>Where to put it???</b>
	 */
	protected abstract List/*<String>*/getPluginDependencies();

	static public void replace(StringBuffer sb, String target, String substitute) {
	    for(int index= sb.indexOf(target); index != -1; index= sb.indexOf(target))
		sb.replace(index, index + target.length(), substitute);
	}

	protected byte[] getSampleFile(String fileName) {
	    try {
		URL url= Platform.asLocalURL(Platform.find(WizardPlugin.getInstance().getBundle(), new Path("/templates/"
			+ fileName)));
		String path= url.getPath();
		FileInputStream fis= new FileInputStream(path);
		DataInputStream is= new DataInputStream(fis);
		byte bytes[]= new byte[fis.available()];

		is.readFully(bytes);
		is.close();
		fis.close();
		return bytes;
	    } catch (Exception e) {
		e.printStackTrace();
		return ("// missing template file: " + fileName).getBytes();
	    }
	}

	public String getLanguage() {
	    return pages[0].languageText.getText();
	}
    }

    public static abstract class NoCodeServiceWizard extends GenericServiceWizard {
	public void generateCodeStubs(IProgressMonitor m) {}
    }

    public static class NewLanguage extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "languageDescription") });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}
    }

    public static class NewBuilder extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, "org.eclipse.core.resources",
		    "builders") });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
            IProject project= page.getProject();
            String langName= getLanguage();
            String[][] subs= new String[][] { { "$LANG_NAME$", langName } };

            createFileFromTemplate(langName + "Builder.java", "builder.tmpl", subs, project, mon);
            createFileFromTemplate(langName + "Nature.java", "nature.tmpl", subs, project, mon);
	}
    }

    public static class NewIndexer extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "index"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}
    }

    public static class NewParser extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "parser"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}
    }

    public static class NewModelListener extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "modelListener"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}
    }

    public static class NewOutliner extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "outliner"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
	    IProject project= page.getProject();
            String langName= getLanguage();
	    String[][] subs= new String[][] { { "$LANG_NAME$", langName } };

            createFileFromTemplate(langName + "Outliner.java", "outliner.tmpl", subs, project, mon);
	}
    }

    public static class NewContentProposer extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "contentProposer"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) {
	// TODO Auto-generated method stub
	}
    }

    public static class NewHoverHelper extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "hoverHelper"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) {
	// TODO Auto-generated method stub
	}
    }

    public static class NewTokenColorer extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "tokenColorer"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
            IProject project= page.getProject();
            String langName= getLanguage();
            String[][] subs= new String[][] { { "$LANG_NAME$", langName } };

            createFileFromTemplate(langName + "Colorer.java", "colorer.tmpl", subs, project, mon);
	}
    }

    public static class NewFoldingUpdater extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "foldingUpdater"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    // TODO Auto-generated method stub
	}
    }

    public static class NewAutoEditStrategy extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "autoEditStrategy"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    // TODO Auto-generated method stub
	}
    }

    public static class NewHyperlinkDetector extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "hyperLink"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    // TODO Auto-generated method stub
	}
    }

    public static class NewAnnotationHover extends GenericServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "annotationHover"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    // TODO Auto-generated method stub
	}
    }
}
