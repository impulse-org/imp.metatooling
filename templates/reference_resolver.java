package $PACKAGE_NAME$;

import java.util.*;

import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.editor.IReferenceResolver;
import org.eclipse.uide.parser.IParseController;

import lpg.runtime.*;

import $PARSER_PKG$.$CLASS_NAME_PREFIX$Parser;
import $PARSER_PKG$.Ast.*;


public class $CLASS_NAME_PREFIX$ReferenceResolver implements IReferenceResolver, ILanguageService {

    public $CLASS_NAME_PREFIX$ReferenceResolver () {
    }

    /**
      * Get the text associated with a given node for use in a link
      * from (or to) that node
      */
    public String getLinkText(Object node) {
        // TODO:  Replace the call to super.getLinkText(..) with an implementation
        // suitable to you language and link types
        return node.toString();
    }
        
    /**
      * Get the target for a given source node in the AST represented by a
      * given Parse Controller.
      */
    public Object getLinkTarget(Object node, IParseController controller) {
        // START_HERE
        // Replace the given implementation with an implementation
        // that is suitable to you language and link types
		
        // NOTE:  The code shown in this method body works with the
        // example grammar used in the SAFARI language-service templates.
        // It may be adaptable for use with other languages.  HOWEVER,
        // this particular code is not essential to reference resolvers
        // in general, and the user should provide an implementation
        // that is appropriate to the language and AST structure for
        // which the service is being defined.
		
        if (node instanceof Iidentifier && controller.getCurrentAst() != null) {
            Identifier id = (Identifier) node;
            $CLASS_NAME_PREFIX$Parser parser = ($CLASS_NAME_PREFIX$Parser) controller.getParser();
            $CLASS_NAME_PREFIX$Parser.SymbolTable symtab = parser.getEnclosingSymbolTable(id);
            return symtab.findDeclaration(id.toString());
        }
        
        return null;
    }
}
