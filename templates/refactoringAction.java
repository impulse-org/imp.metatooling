package $PACKAGE_NAME$;

import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.refactoring.RefactoringStarter;

public class $REFACTORING_PREFIX$RefactoringAction extends TextEditorAction {
//    private final UniversalEditor fEditor;

    public $REFACTORING_PREFIX$RefactoringAction(UniversalEditor editor) {
	super(RefactoringMessages.ResBundle, "$REFACTORING_PREFIX$.", editor);
//	fEditor= editor;
    }

    public void run() {
	final $REFACTORING_PREFIX$Refactoring refactoring= new $REFACTORING_PREFIX$Refactoring((UniversalEditor) this.getTextEditor());

	if (refactoring != null)
		new RefactoringStarter().activate(refactoring, new $REFACTORING_PREFIX$Wizard(refactoring, "$REFACTORING_NAME$"), this.getTextEditor().getSite().getShell(), "$REFACTORING_NAME$", false);
    }
}
