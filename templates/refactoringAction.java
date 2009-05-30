package $PACKAGE_NAME$;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.imp.refactoring.RefactoringStarter;

public class $REFACTORING_PREFIX$RefactoringAction extends TextEditorAction {
    public $REFACTORING_PREFIX$RefactoringAction(ITextEditor editor) {
        super(RefactoringMessages.ResBundle, "$REFACTORING_PREFIX$.", editor);
    }

    public void run() {
        final $REFACTORING_PREFIX$Refactoring refactoring= new $REFACTORING_PREFIX$Refactoring(getTextEditor());

        if (refactoring != null) {
            new RefactoringStarter().activate(refactoring, new $REFACTORING_PREFIX$Wizard(refactoring),
                    getTextEditor().getSite().getShell(), refactoring.getName(), false);
        }
    }
}
