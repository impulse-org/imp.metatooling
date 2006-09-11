package $PACKAGE_NAME$;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.parser.IASTNodeLocator;
import org.eclipse.uide.parser.IParseController;

public class $REFACTORING_PREFIX$Refactoring extends Refactoring {
    private final IFile fSourceFile;
    private final ASTNode fNode;

    public $REFACTORING_PREFIX$Refactoring(UniversalEditor editor) {
	super();

	IEditorInput input= editor.getEditorInput();

	if (input instanceof IFileEditorInput) {
	    IFileEditorInput fileInput= (IFileEditorInput) input;

	    fSourceFile= fileInput.getFile();
	    fNode= findNode(editor);
	} else {
	    fSourceFile= null;
	    fNode= null;
	}
    }

    private ASTNode findNode(UniversalEditor editor) {
	Point sel= editor.getSelection();
	IParseController parseController= editor.getParseController();
	ASTNode root= (ASTNode) parseController.getCurrentAst();
	IASTNodeLocator locator= parseController.getNodeLocator();

	return (ASTNode) locator.findNode(root, sel.x);
    }

    public String getName() {
	return "$REFACTORING_NAME$";
    }

    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
	// Check parameters retrieved from editor context
	return new RefactoringStatus();
    }

    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
	return new RefactoringStatus();
    }

    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
	TextFileChange tfc= new TextFileChange("$REFACTORING_NAME$", fSourceFile);

	tfc.setEdit(new MultiTextEdit());

	int startOffset= 0;
	int endOffset= 5;

	// START HERE
	tfc.addEdit(new ReplaceEdit(startOffset, endOffset - startOffset + 1, "Boo!"));

	return tfc;
    }
}
