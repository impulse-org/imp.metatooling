/**
 * 
 */
package org.eclipse.uide.wizards;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewFoldingUpdater extends CodeServiceWizard {
	public void addPages() {
	    addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "foldingUpdater"), });
	}

	protected List getPluginDependencies() {
	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
		    "org.eclipse.uide.runtime" });
	}

	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
            ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
            IProject project= page.getProject();
            Map subs= getStandardSubstitutions();

//          { "$PKG_NAME$", fPackageName },
            subs.put("$PARSER_PKG$", fParserPackage);
       
            // SMS 18 Jul 2006
            // Added (or modified) following to accommodate
            // values provided through wizard by user
            
            WizardPageField field = pages[0].getField("class");
            String qualifiedClassName = field.fValue;
            String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1);
            subs.remove("$FOLDER_CLASS_NAME$");
            subs.put("$FOLDER_CLASS_NAME$", className);
            
            subs.remove("$PACKAGE_NAME$");
            String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
            subs.put("$PACKAGE_NAME$", packageName);
            
            String packageFolder = packageName.replace('.', File.separatorChar);
            
            String folderTemplateName = "folder.java";
            IFile folderSrc = createFileFromTemplate(className + ".java", folderTemplateName, packageFolder, subs, project, mon);
            
            editFile(mon, folderSrc);
	}
    }