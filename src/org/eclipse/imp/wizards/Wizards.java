package org.eclipse.uide.wizards;

import java.io.DataInputStream;
import java.io.File;
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
	}

	/**
	 * @return the list of plugin dependencies for this language service.
	 */
	// This information is specific to the stubs generated for a given language
	// service extension. So, it really belongs to a wizard page, not a wizard.
	// Where to put it???
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
    }

    public static abstract class NoCodeServiceWizard extends GenericServiceWizard {
	public void generateCodeStubs(IProgressMonitor m) {}
    }

    // HACK: These must be in sync with the corresponding definitions in NewUIDEParserWizard.
    static final String astDirectory= "Ast";

    static final String astNode= "ASTNode";

    public static class NewLanguage extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "languageDescription") });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] {
                    "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime", "org.eclipse.ui" });
	}
    }

    public static abstract class CodeServiceWizard extends GenericServiceWizard {
        protected String fLanguageName;
        protected String fPackageName;
        protected String fPackageFolder;
        protected String fParserPackage;
        protected String fClassName;

        protected void collectCodeParms() {
            fLanguageName= pages[0].fLanguageText.getText();
            fPackageName= pages[0].fPackageName;
            fPackageName= Character.toLowerCase(fPackageName.charAt(0)) + fPackageName.substring(1);
            fPackageFolder= fPackageName.replace('.', File.separatorChar);
            String[] subPkgs= fPackageName.split("\\.");
            StringBuffer buff= new StringBuffer();
            for(int i= 0; i < subPkgs.length-1; i++) {
                if (i > 0) buff.append('.');
                buff.append(subPkgs[i]);
            }
            buff.append(".parser");
            fParserPackage= buff.toString();
            fClassName= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
        }
    }

    public static class NewBuilder extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new BuilderWizardPage(this) });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] {
                    "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
            IProject project= page.getProject();
            String[][] subs= new String[][] {
                    { "$LANG_NAME$", fLanguageName },
                    { "$PACKAGE$", fPackageName },
                    // TODO Should pull the source file-name extension from the language description
                    { "$EXTEN$", "XXX" }
            };

            createFileFromTemplate(fLanguageName + "IncrementalProjectBuilder.java", "builder.tmpl", fPackageFolder, subs, project, mon);
            createFileFromTemplate(fLanguageName + "Nature.java", "nature.tmpl", fPackageFolder, subs, project, mon);
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

    public static class NewOutliner extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "outliner"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] {
                    "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime", "org.eclipse.ui", "org.eclipse.jface.text", 
                    "org.eclipse.ui.editors", "org.eclipse.ui.workbench.texteditor", "lpg" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
	    IProject project= page.getProject();
	    String[][] subs= new String[][] {
	        { "$LANG_NAME$", fLanguageName },
                { "$PACKAGE$", fPackageName },
                { "$PARSER_PKG$", fParserPackage },
                { "$AST_PKG$", fParserPackage + "." + astDirectory },
                { "$AST_NODE$", astNode }
	    };

            createFileFromTemplate(fLanguageName + "Outliner.java", "outliner.tmpl", fPackageFolder, subs, project, mon);
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

    public static class NewTokenColorer extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, UIDE_RUNTIME, "tokenColorer"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime", "org.eclipse.ui", "org.eclipse.jface.text", "lpg" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
            IProject project= page.getProject();
            String[][] subs= new String[][] {
                { "$LANG_NAME$", fLanguageName },
                { "$PKG_NAME$", fPackageName },
                { "$PARSER_PKG$", fParserPackage }
            };

            createFileFromTemplate(fLanguageName + "TokenColorer.java", "colorer.tmpl", fPackageFolder, subs, project, mon);
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
