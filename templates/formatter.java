    package $PACKAGE_NAME$;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ISourceFormatter;

import $AST_PACKAGE$.*;


public class $FORMATTER_CLASS_NAME$ implements ILanguageService, ISourceFormatter {
    private int fIndentSize= 4;
    private String fIndentString;

    public void formatterStarts(String initialIndentation) {
        // Should pick up preferences here
        fIndentSize= 4;
        StringBuffer buff= new StringBuffer(fIndentSize);
        for(int i=0; i < fIndentSize; i++)
            buff.append(' ');
        fIndentString= buff.toString();
    }

    public String format(IParseController parseController, String content, boolean isLineStart, String indentation, int[] positions) {
        final StringBuffer buff= new StringBuffer();
        $AST_NODE$ root= ($AST_NODE$) parseController.getCurrentAst();

        // SMS 9 Aug 2006
        // The original call to root.accept(..) assumes that the AST-related
        // classes (including AbstractVisitor) are generated within the parser
        // class, which (I don't think) is what happens by default now in the
        // usual case (instead they're in the AST package)
        //root.accept(new $CLASS_NAME_PREFIX$Parser.AbstractVisitor() {
        root.accept(new AbstractVisitor() {
            private int prodCount;
            private int prodIndent;
            public void unimplementedVisitor(String s) {
                System.out.println("Unhandled node type: " + s);
            }
            // START_HERE
            // Put in some visit methods with node types
            // appropriate to your AST
            ///*
            public boolean visit(assignmentStmt n) {
                buff.append(fIndentString);
                return true;
            }
			public boolean visit(declaration n) {
				buff.append(fIndentString);
				IprimitiveType primType = n.getprimitiveType();
				if (primType instanceof primitiveType0)
					buff.append(((primitiveType0)primType).getboolean());
				else if (primType instanceof primitiveType1)
					buff.append(((primitiveType1)primType).getdouble());
				else if (primType instanceof primitiveType2)
					buff.append(((primitiveType2)primType).getint());
				buff.append(' ');
				buff.append(n.getidentifier());
				return true;
			}
            //*/
        });

    return buff.toString();
    }

    public void formatterStops() {
    // TODO Auto-generated method stub
    }
}
