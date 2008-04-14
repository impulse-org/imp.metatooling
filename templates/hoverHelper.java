package $PACKAGE_NAME$;

import java.util.List;

import lpg.runtime.IToken;

import org.eclipse.imp.editor.AnnotationHoverBase;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.IDocumentationProvider;
import org.eclipse.imp.services.IHoverHelper;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.services.base.HoverHelperBase;
import org.eclipse.imp.utils.ExtensionException;
import org.eclipse.imp.utils.ExtensionFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;

import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;
import $PARSER_PKG$.Ast.*;

public class $HOVER_HELPER_CLASS_NAME$ extends HoverHelperBase implements IHoverHelper
{
    IReferenceResolver fResolver = null;

    public String getHoverHelpAt(IParseController parseController, ISourceViewer srcViewer, int offset)
    {
        // If there are any annotations associated with the line that contains
        // the given offset, return those
        try {
            List annotations = AnnotationHoverBase.getSourceAnnotationsForLine(srcViewer, srcViewer.getDocument().getLineOfOffset(offset));
            if (annotations != null && annotations.size() > 0) {
            	// Some annotations have no text, such as breakpoint annotations;
            	// if that's all we have, then don't bother returning it
				String msg = AnnotationHoverBase.formatAnnotationList(annotations);
				if (msg != null) {
					return msg;
				}
			}
        } catch (BadLocationException e) {
            return "??? (BadLocationException for annotation)";
        }
    
        // Otherwise, return a message determined directly or indirectly based
        // on the node whose representation occurs at the given offset
    	
        // Get the current AST; no AST implies no message
        Object ast = parseController.getCurrentAst();
        if (ast == null) return null;

        // Declare variables used in formulating the message
        Object sourceNode = null;    // node at current hover point
        Object targetNode = null;    // node referenced from current hover point
        Object helpNode = null;      // node for which a help message is to be constructed
        String msg = null;           // the help message for helpNode
        
        // Get the node at the given offset; no node implies no message
        ISourcePositionLocator nodeLocator= parseController.getNodeLocator();
        sourceNode = nodeLocator.findNode(ast, offset);
        if (sourceNode == null)
            return null;

        // Check whether there is a reference resolver for the identified
        // source node; if so, attempt to get the node that is referenced by
        // the source node, on the assumption that the referenced node should
        // be the basis for the help message (e.g., as a decl for an identifier)
        if ( $USE_REFERENCE_RESOLVER$ ) {
            if (fResolver == null && fLanguage != null) {
				try {
					fResolver = ServiceFactory.getInstance().getReferenceResolver(fLanguage);
				} catch (Exception e) {
					$PLUGIN_CLASS$.getInstance().writeErrorMsg("Exception getting Reference Resolver service from service factory");
					fResolver = null;
				}
            }        
            if (fResolver != null) {
                targetNode = fResolver.getLinkTarget(sourceNode, parseController);
            }
        }

        // If the target node is not null, provide help based on that;
        // otherwise, provide help based on the source node
        if (targetNode != null)
            helpNode = targetNode;
        else
            helpNode = sourceNode;
        
        // Now need to determine whether the help message should be determined
        // based on the text represented by the node or based on some separate
        // text provided through an IDocumentationProvider
        
        // Check whether there is a documentation provider for the language;
        // if so, check whether it provides documentation for the help node;
        // if so, return that documentation
        IDocumentationProvider docProvider = null;
        if (fLanguage != null && $USE_DOCUMENTATION_PROVIDER$ ) {
			try {
				docProvider = ServiceFactory.getInstance().getDocumentationProvider(fLanguage);

			} catch (Exception e) {
				$PLUGIN_CLASS$.getInstance().writeErrorMsg("Exception getting Documentation Provider Service from service factory");
				fResolver = null;
			}
        }
        if (docProvider != null) {
                msg = (docProvider != null) ? docProvider.getDocumentation(helpNode, parseController) : null;
                if (msg != null)
                        return msg;
        }
        
        // Otherwise, base the help message on the text that is represented
        // by the help node
        if (helpNode instanceof ASTNode) {
                ASTNode def = (ASTNode) helpNode;
                msg = getSubstring(parseController, def.getLeftIToken().getStartOffset(), def.getRightIToken().getEndOffset());
                int maxMsgLen = 80;
                if (msg == null || msg.length() == 0)
                        return "No help available";
                else if (msg.length() <= maxMsgLen)
                        return msg;
                else
                    return msg.subSequence(0, maxMsgLen) + "...";
        } else {
            return "No help available";
        }
    }

    public static String getSubstring(IParseController parseController, int start, int end) {
        return new String(
        		((SimpleLPGParseController)parseController).getLexer().getLexStream().getInputChars(), start, end-start+1);
    }

    public static String getSubstring(IParseController parseController, IToken token) {
        return getSubstring(parseController, token.getStartOffset(), token.getEndOffset());
    }  
    
}
