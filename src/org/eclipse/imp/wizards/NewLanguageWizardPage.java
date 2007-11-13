package org.eclipse.imp.wizards;

import org.eclipse.swt.widgets.Composite;

/**
 * A wizard page for the New Language wizard.  Not especially different
 * from other ExtensionPointWizardPages, but needs to override the new
 * version of createLangaugeFieldForComponent(..) so as to enable that
 * field.  (The default state for the language field is now disabled,
 * which is appropriate for all wizards with the field except this one.)
 * 
 * @author sutton (Stan Sutton, suttons@us.ibm.com)
 * @since 2007 11 13
 *
 */
public class NewLanguageWizardPage extends ExtensionPointWizardPage {

   public NewLanguageWizardPage(ExtensionPointWizard owner, String pluginID, String pointID) {
    	super(owner, pluginID, pointID, true);
    }
    
    public NewLanguageWizardPage(ExtensionPointWizard owner, String pluginID, String pointID, boolean omitIDName) {
    	super(owner, 0, 1, pluginID, pointID, false);
    }
    
    public NewLanguageWizardPage(ExtensionPointWizard owner, String pluginID, String pointID, boolean omitIDName, boolean local) {
        super(owner, 0, 1, pluginID, pointID, false, local);
    }

    public NewLanguageWizardPage(ExtensionPointWizard owner, int pageNumber, int totalPages, String pluginID, String pointID, boolean isOptional) {
        super(owner, pageNumber, totalPages, pluginID, pointID, isOptional);
    }
	
    protected void createLanguageFieldForComponent(Composite parent, String componentID) {
    	super.createLanguageFieldForComponent(parent, componentID);
    	fLanguageText.setEnabled(true);
    }
	
	
}
