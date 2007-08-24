/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Mar 22, 2006
 */
package org.eclipse.imp.wizards;

import org.eclipse.swt.widgets.Composite;

public class NewProjectWizardPage extends ExtensionPointWizardPage {
    public NewProjectWizardPage(ExtensionPointWizard owner) {
	super(owner, "org.eclipse.ui", "newWizards");
    }

    protected void createFirstControls(Composite parent) {
	createLanguageFieldForPlatformSchema(parent);
    }
}
