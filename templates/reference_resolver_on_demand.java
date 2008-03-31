package $PACKAGE_NAME$;

import java.util.*;

import org.eclipse.imp.core.ILanguageService;	
import org.eclipse.imp.editor.IReferenceResolver;
import org.eclipse.imp.parser.IParseController;

import lpg.runtime.*;

import $PARSER_PKG$.Ast.*;


public class $REFERENCE_RESOLVER_CLASS_NAME$ implements IReferenceResolver, ILanguageService {

	public $REFERENCE_RESOLVER_CLASS_NAME$ () {
	}
	
	/**
	 * Get the target for a given source node in the AST represented by a
	 * given Parse Controller.
	 */
	public Object getLinkTarget(Object node, IParseController parseController)
	{
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
		
    	if (!(node instanceof Iidentifier)) return null;
    	buildScopeAndDeclStructures(parseController);
    	ASTNode bindingTarget = findDeclForIdentifier((Iidentifier)node);
    	return bindingTarget;
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
	 * Finds the ASTNode for the declaration of the given identifier
	 * reference; returns null if no declaration found
	 * 
	 * @param node	ASTNode for an identifier reference
	 * @return		ASTNode for the declaration of the identifier
	 * 				or null if no declaration found
	 */
	
	public ASTNode findDeclForIdentifier(Iidentifier node)
	{
		for (int i = 0; i < idStack.size(); i++) {
			IdRecord idRec = (IdRecord) idStack.get(i);
			if (idRec.getId() == node)
				return idRec.getDecl();
		}
		return null;
	}
	
	/**
	 * Build the data structures representing information about declarations and
	 * scopes in the AST associated with a given Parse Controller.
	 * 
	 * @param parseController	The parse controller that contains the AST
	 * 							for which the scope and decl structures are
	 * 							to be built
	 */
	public void buildScopeAndDeclStructures(IParseController parseController)
	{
		scopeStack = new Stack();
		declStack = new Stack();
		idStack = new Stack();
		
		ScopeAndDeclVisitor visitor = new ScopeAndDeclVisitor();
		ASTNode ast = (ASTNode) parseController.getCurrentAst();
		ast.accept(visitor);
	}

	
	private Stack scopeStack = null;
	private Stack declStack = null;
	private Stack idStack = null;		// can be a list
	
		
	/**
	 * Intended for the recording of entities and information relating
	 * to the scopes in a program.  Each record represents the AST node
	 * that defines the scope, the starting and ending offsets of the
	 * scope within the source file and the immediate parent and children
	 * of the node. Parents and children are represented as ASTNodes, not
	 * ScopeRecords (so, given an ASTNode, any ScopeRecord to which it
	 * belongs must be discovered by some other means).  The organization
	 * of scopes is assumed to be hierarchical, so a given node may have
	 * at most one parent and any number of children.
	 * 
	 * A structure such as this could be subject to many internal
	 * consistency checks, but for now none of those are performed here.
	 * (Users are assumed to be competent.)
	 */
	public class ScopeRecord
	{
		private ASTNode node = null;
		private int startOffset = -1;
		private int endOffset = -1;
	
		public ScopeRecord() {}
		
		public void setNode(ASTNode node) { this.node = node; }
		public ASTNode getNode() { return node; }
		
		public void setStartOffset(int offset) { this.startOffset = offset; }
		public int getStartOffset() { return startOffset; }

		public void setEndOffset(int offset) { this.endOffset = offset; }
		public int getEndOffset() { return endOffset; }

	}
	

	
	/**
	 * Intended for the recording of entities and information about
	 * declarations in a program.  Each DeclRecord represents a
	 * single declaration (as an ASTNode), the scope in which the
	 * declaration occurs (also represented as an ASTNode), and the
	 * offset of the declaration in its source file.
	 * 
	 * @author sutton
	 *
	 */
	public class DeclRecord
	{
		private ASTNode scope = null;
		private ASTNode decl = null;
		//private String name = null;
		
		public DeclRecord() { }
			
		public DeclRecord(
				ASTNode scope,
				ASTNode decl)
		{
			this.scope = scope;
			this.decl = decl;
		}
		
		public void setScope(ASTNode scope) { this.scope = scope; }
		public ASTNode getScope() { return scope; }
		
		public void setDecl(ASTNode decl) { this.decl = decl; }
		public ASTNode getDecl() { return decl; }

		public int getOffset() {
			int offset = decl.getLeftIToken().getStartOffset();
			return offset; }
	}
	
	
	public class IdRecord {
		Iidentifier id = null;
		ASTNode decl = null;
		
		public IdRecord(Iidentifier id, ASTNode decl) {
			this.id = id;
			this.decl = decl;
		}

		public Iidentifier getId() {
			return id;
		}
		
		public ASTNode getDecl() {
			return decl;
		}	
	}
	
	
	/**
	 * A visitor for ASTs that records information about scopes,
	 * declarations and identifiers.
	 */
	public class ScopeAndDeclVisitor extends AbstractVisitor
	{
  	
       	ScopeAndDeclVisitor() { }
   			
       	// Utility routines to do standard processing for nodes	
       	// that represent declarations, scopes, and identfiers
      	
       	private void nestedScopeVisit(ASTNode node)
       	{
       		ScopeRecord record = new ScopeRecord();
       		record.setNode(node);
       		record.setStartOffset(node.getLeftIToken().getStartOffset());
       		record.setEndOffset(node.getRightIToken().getEndOffset());
       		scopeStack.push(record);
       	}
       	
       	private void nestedScopeEndVisit(ASTNode node)
       	{
      		scopeStack.pop();
       		while (!declStack.empty()) {
       			DeclRecord record = (DeclRecord) declStack.peek();
       			if (record.getScope().equals(node)) {
       				declStack.pop();
       			} else {
       				break;
       			}
       		}
       	}
 
       	
       	private void declVisit(ASTNode node)
       	{
       		DeclRecord record = new DeclRecord();
       		record.setDecl(node);
       		record.setScope(((ScopeRecord)scopeStack.peek()).getNode());
       		declStack.push(record);
       	}
 
       	
       	
       	//private String logging message(String methodName, String stackName, String action)
       	
       	
       	private void identifierVisit(Iidentifier node)
       	{
       		if (node instanceof identifier) {
       			for (int i = declStack.size()-1; i >=0; i--) {
       				DeclRecord declRecord = (DeclRecord) declStack.get(i);
       				ASTNode declNode = declRecord.getDecl();
       				identifier declaredIdentifier = null;
       				if (declNode instanceof declaration) {
       					declaration decl = (declaration) declNode;
       					declaredIdentifier = decl.getidentifier();
       				}
       				if (declaredIdentifier != null) {
       					if (declaredIdentifier.equals(node)) {
       						IdRecord record = new IdRecord(node, declNode);
       						idStack.push(record);
       						return;
       					}
       				}
       			}
       		}
       	}
  
       	// Visit methods
       	
       	public void unimplementedVisitor(String s) {
       	    //System.out.println("ScopeAndDeclVisitor:  Unimplemented visitor:  " + s);
       	}
       	
       	
        	
       	public boolean visit(statementList node) {
    		nestedScopeVisit(node);		
       		return true;
       	}
       	
       	public void endVisit(statementList node) {
       		nestedScopeEndVisit(node);
       	}
 
       	
       	public boolean visit(block node) {
       		nestedScopeVisit(node);
       		return true;
       	}
       	
       	public void endVisit(block node) {
       		nestedScopeEndVisit(node);
       	}
       	
       	public boolean visit(declaration node) {
       		declVisit(node);
       		return true;	
       	}

      	
       	public boolean visit(identifier node) {
  			identifierVisit(node);
       		return false;
       	}
     	
	}		// End ScopeAndDeclVisitor

}
