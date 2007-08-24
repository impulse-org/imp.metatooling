package $DOCUMENTATION_PROVIDER_PACKAGE_NAME$;


import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IDocumentationProvider;

import $PARSER_PKG$.Ast.*;

import $PARSER_PACKAGE$.$LEXER_CLASS_NAME$;


public class $DOCUMENTATION_PROVIDER_CLASS_NAME$ implements IDocumentationProvider, ILanguageService {

    public String getDocumentation(Object target, IParseController parseController) {

    	Object node = target;
	
        if (node == null)
            return null;
        
        if (node instanceof ASTNode) {
        	int tokenKind = ((ASTNode) node).getRightIToken().getKind();
        	
        	switch (tokenKind) {
        	
        	// START HERE
        	// Create a case for each kind of token for which you want
        	// to provide help text and return the text corresponding
        	// to that token kind (such as in the following examples)
        	case $LEXER_CLASS_NAME$.TK_IDENTIFIER:
        		return "This is an identifier";
        	case $LEXER_CLASS_NAME$.TK_NUMBER:
        		return "This is a number";
        			
        	default:
        		//return "No documentation available for token kind = " + tokenKind;
        		return null;
        	}
        }
        
        return null;
    }


    public static String getSubstring(IParseController parseController, int start, int end) {
        return new String(parseController.getLexer().getLexStream().getInputChars(), start, end-start+1);
    }
}
