package $PACKAGE_NAME$;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class $REFACTORING_PREFIX$Wizard extends RefactoringWizard {
    public $REFACTORING_PREFIX$Wizard($REFACTORING_PREFIX$Refactoring refactoring, String pageTitle) {
	super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
	setDefaultPageTitle(pageTitle);
    }

    protected void addUserInputPages() {
	$REFACTORING_PREFIX$InputPage page= new $REFACTORING_PREFIX$InputPage("$REFACTORING_NAME$");

	addPage(page);
    }

    public $REFACTORING_PREFIX$Refactoring get$REFACTORING_PREFIX$Refactoring() {
	return ($REFACTORING_PREFIX$Refactoring) getRefactoring();
    }
}
