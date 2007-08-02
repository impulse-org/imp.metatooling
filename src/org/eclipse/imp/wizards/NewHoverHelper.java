/**
 * 
 */
package org.eclipse.imp.wizards;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;

public class NewHoverHelper extends CodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "hoverHelper"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime" });
    }

    public void generateCodeStubs(IProgressMonitor mon) {
    // TODO Auto-generated method stub
    	// Note:  Take advantage of field values set by collectCodeParms()
    	// in CodeServiceWizard, i.e., you shouldn't have to take values
    	// off the wizard pages here (unless something special is needed)
    }
    
}