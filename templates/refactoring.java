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
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IASTFindReplaceTarget;

public class $REFACTORING_PREFIX$Refactoring extends Refactoring {
    private final IFile fSourceFile;
    private final ASTNode fNode;
    private final ITextEditor fEditor;

    public $REFACTORING_PREFIX$Refactoring(ITextEditor editor) {
        super();

        fEditor= editor;

        IASTFindReplaceTarget frt= (IASTFindReplaceTarget) fEditor;
        IEditorInput input= editor.getEditorInput();

        if (input instanceof IFileEditorInput) {
            IFileEditorInput fileInput= (IFileEditorInput) input;

            fSourceFile= fileInput.getFile();
            fNode= findNode(frt);
        } else {
            fSourceFile= null;
            fNode= null;
        }
    }

    private ASTNode findNode(IASTFindReplaceTarget frt) {
        Point sel= frt.getSelection();
        IParseController parseController= frt.getParseController();
        ASTNode root= (ASTNode) parseController.getCurrentAst();
        ISourcePositionLocator locator= parseController.getSourcePositionLocator();

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
