package $PACKAGE_NAME$;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.IReferenceResolver;


import $PARSER_PKG$.$CLASS_NAME_PREFIX$Parser;
import $PARSER_PKG$.Ast.*;


public class $REFERENCE_RESOLVER_CLASS_NAME$ implements IReferenceResolver {

    public $REFERENCE_RESOLVER_CLASS_NAME$ () {
    }

    /**
      * Get the text associated with the given node for use in a link
      * from (or to) that node
      */
    public String getLinkText(Object node) {
        // TODO:  Replace the call to super.getLinkText(..) with an implementation
        // suitable to your language and link types
        return node.toString();
    }
        
    /**
      * Get the target for the given source node in the AST produced by the
      * given Parse Controller.
      */
    public Object getLinkTarget(Object node, IParseController controller) {
        // START_HERE
        // Replace the given implementation with an implementation
        // that is suitable to your language and reference types
		
        // NOTE:  The code shown in this method body works with the
        // example grammar used in the IMP language-service templates.
        // It may be adaptable for use with other languages.  HOWEVER,
        // this particular code is not essential to reference resolvers
        // in general, and the user should provide an implementation
        // that is appropriate to the language and AST structure for
        // which the service is being defined.
		
        if (node instanceof Iidentifier && controller.getCurrentAst() != null) {
            identifier id = (identifier) node;
            $CLASS_NAME_PREFIX$Parser parser = ($CLASS_NAME_PREFIX$Parser) ((SimpleLPGParseController)controller).getParser();
            $CLASS_NAME_PREFIX$Parser.SymbolTable symtab = parser.getEnclosingSymbolTable(id);
            return symtab.findDeclaration(id.toString());
        }
        
        return null;
    }
}
