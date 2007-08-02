/**
 * 
 */
package org.eclipse.imp.wizards;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class NoCodeServiceWizard extends ExtensionPointWizard {
    public void generateCodeStubs(IProgressMonitor m) {}

    protected Map getStandardSubstitutions() {
        return Collections.EMPTY_MAP; // noone should be calling this: generateCodeStubs() is empty...
    }
}