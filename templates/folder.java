package $PACKAGE_NAME$;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.imp.services.base.FolderBase;

import $AST_PACKAGE$.*;
import lpg.runtime.*;

/**
 * This file provides a skeletal implementation of the language-dependent aspects
 * of a source-text folder.  This implementation is generated from a template that
 * is parameterized with respect to the name of the language, the package containing
 * the language-specific types for AST nodes and AbstractVisitors, and the name of
 * the folder package and class.
 * 
 * @author suttons@us.ibm.com
 *
 */
public class $FOLDER_CLASS_NAME$ extends FolderBase {
    IPrsStream prsStream;

    public void makeAnnotationWithOffsets(int first_offset, int last_offset)
    {
        super.makeAnnotation(first_offset, last_offset - first_offset + 1);
    }
    
    //
    // Use this version of makeAnnotation when you have a range of 
    // tokens to fold.
    //
    private void makeAnnotation(IToken first_token, IToken last_token)
    {
        if (last_token.getEndLine() > first_token.getLine())
        {
            IToken next_token = prsStream.getIToken(prsStream.getNext(last_token.getTokenIndex()));
            IToken[] adjuncts = next_token.getPrecedingAdjuncts();
            IToken gate_token = adjuncts.length == 0 ? next_token : adjuncts[0];
            makeAnnotationWithOffsets(first_token.getStartOffset(), 
                                      gate_token.getLine() > last_token.getEndLine()
                                          ? prsStream.getLexStream().getLineOffset(gate_token.getLine() - 1)
                                          : last_token.getEndOffset());
        }
    }
    
    private void makeAnnotation(ASTNode n) {
    	makeAnnotation(n.getLeftIToken(), n.getRightIToken());
    }

    private void makeAdjunctAnnotations(ASTNode theAST)
    {
        ILexStream lexStream = prsStream.getLexStream();
		ArrayList adjuncts = (ArrayList) prsStream.getAdjuncts();
        for (int i = 0; i < adjuncts.size();)
        {
            Adjunct adjunct = (Adjunct) adjuncts.get(i);

            IToken previous_token = prsStream.getIToken(adjunct.getTokenIndex()),
                   next_token = prsStream.getIToken(prsStream.getNext(previous_token.getTokenIndex())),
                   comments[] = previous_token.getFollowingAdjuncts();

            for (int k = 0; k < comments.length; k++)
            {
                Adjunct comment = (Adjunct) comments[k];
                if (comment.getEndLine() > comment.getLine())
                {
                    IToken gate_token = k + 1 < comments.length ? comments[k + 1] : next_token;
                    makeAnnotationWithOffsets(comment.getStartOffset(),
                                              gate_token.getLine() > comment.getEndLine()
                                                  ? lexStream.getLineOffset(gate_token.getLine() - 1)
                                                  : comment.getEndOffset());
                }
            }

            i += comments.length;
		}    
    }
    
	/*
	 * A visitor for ASTs.  Its purpose is to create ProjectionAnnotations
	 * for regions of text corresponding to various types of AST node or to
	 * text ranges computed from AST nodes.  Projection annotations appear
	 * in the editor as the widgets that control folding.
	 */
	private class FoldingVisitor extends AbstractVisitor
	{
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
			makeAnnotation(n);
			return true;
		}
	};
   
	
	// When instantiated will provide a concrete implementation of an abstract method
	// defined in FolderBase
	public void sendVisitorToAST(HashMap newAnnotations, List annotations, Object ast) {
		$AST_NODE$ theAST= ($AST_NODE$) ast;
                prsStream = theAST.getLeftIToken().getPrsStream();
		AbstractVisitor abstractVisitor = new FoldingVisitor();
		theAST.accept(abstractVisitor);
                makeAdjunctAnnotations(theAST);		
	}
}
