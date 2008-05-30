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

import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * The "New" wizard page allows setting the container for the new file as well as the
 * file name. The page will only accept file name without the extension OR with the
 * extension that matches the expected one (g).
 */
public class NewEditorServiceWizardPage extends ExtensionPointWizardPage
{
    protected String fServiceQualifiedClassName = null;
    protected IModelListener.AnalysisRequired fAnalysisLevel = null;
    protected WizardPageField fAnalysisRequiredField = null;
    
	
	public NewEditorServiceWizardPage(ExtensionPointWizard wizard) {
		super(wizard, RuntimePlugin.IMP_RUNTIME, "editorService");
		setTitle("New Editor Service");
		setDescription("This wizard supports the creation of an arbitrary service for an IMP-based IDE editor.");
		// flag that controls whether the extension name and id fields show up in the wizard page
		fOmitExtensionIDName = false;
    }
	
    protected void createAdditionalControls(Composite parent) {
    	Combo analysisRequired = createAnalysisRequiredCombo(parent);
    	analysisRequired.addModifyListener(new ModifyListener() {
    		public void modifyText(ModifyEvent e) {
    			fAnalysisLevel = IModelListener.AnalysisRequired.valueOf(
    				((Combo)e.getSource()).getText());
	            dialogChanged();
    		}
    	});
    }	


    public IModelListener.AnalysisRequired getAnalysisLevel() {
    	return fAnalysisLevel;
    }
    
    
    public String getAnalysisLevelName() {
    	return fAnalysisLevel.name();
    }
    

    public Combo createAnalysisRequiredCombo(Composite parent)
    {
    	fAnalysisRequiredField = new WizardPageField(
    		null, "Analysis level required", "Analysis level required", null, 1, true,
    		"Specifies the level of analysis required as a precondition to this service");
    	
    	IModelListener.AnalysisRequired[] analysisLevels = IModelListener.AnalysisRequired.values();
    	String[] values = new String[analysisLevels.length];
    	for (int i =0; i < analysisLevels.length; i++) {
    		values[i] = analysisLevels[i].name();
    	}
    	Combo combo = createLabelCombo(parent, fAnalysisRequiredField, values);
    	
    	return combo;
    }
    
    
    protected WizardPageField getUncompletedField() {
    	WizardPageField uncompletedField = super.getUncompletedField();
    	if (uncompletedField != null)
    		return uncompletedField;
    	if (fAnalysisLevel == null || getAnalysisLevelName() == null || getAnalysisLevelName().length() == 0)
    		return fAnalysisRequiredField;
        return null;
    }
	
}
