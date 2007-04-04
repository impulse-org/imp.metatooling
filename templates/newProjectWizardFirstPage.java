package $PACKAGE_NAME$;

import org.eclipse.uide.wizards.NewProjectWizardFirstPage;

public class $CLASS_NAME_PREFIX$ProjectWizardFirstPage extends NewProjectWizardFirstPage {
    public $CLASS_NAME_PREFIX$ProjectWizardFirstPage() {
	super("$LANG_NAME$ Project");
	setPageComplete(false);
	setTitle("New $LANG_NAME$ Project");
	setDescription("Creates a new $LANG_NAME$ project");
	fInitialName= ""; //$NON-NLS-1$
    }
}
