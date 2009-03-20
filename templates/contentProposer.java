package $PACKAGE_NAME$;

import $PARSER_PKG$.*;
import $AST_PKG$.*;
import lpg.runtime.*;

import java.util.*;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.imp.editor.ErrorProposal;
import org.eclipse.imp.editor.SourceProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.parser.SymbolTable;

public class $CONTENT_PROPOSER_CLASS_NAME$ implements IContentProposer {
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
    public ICompletionProposal[] getContentProposals(IParseController ctlr, int offset, ITextViewer viewer) {
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

        if (ctlr.getCurrentAst() != null) {
            IToken token = getToken(ctlr, offset);        
            String prefix = getPrefix(token, offset);
            $CLASS_NAME_PREFIX$Parser parser = ($CLASS_NAME_PREFIX$Parser) ((SimpleLPGParseController) ctlr).getParser();
            ISourcePositionLocator locator = ctlr.getNodeLocator();
            ASTNode node = (ASTNode) locator.findNode(ctlr.getCurrentAst(), token.getStartOffset(), token.getEndOffset());

            if (node != null) {
                result= computeProposals(prefix, node, offset, parser);
            }
        } else {
            result.add(new ErrorProposal("No proposals available - syntax errors", offset));
        }
        return result.toArray(new ICompletionProposal[result.size()]);
    }

    private List<ICompletionProposal> computeProposals(String prefix, ASTNode node,
            int offset, $CLASS_NAME_PREFIX$Parser parser) {
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
        // START_HERE
        if (node.getParent() instanceof Iexpression ||
            node.getParent() instanceof assignmentStmt ||
            node.getParent() instanceof BadAssignment ||
            node.getParent() instanceof returnStmt)
        {
            Map<String, IAst> decls = collectVisibleDecls(
                   parser.getEnclosingSymbolTable(node));
            List<IAst> matchingDecls = filterSymbols(decls, prefix);

            for (IAst decl : matchingDecls) {
                result.add(createProposalForDecl(decl, prefix, offset));
            }
        } else {
            result.add(new ErrorProposal("no completion exists for that prefix", offset));
        }
        return result;
    }

    private Map<String,IAst> collectVisibleDecls(SymbolTable<IAst> innerScope) {
        Map<String,IAst> result = new HashMap<String,IAst>();
        // Move outward from innermost enclosing scope
        for (SymbolTable<IAst> s = innerScope; s != null; s = s.getParent()) {
            for (String key: s.keySet()) {
                if (! result.containsKey(key)) { // omit shadowed decls
                    result.put(key, (IAst) s.get(key));
                }
            }
        }
        return result;
    }

    private List<IAst> filterSymbols(Map<String,IAst> in_symbols, String prefix) {
        List<IAst> symbols = new ArrayList<IAst>();
        for (IAst decl: in_symbols.values()) {
            String name = getNameOf(decl);
            if (name.length() >= prefix.length() && prefix.equals(name.substring(0, prefix.length())))
                symbols.add(decl);
        }
        return symbols;
    }

    private SourceProposal createProposalForDecl(IAst decl, String prefix, int offset) {
        String propDescrip= "", newText= "";

        if (decl instanceof declaration) {
            newText = ((declaration) decl).getidentifier().toString();
            propDescrip = ((declaration) decl).getprimitiveType().toString() + " " + newText;
        } else if (decl instanceof functionDeclaration) {
            functionDeclaration fdecl = (functionDeclaration) decl;
            functionHeader fhdr = fdecl.getfunctionHeader();
            declarationList parameters = fhdr.getparameters();

            newText= fhdr.getidentifier().toString() + "(";
            for (int i = 0; i < parameters.size(); i++)
                newText += ((declaration) parameters.getdeclarationAt(i)).getprimitiveType() +
                           (i < parameters.size() - 1 ? ", " : "");
            newText += ")";
            propDescrip = fhdr.getType().toString() + " " + newText;
        }
        return new SourceProposal(propDescrip, newText, prefix, offset);
    }

    private String getNameOf(IAst decl) {
        if (decl instanceof declaration)
             return ((declaration) decl).getidentifier().toString();
        else if (decl instanceof functionDeclaration)
            return ((functionDeclaration) decl).getfunctionHeader().getidentifier().toString();
        else if (decl instanceof functionHeader)
             return ((functionHeader) decl).getidentifier().toString();
        return "";
    }
    
    // LPG utility methods
    private IToken getToken(IParseController controller, int offset) {
        PrsStream stream = ((SimpleLPGParseController)controller).getParser().getParseStream();
        int index = stream.getTokenIndexAtCharacter(offset),
            token_index = (index < 0 ? -(index - 1) : index),
            previous_index = stream.getPrevious(token_index),
            previous_kind = stream.getKind(previous_index);
        return stream.getIToken(((previous_kind == $CLASS_NAME_PREFIX$Parsersym.TK_IDENTIFIER ||
                                  ((SimpleLPGParseController) controller).isKeyword(previous_kind)) &&
                                 offset == stream.getEndOffset(previous_index) + 1)
                                         ? previous_index
                                         : token_index);
    }

    private String getPrefix(IToken token, int offset) {
        if (token.getKind() == $CLASS_NAME_PREFIX$Parsersym.TK_IDENTIFIER)
            if (offset >= token.getStartOffset() && offset <= token.getEndOffset() + 1)
                return token.toString().substring(0, offset - token.getStartOffset());
        return "";
    }
}
