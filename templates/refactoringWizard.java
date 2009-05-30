package $PACKAGE_NAME$;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class $REFACTORING_PREFIX$Wizard extends RefactoringWizard {
    public $REFACTORING_PREFIX$Wizard($REFACTORING_PREFIX$Refactoring refactoring) {
        super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
        setDefaultPageTitle(refactoring.getName());
    }

    protected void addUserInputPages() {
        $REFACTORING_PREFIX$InputPage page= new $REFACTORING_PREFIX$InputPage(getRefactoring().getName());

        addPage(page);
    }

    public $REFACTORING_PREFIX$Refactoring get$REFACTORING_PREFIX$Refactoring() {
        return ($REFACTORING_PREFIX$Refactoring) getRefactoring();
    }
}
