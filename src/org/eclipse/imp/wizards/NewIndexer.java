/**
 * 
 */
package org.eclipse.uide.wizards;

import java.util.Arrays;
import java.util.List;

import org.eclipse.uide.runtime.RuntimePlugin;

public class NewIndexer extends NoCodeServiceWizard {
    public void addPages() {
        addPages(new ExtensionPointWizardPage[] { new ExtensionPointWizardPage(this, RuntimePlugin.UIDE_RUNTIME, "indexContributor"), });
    }

    protected List getPluginDependencies() {
        return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
    	    "org.eclipse.uide.runtime" });
    }
}