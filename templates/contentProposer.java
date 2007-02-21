package $PACKAGE_NAME$;

import $PARSER_PKG$.*;
import $AST_PKG$.*;
import lpg.runtime.*;

import java.util.*;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.uide.editor.IContentProposer;
import org.eclipse.uide.editor.SourceProposal;
import org.eclipse.uide.parser.Ast;
import org.eclipse.uide.parser.IParseController;


public class $CONTENT_PROPOSER_CLASS_NAME$ implements IContentProposer
{
    private $CLASS_NAME_PREFIX$Parser.SymbolTable getLocalSymbolTable($CLASS_NAME_PREFIX$Parser parser, ASTNode n) {
	// SMS 20 Feb 2007  added cast in assignment to n:
        for ( ; n != null; n = (ASTNode) n.getParent())
            if (n instanceof block)
                 return ((block) n).getSymbolTable();
        return parser.getTopLevelSymbolTable();
    }
        
    private HashMap getVisibleVariables($CLASS_NAME_PREFIX$Parser parser, ASTNode n) {
        HashMap map = new HashMap();
        for ($CLASS_NAME_PREFIX$Parser.SymbolTable s = getLocalSymbolTable(parser, n); s != null; s = s.getParent())
            for (Enumeration e = s.keys(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                if (! map.containsKey(key))
                    map.put(key, s.get(key));
            }

        return map;
    }

    private ArrayList filterSymbols(HashMap in_symbols, String prefix)
    {
        ArrayList symbols = new ArrayList();
        for (Iterator i = in_symbols.values().iterator(); i.hasNext(); ) {
            declaration decl = (declaration) i.next();
            String name = decl.getidentifier().toString();
            if (name.length() >= prefix.length() && prefix.equals(name.substring(0, prefix.length())))
                symbols.add(decl);
        }

        return symbols;
    }

    private IToken getToken(IParseController controller, int offset) {
        PrsStream stream = (PrsStream) controller.getParser();
        int index = stream.getTokenIndexAtCharacter(offset),
            token_index = (index < 0 ? -(index - 1) : index),
            previous_index = stream.getPrevious(token_index);
        return stream.getIToken(((stream.getKind(previous_index) == $CLASS_NAME_PREFIX$Lexer.TK_IDENTIFIER ||
                                  controller.isKeyword(stream.getKind(previous_index))) &&
                                 offset == stream.getEndOffset(previous_index) + 1)
                                         ? previous_index
                                         : token_index);
    }

    private String getPrefix(IToken token, int offset) {
        if (token.getKind() == $CLASS_NAME_PREFIX$Lexer.TK_IDENTIFIER)
            if (offset >= token.getStartOffset() && offset <= token.getEndOffset() + 1)
                return token.toString().substring(0, offset - token.getStartOffset());
        return "";
    }


    /**
     * Returns an array of content proposals applicable relative to the AST of the given
     * parse controller at the given position.
     * 
     * (The provided ITextViewer is not used in the default implementation provided here
     * but but is stipulated by the IContentProposer interface for purposes such as accessing
     * the IDocument for which content proposals are sought.)
     * 
     * @param controller	A parse controller from which the AST of the document being edited
     * 						can be obtained
     * @param int			The offset for which content proposals are sought
     * @param viewer		The viewer in which the document represented by the AST in the given
     * 						parse controller is being displayed (may be null for some implementations)
     * @return				An array of completion proposals applicable relative to the AST of the given
     * 						parse controller at the given position
     */
    public ICompletionProposal[] getContentProposals(IParseController controller, int offset, ITextViewer viewer)
    {
        // START_HERE           
        ArrayList list = new ArrayList(); // a list of proposals.
        if (controller.getCurrentAst() != null) {
            IToken token = getToken(controller, offset);        
            String prefix = getPrefix(token, offset);
        
            $CLASS_NAME_PREFIX$ASTNodeLocator locator = new $CLASS_NAME_PREFIX$ASTNodeLocator();
            ASTNode node = (ASTNode) locator.findNode(controller.getCurrentAst(), token.getStartOffset(), token.getEndOffset());
            if (node != null &&
                (node.getParent() instanceof Iexpression ||
                 node.getParent() instanceof assignment ||
                 node.getParent() instanceof BadAssignment)) {
            	HashMap symbols = getVisibleVariables(($CLASS_NAME_PREFIX$Parser) controller.getParser(), node);
            	ArrayList vars = filterSymbols(symbols, prefix);
                for (int i = 0; i < vars.size(); i++) {
                    declaration decl = (declaration) vars.get(i);
                    list.add(new SourceProposal(decl.gettype().toString() + " " + decl.getidentifier().toString(),
                                                decl.getidentifier().toString(),
                                                prefix,
                                                offset));
                }
            }
            else list.add(new SourceProposal("no completion exists for that prefix", "", offset));
        }
        else list.add(new SourceProposal("no info available due to Syntax error(s)", "", offset));

        return (ICompletionProposal[]) list.toArray(new ICompletionProposal[list.size()]);
    }
}
