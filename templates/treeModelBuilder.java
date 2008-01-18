package $PACKAGE_NAME$;

import org.eclipse.imp.services.base.TreeModelBuilderBase;

import $AST_PKG$.*;


public class $TREE_MODEL_BUILDER_CLASS_NAME$ extends TreeModelBuilderBase
{
    @Override
    public void visitTree(Object root) {
        ASTNode rootNode= (ASTNode) root;
        $CLASS_NAME_PREFIX$ModelVisitor visitor= new $CLASS_NAME_PREFIX$ModelVisitor();

        rootNode.accept(visitor);
    }

    private class $CLASS_NAME_PREFIX$ModelVisitor extends AbstractVisitor {
        private StringBuffer fRHSLabel;

        @Override
        public void unimplementedVisitor(String s) { }

        public boolean visit(block n) {
        	pushSubItem(n);
            return true;
        }

        public void endVisit(block n) {
            popSubItem();
        }

        public boolean visit(declarationStmt0 n) {
        	createSubItem(n);
        	return true;
        }

        public boolean visit(declarationStmt1 n) {
        	createSubItem(n);
            return true;
        }

        public boolean visit(assignmentStmt n) {
        	createSubItem(n);
            return true;
        }

        @Override
        public boolean visit(functionDeclaration n) {
        	pushSubItem(n);
            return true;
        }
        
        @Override
        public void endVisit(functionDeclaration n) {
            popSubItem();
        }
        
    }
}
