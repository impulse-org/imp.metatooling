package org.eclipse.imp.wizards;

import org.eclipse.imp.ui.dialogs.ListSelectionDialog;
import org.eclipse.imp.ui.dialogs.filters.ViewerFilterForIDEProjects;
import org.eclipse.imp.ui.dialogs.filters.ViewerFilterForPluginProjects;
import org.eclipse.imp.ui.dialogs.validators.SelectionValidatorForPluginProjects;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ISelectionValidator;

/**
 * A wizard page for the New Language wizard.  Not especially different
 * from other ExtensionPointWizardPages, but needs to override the new
 * version of createLangaugeFieldForComponent(..) so as to enable that
 * field.  (The default state for the language field is now disabled,
 * which is appropriate for all wizards with the field except this one.)
 * 
 * @author sutton (Stan Sutton, suttons@us.ibm.com)
 * @since 20071113
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
	
    /**
     * Overrides the default version of this method so as to do nothing, i.e.,
     * so as to not create a language field.
     * 
     * This method is called "automatically" for all IMP wizard pages.  By default
     * it creates a language field that shows the langauge associated with the
     * selected project.  This is apporpriate for almost all IMP wizards.  The
     * NewLanguage wizard is an exception, since this is the wizard through which
     * the language is associated with the project in the first place.  The wizard
     * page does need a language field (for the naming of the new language), but the
     * field is defined as an attribute in the extension schema and will be created
     * by the mechanism that creates fields for all extension attributes.
     * 
     * @see org.eclipse.imp.wizards.IMPWizardPage#createLanguageFieldForComponent(org.eclipse.swt.widgets.Composite, java.lang.String)
     */
    protected void createLanguageFieldForComponent(Composite parent, String componentID) {
    	// nichts
    }
    
    /**
     * Overrides the default version of this method so as to add a viewer filter
     * for plug-in projects rather than for IDE projects (as in the default case).
     * The language must be defined in a plug-in project, but that project does
     * not become an IDE project until after this wizard is completed.
     * 
     * @see org.eclipse.imp.wizards.IMPWizardPage#addFilterToDialog(org.eclipse.imp.ui.dialogs.ListSelectionDialog)
     */
    protected void addFilterToDialog(ListSelectionDialog dialog) {
        dialog.addFilter(new ViewerFilterForPluginProjects());
    }
    
    /**
     * Overrides the default version of this method so as to add a selection validator
     * for plug-in projects rather than for IDE projects (as in the default case).
     * The language must be defined in a plug-in project, but that project does
     * not become an IDE project until after this wizard is completed.
     * 
     * @see org.eclipse.imp.wizards.IMPWizardPage#getSelectionValidatorForProjects()
     */
	protected ISelectionValidator getSelectionValidatorForProjects() {
		return new SelectionValidatorForPluginProjects();
	}
    
	
}
