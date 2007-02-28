package $PACKAGE_NAME$;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Stack;

import $PARSER_PACKAGE$.*;
import $AST_PACKAGE$.*;
import $PARSER_PACKAGE$.$CLASS_NAME_PREFIX$Parser.SymbolTable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.builder.MarkerCreator;
import org.eclipse.uide.parser.IParseController;

public class $COMPILER_CLASS_NAME$ {
    private static final String sClassNameMacro= "$FILE$";

    private static final String sTemplateHeader= "public class " + sClassNameMacro + " {\n" +
        "\tpublic static void main(String[] args) {\n" +
        "\t\tnew " + sClassNameMacro + "().main();\n" +
        "\t\tSystem.out.println(\"done.\");\n" +
        "\t}\n";

    private static final String sTemplateFooter= "}\n";

    Stack/*<String>*/ fTranslationStack= new Stack();

    private final class TranslatorVisitor extends AbstractVisitor {
    	SymbolTable innerScope;

        public void unimplementedVisitor(String s) {
            // System.err.println("Don't know how to translate node type '" + s + "'.");
        }

        // START_HERE
        // Provide appropriate visitor methods (like the following examples)
        // for the node types in your AST
        ///*
        public void endVisit(statementList n) {
            StringBuffer buff= new StringBuffer();

            for(int i=0; i < n.size(); i++) {
                buff.insert(0, fTranslationStack.pop() + "\n");
            }
            fTranslationStack.push(buff.toString());
        }
        public void endVisit(assignmentStmt n) {
            String rhs= (String) fTranslationStack.pop();
            String lhs= (String) fTranslationStack.pop();
            fTranslationStack.push("\t\t//#line " + n.getRightIToken().getEndLine() + "\n\t\t" + lhs + " = " + rhs + ";" + "\n\t\tSystem.out.println(\"" + lhs + " = \" + " + lhs + ");");
        }
        public void endVisit(expression0 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + "+" + right);
        }
        public void endVisit(expression1 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + "-" + right);
        }
        public void endVisit(expression2 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + "*" + right);
        }
        public void endVisit(expression3 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + "/" + right);
        }
        public void endVisit(expression4 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + ">" + right);
        }
        public void endVisit(expression5 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + "<" + right);
        }
        public void endVisit(expression6 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + " == " + right);
        }
        public void endVisit(expression7 n) {
            String right= (String) fTranslationStack.pop();
            String left= (String) fTranslationStack.pop();
            fTranslationStack.push(left + " != " + right);
        }
        public void endVisit(declaration n) {
        	fTranslationStack.pop(); // discard identifier's trivial translation - we know what it is
            fTranslationStack.push("\t\t//#line " + n.getRightIToken().getEndLine() + "\n\t\t" + n.getprimitiveType() + " " + n.getidentifier() + ";");
        }
        public boolean visit(block n) {
        	innerScope= n.getSymbolTable();
        	return true;
        }
        public void endVisit(block n) {
        	innerScope= innerScope.getParent();
        	String body= (String) fTranslationStack.pop();
        	fTranslationStack.push("{\n" + body + "\t\t}\n");
        }
        public void endVisit(ifStmt n) {
        	String elseStmt= (n.getelseStmtOpt() != null) ? (String) fTranslationStack.pop() : null;
        	String then= (String) fTranslationStack.pop();
        	String cond= (String) fTranslationStack.pop();
        	if (n.getelseStmtOpt() == null)
        		fTranslationStack.push("if (" + cond + ") {\n" + then);
        	else {
        		fTranslationStack.push("if (" + cond + ") {\n" + then + " } else {\n" + elseStmt + "}\n");
        	}
        }
        public void endVisit(whileStmt n) {
        	String body= (String) fTranslationStack.pop();
        	String cond= (String) fTranslationStack.pop();
        	fTranslationStack.push("\t\twhile (" + cond + ") " + body);
        }
        public boolean visit(identifier n) {
            fTranslationStack.push(n.getIDENTIFIER().toString());
            return false;
        }
        public boolean visit(term0 n) {
            fTranslationStack.push(n.getNUMBER().toString());
            return true;
        }
        public void endVisit(functionDeclaration n) {
        	IType retType= n.getType();
        	String body= (String) fTranslationStack.pop();
        	String funcName= n.getidentifier().toString();
        	declarationList formals= n.getparameters();
        	StringBuffer buff= new StringBuffer("\t");
        	buff.append(retType.toString())
        	    .append(' ')
        	    .append(funcName)
        	    .append('(');
        	if (formals != null) {
        		for(int i=0; i < formals.size(); i++) {
        			if (i > 0) buff.append(',');
        			fTranslationStack.pop(); // discard trivial translation of formal arg
        			declaration formal= formals.getdeclarationAt(i);
        			buff.append(formal.getprimitiveType().toString())
        			    .append(' ')
        			    .append(formal.getidentifier().toString());
        		}
        	}
        	buff.append(") ");
        	buff.append(body);
        	buff.append("\n");
        	fTranslationStack.pop(); // discard function name
        	fTranslationStack.push(buff.toString());
        }
        public void endVisit(returnStmt n) {
        	String retVal= (String) fTranslationStack.pop();
        	fTranslationStack.push("\t\treturn " + retVal + ";\n");
        }
        public void endVisit(term1 n) {
        	String funcName= n.getidentifier().toString();
        	functionDeclaration func= (functionDeclaration) innerScope.findDeclaration(funcName);
        	int numArgs= func.getparameters().size();
        	StringBuffer buff= new StringBuffer();
        	buff.append(funcName)
        	    .append('(');
        	Stack actualArgs= new Stack();
        	for(int arg=0; arg < numArgs; arg++) {
        		actualArgs.push(fTranslationStack.pop());
        	}
        	for(int arg=0; arg < numArgs; arg++) {
        		if (arg > 0) buff.append(',');
        		buff.append(actualArgs.pop());
        	}
        	buff.append(")");
        	fTranslationStack.pop(); // discard function name
        	fTranslationStack.push(buff.toString());
        }
        public void endVisit(functionDeclarationList n) {
        	StringBuffer buff= new StringBuffer();
        	for(int i=0; i < n.size(); i++) {
        		buff.append(fTranslationStack.pop());
        	}
        	fTranslationStack.push(buff.toString());
        }
        //*/
    }

    public $COMPILER_CLASS_NAME$() {
        super();
    }

    public String getFileContents(IFile file) {
        char[] buf= null;
        try {
            File javaFile= new File(file.getLocation().toOSString());
            FileReader fileReader= new FileReader(javaFile);
            int len= (int) javaFile.length();

            buf= new char[len];
            fileReader.read(buf, 0, len);
            return new String(buf);
        } catch(FileNotFoundException fnf) {
            System.err.println(fnf.getMessage());
            return "";
        } catch(IOException io) {
            System.err.println(io.getMessage());
            return "";
        }
    }

    public void compile(IFile file, IProgressMonitor mon) {
        IProject project= file.getProject();
        IParseController parseController= new $ParseControllerClassName$();
        
        // Marker creator handles error messages from the parse controller
        MarkerCreator markerCreator = new MarkerCreator(file, parseController, "$CLASS_NAME_PREFIX$.problem");

        parseController.initialize(file.getProjectRelativePath().toString(), project, markerCreator);
        parseController.parse(getFileContents(file), false, mon);

        $AST_NODE$ currentAst= ($AST_NODE$) parseController.getCurrentAst();

        if (currentAst == null) return;

        String fileExten= file.getFileExtension();
        String fileBase= file.getName().substring(0, file.getName().length() - fileExten.length() - 1);

        currentAst.accept(new TranslatorVisitor());

        IFile javaFile= project.getFile(file.getProjectRelativePath().removeFileExtension().addFileExtension("java"));
        String javaSource= sTemplateHeader.replaceAll(sClassNameMacro.replaceAll("\\$", "\\\\\\$"), fileBase) + fTranslationStack.pop() + sTemplateFooter;
        StringBufferInputStream sbis= new StringBufferInputStream(javaSource);

        try {
            if (!javaFile.exists())
                javaFile.create(sbis, true, mon);
            else
                javaFile.setContents(sbis, true, false, mon);
        } catch (CoreException ce) {
            System.err.println(ce.getMessage());
        }
    }
}
