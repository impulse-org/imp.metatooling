package $PACKAGE_NAME$;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.imp.defaults.DefaultTokenColorer;
import org.eclipse.imp.editor.ITokenColorer;
import org.eclipse.imp.parser.IParseController;
 
import $PARSER_PKG$.$CLASS_NAME_PREFIX$Parsersym;

import lpg.runtime.IToken;

public class $COLORER_CLASS_NAME$ extends $BASE_CLASS$ implements $CLASS_NAME_PREFIX$Parsersym, ITokenColorer {

$TOKEN_ATTRIBUTE_DECLS$
    public TextAttribute getColoring(IParseController controller, IToken token) {
        switch (token.getKind())
        {
            // START_HERE
$TOKEN_ATTRIBUTE_CASES$
//            default:
//                if (controller.isKeyword(token.getKind()))
//                     return keywordAttribute;
            }
        return super.getColoring(controller, token);
    }

    public $COLORER_CLASS_NAME$() {
        super();
        Display display = Display.getDefault();

$TOKEN_ATTRIBUTE_INITS$
//      keywordAttribute = new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD);
    }

    public void setLanguage(String language) {
    }
}
