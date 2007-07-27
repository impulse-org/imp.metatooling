/**
 * 
 */
package org.eclipse.uide.wizards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.runtime.RuntimePlugin;

public class NewContentProposer extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "contentProposer"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime",
                                            "org.eclipse.core.resources",
                                            "org.eclipse.uide.runtime" 
                                          });
    }

    public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
    	
        Map subs= getStandardSubstitutions();

        subs.put("$PARSER_PKG$", fParserPackage);
        subs.put("$AST_PKG$", fParserPackage + "." + Wizards.astDirectory);
        subs.put("$AST_NODE$", Wizards.astNode);

        subs.remove("$CONTENT_PROPOSER_CLASS_NAME$");
        subs.put("$CONTENT_PROPOSER_CLASS_NAME$", fFullClassName);
        
        subs.remove("$PACKAGE_NAME$");
        subs.put("$PACKAGE_NAME$", fPackageName);

        IFile outlinerSrc= createFileFromTemplate(
        	fFullClassName + ".java", "contentProposer.java", fPackageFolder, subs, fProject, mon);
 
        editFile(mon, outlinerSrc);
    }
    
    
}