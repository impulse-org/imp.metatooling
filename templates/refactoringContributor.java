package $PACKAGE_NAME$;

import org.eclipse.jface.action.IAction;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.editor.UniversalEditor.IRefactoringContributor;

public class RefactoringContributor implements IRefactoringContributor {
    public RefactoringContributor() { }

    public IAction[] getEditorRefactoringActions(UniversalEditor editor) {
	return new IAction[] {
	};
    }
}
