package $PACKAGE_NAME$;

import $PARSER_PKG$.*;
import $AST_PKG$.*;
import lpg.lpgjavaruntime.*;

import java.util.*;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.uide.editor.IContentProposer;
import org.eclipse.uide.editor.SourceProposal;
import org.eclipse.uide.parser.Ast;
import org.eclipse.uide.parser.IParseController;

public class $CONTENT_PROPOSER_CLASS_NAME$ implements IContentProposer
{
    private $CLASS_NAME_PREFIX$Parser.SymbolTable getLocalSymbolTable($CLASS_NAME_PREFIX$Parser parser, ASTNode n) {
        for ( ; n != null; n = n.getParent())
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

    public ICompletionProposal[] getContentProposals(IParseController controller, int offset)
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
