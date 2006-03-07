package org.eclipse.uide.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.runtime.RuntimePlugin;

public class Wizards {
    public static abstract class NoCodeServiceWizard extends ExtensionPointWizard {
	public void generateCodeStubs(IProgressMonitor m) {}

	protected Map getStandardSubstitutions() {
	    return Collections.EMPTY_MAP; // noone should be calling this: generateCodeStubs() is empty...
	}
    }

    public static abstract class CodeServiceWizard extends ExtensionPointWizard {
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

        protected Map getStandardSubstitutions() {
            Map result= new HashMap();
            result.put("$LANG_NAME$", fLanguageName);
            result.put("$CLASS_NAME_PREFIX$", fClassName);
            result.put("$PACKAGE_NAME$", fPackageName);
            return result;
        }
    }

    // HACK: These must be in sync with the corresponding definitions in NewUIDEParserWizard.
    static final String astDirectory= "Ast";

    static final String astNode= "ASTNode";

    public static class NewLanguage extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "languageDescription") });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] {
                    "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime", "org.eclipse.ui" });
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
            Map subs= getStandardSubstitutions();

            // TODO Should pull the source file-name extension from the language description
            subs.put("$EXTEN$", "XXX");

            createFileFromTemplate(fClassName + "IncrementalProjectBuilder.java", "builder.tmpl", fPackageFolder, subs, project, mon);
            createFileFromTemplate(fClassName + "Nature.java", "nature.tmpl", fPackageFolder, subs, project, mon);
	}
    }

    public static class NewIndexer extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "index"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}
    }

    public static class NewParser extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "parser"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}
    }

    public static class NewModelListener extends NoCodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "modelListener"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}
    }

    public static class NewOutliner extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "outliner"), });
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
            Map subs= getStandardSubstitutions();

            subs.put("$PARSER_PKG$", fParserPackage);
            subs.put("$AST_PKG$", fParserPackage + "." + astDirectory);
            subs.put("$AST_NODE$", astNode);

            createFileFromTemplate(fClassName + "Outliner.java", "outliner.tmpl", fPackageFolder, subs, project, mon);
	}
    }

    public static class NewContentProposer extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "contentProposer"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) {
	// TODO Auto-generated method stub
	}
    }

    public static class NewHoverHelper extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "hoverHelper"), });
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
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "tokenColorer"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime", "org.eclipse.ui", "org.eclipse.jface.text", "lpg" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
            IProject project= page.getProject();
            Map subs= getStandardSubstitutions();

//          { "$PKG_NAME$", fPackageName },
            subs.put("$PARSER_PKG$", fParserPackage);

            createFileFromTemplate(fClassName + "TokenColorer.java", "colorer.tmpl", fPackageFolder, subs, project, mon);
	}
    }

    public static class NewFoldingUpdater extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "foldingUpdater"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    // TODO Auto-generated method stub
	}
    }

    public static class NewAutoEditStrategy extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "autoEditStrategy"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    // TODO Auto-generated method stub
	}
    }

    public static class NewHyperlinkDetector extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "hyperLink"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
	    // TODO Auto-generated method stub
	}
    }

    public static class NewAnnotationHover extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "annotationHover"), });
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
