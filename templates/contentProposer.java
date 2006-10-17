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
    private ArrayList getVisibleVariables($CLASS_NAME_PREFIX$Parser parser, ASTNode n) {
        block b = null;
        while (n != null) {
            if (n instanceof block) {
                b = (block) n;
                break;
            }
            else n = n.getParent();
        }
        
        ArrayList result = new ArrayList();
        HashSet set = new HashSet();
        for ($CLASS_NAME_PREFIX$Parser.SymbolTable s = (b == null ? parser.getTopLevelSymbolTable() : b.getSymbolTable());
             s != null;
             s = s.getParent()) {
            for (Enumeration e = s.keys(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                if (! set.contains(key)) {
                    set.add(key);
                    result.add(s.get(key));
                }
            }
        }

        return result;
    }

    private ArrayList filterSymbols(List in_symbols, String prefix)
    {
        ArrayList symbols = new ArrayList();
        for (int i = 0; i < in_symbols.size(); i++)
        {
            $CLASS_NAME_PREFIX$Parser.Symbol symbol = ($CLASS_NAME_PREFIX$Parser.Symbol) in_symbols.get(i);
            String name = symbol.getName();
            if (name.length() >= prefix.length() && prefix.equals(name.substring(0, prefix.length())))
                symbols.add(symbol);
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
        IToken token = getToken(controller, offset);        
        String prefix = getPrefix(token, offset);
        
        $CLASS_NAME_PREFIX$ASTNodeLocator locator = new $CLASS_NAME_PREFIX$ASTNodeLocator();
        ASTNode node = (ASTNode) locator.findNode(controller.getCurrentAst(), token.getStartOffset(), token.getEndOffset());
        if (node != null) {
            if (node.getParent() instanceof Iexpression ||
                node.getParent() instanceof assignment ||
                node.getParent() instanceof BadAssignment) {
                ArrayList vars = filterSymbols(getVisibleVariables(($CLASS_NAME_PREFIX$Parser) controller.getParser(), node), prefix);
                for (int i = 0; i < vars.size(); i++) {
                    $CLASS_NAME_PREFIX$Parser.Symbol symbol = ($CLASS_NAME_PREFIX$Parser.Symbol) vars.get(i);
                    list.add(new SourceProposal(symbol.getType() + " " + symbol.getName(),
                                                    symbol.getName(),
                                                    prefix,
                                                    offset));
                }
            }
            else list.add(new SourceProposal("no info available", "", offset));
        }
        else list.add(new SourceProposal("no info available due to Syntax error(s)", "", offset));

        return (ICompletionProposal[]) list.toArray(new ICompletionProposal[list.size()]);
    }
}
