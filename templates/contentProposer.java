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

    public ArrayList getVisibleVariables($CLASS_NAME_PREFIX$Parser parser, ASTNode n) {
        block b = null;
        while (n != null) {
            if (n instanceof block)
            {
                b = (block) n;
                break;
            }
            else n = n.getParent();
        }
        
        ArrayList result = new ArrayList();
        HashSet set = new HashSet();
        for ($CLASS_NAME_PREFIX$Parser.SymbolTable s = (b == null ? parser.getTopLevelSymbolTable() : b.getSymbolTable());
             s != null;
             s = s.getParent())
        {
            for (Enumeration e = s.keys(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                if (! set.contains(key))
                {
                    set.add(key);
                    result.add(s.get(key));
                }
            }
        }

        return result;
    }

    public ICompletionProposal[] getContentProposals(IParseController controller, int offset)
    {
        ArrayList list = new ArrayList();
        //
        //
        //
        $CLASS_NAME_PREFIX$Parser parser = ($CLASS_NAME_PREFIX$Parser) controller.getParser();
        
        //
        // When the offset is in between two tokens (for example, on a white space or comment)
        // the getTokenIndexAtCharacter in parse stream returns the negative index
        // of the token preceding the offset. Here, we adjust this index to point instead
        // to the token following the offset.
        //
        // Note that the controller also has a getTokenIndexAtCharacter and 
        // getTokenAtCharacter. However, these methods won't work here because
        // controller.getTokenAtCharacter(offset) returns null when the offset
        // is not the offset of a valid token; and controller.getTokenAtCharacter(offset) returns
        // the index of the token preceding the offset when the offset is not
        // the offset of a valid token.
        //
        PrsStream prs_stream = parser.getParseStream(); 
        int index = prs_stream.getTokenIndexAtCharacter(offset),
            token_index = (index < 0 ? -(index - 1) : index);
        IToken token = prs_stream.getIToken(token_index),
               candidate = prs_stream.getIToken(prs_stream.getPrevious(token_index));

        //
        // If we are at an offset position immediately following an "identifier"
        // candidate, then consider the candidate to be the token for which we need
        // completion. If the candidate is not the left-hand side of an assignment,
        // we move the candidate and its successor back one token as that still leaves
        // us in the range of the assignment.
        //
        if (candidate.getKind() == $CLASS_NAME_PREFIX$Lexer.TK_IDENTIFIER &&
            token.getKind() != $CLASS_NAME_PREFIX$Lexer.TK_ASSIGN &&
            offset == candidate.getEndOffset() + 1) {
            token = candidate;
            candidate = prs_stream.getIToken(prs_stream.getPrevious(candidate.getTokenIndex()));
        }
        
        String prefix = "";
        if (token.getKind() == $CLASS_NAME_PREFIX$Lexer.TK_IDENTIFIER) {
            if (offset >= token.getStartOffset() && offset <= token.getEndOffset() + 1)
                prefix = token.toString().substring(0, offset - token.getStartOffset());
        }
        
        $CLASS_NAME_PREFIX$ASTNodeLocator locator = new $CLASS_NAME_PREFIX$ASTNodeLocator();
        ASTNode node = (ASTNode) locator.findNode(controller.getCurrentAst(), candidate.getStartOffset(), candidate.getEndOffset()); // offset);
        if (node != null) {
            if (node.getParent() instanceof Iexpression ||
                node.getParent() instanceof assignment ||
                node.getParent() instanceof BadAssignment) {
                ArrayList vars = filterSymbols(getVisibleVariables(parser, node), prefix);
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
