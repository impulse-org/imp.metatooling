/**
 * 
 */
package org.eclipse.imp.wizards;

import java.util.Arrays;
import java.util.List;

import org.eclipse.imp.runtime.RuntimePlugin;

public class NewModelListener extends NoCodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "modelListener"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime" });
    }
}