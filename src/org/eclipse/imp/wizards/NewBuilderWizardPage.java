/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Custom wizard page for the extension point "org.eclipse.core.resources.builders",
 * which adds a "language" field to what gets automatically generated from the
 * extension point schema.
 */
public class NewBuilderWizardPage extends ExtensionPointWizardPage {
    boolean fAddSMAPSupport= true;

    public NewBuilderWizardPage(ExtensionPointWizard owner) {
        super(owner, "org.eclipse.core.resources", "builders", false);
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        // RMF 10/18/2006: Hack to make wizard page be "finishable" right away.
        getFieldByFullName("extension.builder.run.parameter:name").fText.setText("foo");
        getFieldByFullName("extension.builder.run.parameter:value").fText.setText("bar");
    }

    
    protected void createFirstControls(Composite parent, String componentID) {
        createLanguageFieldForComponent(parent, fSchema.getPointId());
    }

    @Override
    protected void createAdditionalControls(Composite parent) {
    	
    	// Total hack to move the templates field to
    	// a position after class field
    	Control[] children = parent.getChildren();
    	children[24].moveBelow(children[17]);
    	children[23].moveBelow(children[17]);
    	children[22].moveBelow(children[17]);
    	children = parent.getChildren();
    	
		Button b= new Button(parent, SWT.CHECK);
		b.setText("Add SMAP support");
		b.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
			fAddSMAPSupport= ((Button) e.widget).getSelection();
			if (!fRequiredPlugins.contains("org.eclipse.imp.smapifier"))
			    fRequiredPlugins.add("org.eclipse.imp.smapifier");
		    }
		});
		b.setSelection(true); // Turn on SMAP support by default
    }
    
    /**
     * Overrides the default version of this method so as to do nothing, i.e.,
     * so as to not actually create the template field for this wizard page.
     * Note:  On this page we want to control where we put the field, so we
     * want to remove the effect of the operation that is used to put the
     * field into the default location.
     */
//	public WizardPageField createTemplateBrowseField(Composite parent, String componentID) {
//		return null;
//	}
	
    
}
