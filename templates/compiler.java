package $PACKAGE_NAME$;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Stack;

import $PARSER_PACKAGE$.*;
import $AST_PACKAGE$.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.builder.MarkerCreator;
import org.eclipse.uide.parser.IParseController;

public class $COMPILER_CLASS_NAME$ {
    private static final String sClassNameMacro= "$FILE$";

    private static final String sTemplateHeader= "public class " + sClassNameMacro + " {\n" +
        "\tpublic static void main(String[] args) {\n";

    private static final String sTemplateFooter=
    	"\t\tSystem.out.println(\"done.\");\n" +
        "\t}\n" +
        "}";

    Stack/*<String>*/ fTranslationStack= new Stack();

    private final class TranslatorVisitor extends AbstractVisitor {
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
            fTranslationStack.push("//#line " + n.getRightIToken().getEndLine() + "\n\t\t" + lhs + " = " + rhs + ";" + "\n\t\tSystem.out.println(\"" + lhs + " = \" + " + lhs + ");");
        }
        public void endVisit(expression0 n) {
            fTranslationStack.push(fTranslationStack.pop() + "+" + fTranslationStack.pop());
        }
        public void endVisit(expression1 n) {
            fTranslationStack.push(fTranslationStack.pop() + "-" + fTranslationStack.pop());
        }
        public boolean visit(declaration n) {
            fTranslationStack.push("//#line " + n.getRightIToken().getEndLine() + "\n\t\t" + n.gettype() + " " + n.getidentifier() + ";");
            return false;
        }
        public boolean visit(identifier n) {
            fTranslationStack.push(n.getIDENTIFIER().toString());
            return false;
        }
        public boolean visit(term n) {
            fTranslationStack.push(n.getNUMBER().toString());
            return true;
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
