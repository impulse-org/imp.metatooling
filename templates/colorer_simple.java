package $PACKAGE_NAME$;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.imp.services.base.TokenColorerBase;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import $PARSER_PKG$.$CLASS_NAME_PREFIX$Parsersym;

import lpg.runtime.IToken;

public class $COLORER_CLASS_NAME$ extends TokenColorerBase implements $CLASS_NAME_PREFIX$Parsersym, ITokenColorer {
    protected final TextAttribute doubleAttribute, identifierAttribute, keywordAttribute, numberAttribute;
//  protected final TextAttribute commentAttribute, stringAttribute;

    public $COLORER_CLASS_NAME$() {
        super();
        // TODO Define text attributes for the various token types that will have their text colored
        //
        // NOTE: Colors (i.e., instances of org.eclipse.swt.graphics.Color) are system resources
        // and are limited in number.  THEREFORE, it is good practice to reuse existing system Colors
        // or to allocate a fixed set of new Colors and reuse those.  If new Colors are instantiated
        // beyond the bounds of your system capacity then your Eclipse invocation may cease to function
        // properly or at all.
        Display display = Display.getDefault();
        doubleAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_GREEN), null, SWT.BOLD);
        identifierAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_BLACK), null, SWT.NORMAL);
        keywordAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD);
        numberAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_YELLOW), null, SWT.BOLD);
//      commentAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_RED), null, SWT.ITALIC);
//      stringAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_BLUE), null, SWT.BOLD);
    }
    
	public TextAttribute getColoring(IParseController controller, Object o) {
		if (o == null)
			return null;
        IToken token= (IToken) o;
        if (token.getKind() == TK_EOF_TOKEN)
            return null;
        
        switch (token.getKind()) {
        // START_HERE
        case TK_IDENTIFIER:
             return identifierAttribute;
        case TK_NUMBER:
            return numberAttribute;
        case TK_DoubleLiteral:
            return doubleAttribute;
//      case TK_StringLiteral:
//          return stringAttribute;
//		case TK_SINGLE_LINE_COMMENT:
//			return commentAttribute;
        default:
		    if (((SimpleLPGParseController) controller).isKeyword(token.getKind()))
				return keywordAttribute;
        	return super.getColoring(controller, token);
        }
    }

    public IRegion calculateDamageExtent(IRegion seed) {
        return seed;
    }
}
