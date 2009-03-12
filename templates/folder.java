package $PACKAGE_NAME$;

import java.util.HashMap;
import java.util.List;

import org.eclipse.imp.services.base.LPGFolderBase;

import $AST_PACKAGE$.*;

/**
 * This file provides a skeletal implementation of the language-dependent aspects
 * of a source-text folder.  This implementation is generated from a template that
 * is parameterized with respect to the name of the language, the package containing
 * the language-specific types for AST nodes and AbstractVisitors, and the name of
 * the folder package and class.
 */
public class $FOLDER_CLASS_NAME$ extends LPGFolderBase {
    private void makeFoldable(ASTNode n) {
    	makeAnnotation(n.getLeftIToken(), n.getRightIToken());
    }

	/*
	 * A visitor for ASTs.  Its purpose is to create ProjectionAnnotations
	 * for regions of text corresponding to various types of AST node or to
	 * text ranges computed from AST nodes.  Projection annotations appear
	 * in the editor as the widgets that control folding.
	 */
	private class FoldingVisitor extends AbstractVisitor {
	    public void unimplementedVisitor(String s) { }
	    
		// START_HERE
		//
		// Include visit(..) functions for various types of AST nodes that are
		// associated with folding.  These functions should call one of the two
	    // versions of makeAnnotation(..) that are defined in FolderBase.  The
	    // usual case is to call the version of makeAnnotation that creates a
	    // folding annotation corresponding to the extent of a particular AST node.
	    // The other possibility is to create an annotation with an extent that
	    // is explicitly provided.  An example is shown below ...
	    
	    // Create annotations for the folding of blocks (for example)
		public boolean visit(block n) {
			makeFoldable(n);
            return true;
        }
    };
   
	
    // When instantiated will provide a concrete implementation of an abstract method
    // defined in FolderBase
    public void sendVisitorToAST(HashMap newAnnotations, List annotations, Object ast) {
        $AST_NODE$ theAST= ($AST_NODE$) ast;
        prsStream = theAST.getLeftIToken().getIPrsStream();
        AbstractVisitor abstractVisitor = new FoldingVisitor();
        theAST.accept(abstractVisitor);
        makeAdjunctAnnotations();		
	}
}
