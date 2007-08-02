/**
 * 
 */
package org.eclipse.imp.wizards;

import java.util.Arrays;
import java.util.List;

import org.eclipse.imp.runtime.RuntimePlugin;

public class NewParser extends NoCodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "parser"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime" });
    }
}