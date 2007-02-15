package $PACKAGE_NAME$;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uide.defaults.DefaultTokenColorer;
import org.eclipse.uide.editor.ITokenColorer;
import org.eclipse.uide.parser.IParseController;
 
import $PARSER_PKG$.$CLASS_NAME_PREFIX$Parsersym;

import lpg.lpgjavaruntime.IToken;

public class $COLORER_CLASS_NAME$ extends DefaultTokenColorer implements $CLASS_NAME_PREFIX$Parsersym, ITokenColorer {

    TextAttribute commentAttribute, keywordAttribute, stringAttribute, numberAttribute, identifierAttribute;
	
    public TextAttribute getColoring(IParseController controller, IToken token) {
        switch (token.getKind())
        {
            // START_HERE
            case TK_IDENTIFIER:
                 return identifierAttribute;
            case TK_NUMBER:
                 return numberAttribute;
//          case TK_StringLiteral:
//               return stringAttribute;
            default:
                if (controller.isKeyword(token.getKind()))
                     return keywordAttribute;
               else return null;
            }
    }

    public $COLORER_CLASS_NAME$() {
        super();
        // TODO:  Define text attributes for the various	
        // token types that will have their text colored
        Display display = Display.getDefault();
        commentAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_RED), null, SWT.ITALIC);
        stringAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_BLUE), null, SWT.BOLD);
        identifierAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_BLACK), null, SWT.NORMAL);
        numberAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_YELLOW), null, SWT.BOLD);
        keywordAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD);
    }

    public void setLanguage(String language) {
    }
}