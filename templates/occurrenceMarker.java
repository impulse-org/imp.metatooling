package $PACKAGE_NAME$;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import $AST_PKG$.*;
//import leg.imp.parser.Ast.ASTNode;
//import leg.imp.parser.Ast.ASTNodeToken;
//import leg.imp.parser.Ast.AbstractVisitor;
//import leg.imp.parser.Ast.block;
//import leg.imp.parser.Ast.declaration;
//import leg.imp.parser.Ast.functionCall;
//import leg.imp.parser.Ast.functionDeclaration;
//import leg.imp.parser.Ast.functionDeclarationList;
//import leg.imp.parser.Ast.functionHeader;
//import leg.imp.parser.Ast.identifier;
//import leg.imp.parser.Ast.primitiveType0;
//import leg.imp.parser.Ast.primitiveType1;
//import leg.imp.parser.Ast.primitiveType2;
//import leg.imp.parser.Ast.term0;
//import leg.imp.parser.Ast.term1;
//import leg.imp.parser.Ast.term2;
//import leg.imp.parser.Ast.term3;

import lpg.runtime.IAst;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IOccurrenceMarker;

public class $OCCURRENCE_MARKER_CLASS_NAME$ implements ILanguageService, IOccurrenceMarker {
	private List fOccurrences = Collections.EMPTY_LIST;
	private IAst decl;
	
	public String getKindName() {
		return "$LANG_NAME$ Occurence Marker";
	}

	public List<Object> getOccurrencesOf(IParseController parseController, Object astNode) {
		if (astNode == null) {
			return Collections.EMPTY_LIST;
		}
		
		// Check whether we even have an AST in which to find occurrences
		ASTNode root = (ASTNode) parseController.getCurrentAst();
		if (root == null) {
			return Collections.EMPTY_LIST;
		}
		
		fOccurrences = new ArrayList();
		
		// For those selections where only the occurrence of the given
		// instance is to be marked
		if (nodeKindIndicatesAUniqueOccurrence(astNode)) {
			fOccurrences.add(astNode);
			return fOccurrences;
		}
		
		// For those selections where occurrences of copies of the
		// given instance are to be marked
		if (nodeKindIndicatesLiteralOccurrences(astNode)) {
			root.accept(new LiteralOccurrenceVisitor(astNode));
			return fOccurrences;
		}
				
		// For those selections where the occurrences to be marked are
		// determined by an arbitrary computation based on the given instance
		if (nodeKindIndicatesComputedOccurrences(astNode)) {
			root.accept(new ComputedOccurrenceVisitor(astNode));
			return fOccurrences;
		}
		
		// TODO:  Choose an approach to handling given instances that
		// have not been marked in one of the prior sections
		// One option:  return nothing for these
		//fOccurrences = Collections.EMPTY_LIST;
		// Another option:  return just the given item:
		fOccurrences.add(astNode);
		return fOccurrences;
	}
	
	
	/**
	 * Test whether the given object represents an element in the source
	 * text that should be marked uniquely.
	 * 
	 * @param ast	An instance representing some markable element in the
	 * 				source text
	 * @return		True iff the text should be marked for this instance alone
	 */
	public boolean nodeKindIndicatesAUniqueOccurrence(Object ast) {
		// TODO:  identify AST node types that belong to this category
		// Some examples of syntactic elements that might fall into
		// this category (not an exhaustive list)
		if (ast instanceof block ||
			ast instanceof functionHeader ||
			ast instanceof declaration) 
		{
			return true;
		}
		return false;
	}

	
	/**
	 * Test whether the given object represents an element in the source
	 * text for which all copies should be marked.
	 * 
	 * @param ast	An instance representing some markable element in the
	 * 				source text
	 * @return		True iff all literal occurrences of the text represented
	 * 				by the given object should be marked
	 */
	public boolean nodeKindIndicatesLiteralOccurrences(Object ast) {
		// TODO:  identify AST node types that belong to this category.
		// Some examples are given.
		if (ast instanceof term0 ||				// int values
			ast instanceof term1 ||				// doubles values
			ast instanceof term2 ||				// boolean "true"
			ast instanceof term3 ||				// boolean "false"
			ast instanceof primitiveType0 ||	// "boolean"
			ast instanceof primitiveType1 ||	// "double"
			ast instanceof primitiveType2 ||	// "int"
			(ast instanceof ASTNodeToken && !(ast instanceof identifier)))
												// other tokens, "+", "=", ...,
												// but identifiers are treated separately
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Test whether the given object represents an element in the source
	 * text for which the elements to be marked need to be determined by
	 * some more complicated computation that is not defined a priori
	 * 
	 * @param ast	An instance representing some markable element in the
	 * 				source text
	 * @return		True iff some computation is needed to determine the
	 * 				instances to be marked
	 */
	public boolean nodeKindIndicatesComputedOccurrences(Object ast) {
		// TODO:  identify AST node types that belong to this category.
		// Some examples are given.  For both identifiers and function
		// calls it is assumed that the occurrences to be marked are
		// those of the given instance, the declaration of the given,
		// instance, and the occurrences of other instances with the
		// same declaration.
		if (ast instanceof identifier ||
			ast instanceof functionCall)
		{
			return true;
		}
		return false;
	}
	
	
	/*
	 * A visitor to traverse an AST and search for nodes that match a
	 * given node (that is provided to the constructor).  Matching nodes
	 * are added to a global list fOccurrences.
	 */
	private class LiteralOccurrenceVisitor extends AbstractVisitor
	{	
		// The given node
		Object ast;
		
		LiteralOccurrenceVisitor(Object ast) {
			super();
			this.ast = ast;
		}
		
		@Override
		public void unimplementedVisitor(String s) {}
	
		// TODO:  Include a visit method for each AST node type for which
		// nodeKindIndicatesLiteralOccurrence returns true
		
		// Note:  Depending on the node type, matches between the given node and the
		// visited node may be made based on some value associated with the node
		// (as with numbers) or just based on the type of the node (as with nodes that
		// represent the names of primitive types).
		
		// Note:  Few (if any) of the node types addressed in this visitor will have
		// children, and for those that do, traversal of the children will probably not
		// contribute to occurrence marking.  Therefore, these visit methods will
		// typically return false and endVisit(..) methods will not be unnecessary.
		
		// Example visit(..) methods:
		
		// integer values
		public boolean visit(term0 n) {
			if (ast instanceof term0 &&
				n.getNUMBER().toString().equals(((term0)ast).getNUMBER().toString()))
			{
				fOccurrences.add(n);
			}
			return false;
		}
		
		// double values
		public boolean visit(term1 n) {
			if (ast instanceof term1 &&
					n.getDoubleLiteral().toString().equals(((term1)ast).getDoubleLiteral().toString()))
			{
			fOccurrences.add(n);
			}
			return false;
		}
		
		// term2 == true
		public boolean visit(term2 n) {
			if (ast instanceof term2)
				fOccurrences.add(n);
			return false;
		}
		
		// term 3 == false
		public boolean visit(term3 n) {
			if (ast instanceof term3)
				fOccurrences.add(n);
			return false;
		}
		
		// "boolean"
		public boolean visit(primitiveType0 n) {
			if (ast instanceof primitiveType0)
				fOccurrences.add(n);
			return false;
		}
		
		// "double"
		public boolean visit(primitiveType1 n) {
			if (ast instanceof primitiveType1)
				fOccurrences.add(n);
			return false;
		}
		
		// "int"
		public boolean visit(primitiveType2 n) {
			if (ast instanceof primitiveType2)
				fOccurrences.add(n);
			return false;
		}
		
		// various tokens, including especially operators
		public boolean visit(ASTNodeToken n) {
			if (ast instanceof ASTNodeToken &&
					((ASTNodeToken)ast).toString().equals(n.toString()))
				fOccurrences.add(n);
			return false;
		}

		
	}

	/*
	 * A visitor to traverse an AST and search for nodes that should be
	 * marked along with a given node (that is provided to the constructor).
	 * Identified nodes are added to a global list fOccurrences.
	 * 
	 * This visitor may perform relatively complicated computations and
	 * make use of arbitrary ancillary methods in identifying nodes to
	 * return.
	 */
	private class ComputedOccurrenceVisitor extends AbstractVisitor
	{	
		// The given node
		Object ast;
		
		ComputedOccurrenceVisitor(Object ast) {
			super();
			this.ast = ast;
		}
		
		@Override
		public void unimplementedVisitor(String s) {}
		
		
		// TODO:  Include visit(..) and possibly endVisit(..) methods for each node
		// type that is important in determining the AST nodes that are to be returned
		// by this visitor as targets of occurrence marking.
		
		// NOTE:  Depending on the type of the given AST node, the information that
		// can be obtained from an instance of that type, and the semantics that govern
		// the selection of nodes in relation to instances of that type, the complexity
		// of the calculation required can vary widely.  For instance, it may be sufficient
		// to visit nodes of just one type or necessary to visit nodes of several type,
		// it may or may not require the use of global data structures, and it may or
		// may not be necessary to couple endVisit(..) methods with visit(..) methods.
		
		// NOTE:  Some other considerations to keep in mind:
		// - Some AST node kinds may participate in computations for multiple kinds
		//   of given node.
		// - Some "lower level" AST node kinds may occur in the context of several different
		//   types of "higher level" node kinds where these higher-level kinds require
		//   different kinds of processing at the lower level (so some computations
		//   for a given node kind may need to be context sensitive)
		// - This example uses one visitor to capture information related to two different
		//   types of computed occurrence.  Some complexity is introduced into the visitor
		//   by the need to coordinate these two purposes.  As an alternative, different
		//   computations can be split into different visitors, each of which may be simpler
		//   than their combination.

		
		/*
		 * Visit methods relating to the selection of identifiers and their declarations.
		 * In this case, only nodes for identifiers and (identifier) declarations need to
		 * be visited.  This is possible because the LPG-generated parser has given each
		 * identifier AST node a reference to its corresponding declaration AST node.
		 */
		
		
		// NOTE:  In the following two methods relating to identifiers and their declarations
		// we want to compare the declaration associated with the visited node with the
		// declaration associated with the given node (i.e., the node for which occurrences are
		// being computed).  However, the equality comparison between the declaration node types
		// seems to be value based, so repeated declarations of an identifier, e.g. "int x"
		// all appear to be equal.  However, the different declaration nodes do refer to different
		// tokens that have distinct offsets.  Thus we can use these offsets to distinguish between
		// declarations that are otherwise equal by value.
		
		
		IAst identifierDeclTarget = null;
		int identifierDeclOffset = -2;
		
		
		public boolean visit(identifier n) {
			if (processingFunctionCall)
				return false;
			if (ast instanceof identifier && n.getDeclaration() != null &&
				// test for identity of declarations that may have the same value
				n.getDeclaration().getLeftIToken().getStartOffset() == identifierDeclOffset)
			{
				fOccurrences.add(n);
			}
			return false;
		}

		
		public boolean visit(declaration n) {
			if (processingFunctionCall)
				return false;
			
			if (ast instanceof identifier &&
				// test for identity of declarations that may have the same value
				n.getLeftIToken().getStartOffset() == identifierDeclOffset)
			{
				fOccurrences.add(n);
			}
			return false;
		}
		
		
		/*
		 * Visit methods relating to the selection of function calls and function declarations.
		 * Although the goal is directly analogous to that for identifiers, the implementation
		 * is much more complicated because the AST nodes for function calls to not contain
		 * references to the corresponding function declarations.  As a result, it is necessary
		 * as part of this visitor to build the equivalent of a symbol table for function calls
		 * and declarations.  As implemented here, that entails the use of some global data
		 * structures to store relationships between function calls and declarations, the visiting
		 * of an additional AST node type (functionDeclarationList) to set up these data structures
		 * and extract the results from them, and the use of endVisit(..) methods along with some
		 * of the visit(..) methods.  Additionally, the computations in some of these methods
		 * are comparatively long and involved.
		 */
		
		
		// Fields used in identifying function calls and declarations when the
		// given AST node represents a function call
		
		boolean lookingForFunctionDecls = false;	// Just to flag what we're doing
		boolean processingFunctionCall = false;		// Ditto
		String functionName;						// Name of function used in the call
		Stack<functionDeclaration> functionDecls;	// Stack of declarations of functions with
													// the same name as that used in the call
		functionDeclaration functionDeclTarget;		// The declaration that applies to the given
													// function call
		List<functionCall> callsWithoutDeclarations;	// A list of calls that lack matching declarations
														// (entries probably represent incorrect programs, but
														// if we find any such calls we can store them here)
		Map<functionDeclaration, List<functionCall>> callsWithDeclarations;
														// A map from declarations to calls, where the
														// calls are in the scope of the declaration
														// (this, in effect, is the symbol table)
		
		
		// visit(..) and endVisit(..) methods for AST node types involved in identifying
		// function calls and declarations when the given AST node represents a function call
		
		
		// This is the start construct in the grammar; if we're visiting for
		// a function call then set up the needed data structures
		public boolean visit(functionDeclarationList n) {
			if (ast instanceof functionCall) {
				// Set up for tracking innermost function decls
				lookingForFunctionDecls = true;
				functionName = ((functionCall)ast).getidentifier().getIDENTIFIER().toString();
				functionDeclTarget = null;
				functionDecls = new Stack<functionDeclaration>();
				callsWithoutDeclarations = new ArrayList<functionCall>();
				callsWithDeclarations = new HashMap<functionDeclaration, List<functionCall>>();
			} else if (ast instanceof identifier || ast instanceof declaration) {
				identifierDeclTarget = ((identifier)ast).getDeclaration();
				// may be null if identifier is not declared
				if (identifierDeclTarget != null)
					identifierDeclOffset = identifierDeclTarget.getLeftIToken().getStartOffset();
			}
			return true;
		}
		
	
		// Having traversed the AST, if we've been looking for information related to
		// a function call, then extract the results from our local data structures to
		// the list designated for return values
		public void endVisit(functionDeclarationList n) {
			if (lookingForFunctionDecls) {
				if (functionDeclTarget == null) {
					if (!fOccurrences.contains(ast))
						fOccurrences.add(ast);
				} else {
					fOccurrences.add(functionDeclTarget);
					// The following gives what seems to be an inappropriate compile error:
					//for (List<functionCall> functionCalls:  callsWithDeclarations.get(functionDeclTarget)) {
					List<functionCall> functionCalls = callsWithDeclarations.get(functionDeclTarget);
					for (int i = 0; i < functionCalls.size(); i++) {
						fOccurrences.add(functionCalls.get(i));
					}
				}
				lookingForFunctionDecls = false;
			} else if (ast instanceof identifier || ast instanceof declaration) {
				identifierDeclTarget = null;
			}

		}
		

		// If we're looking for AST nodes related to a function call, then record the
		// information about this call in the appropriate data structures
		public boolean visit(functionCall n) {
			if (ast instanceof functionCall) {
				processingFunctionCall = true;
				if (n.equals(ast)) {
					if (functionDecls.empty()) {
						functionDeclTarget = null;
						callsWithoutDeclarations.add(n);
					} else {
						List<functionCall> callList;
						functionDeclTarget = functionDecls.peek();
						if (!callsWithDeclarations.containsKey(functionDeclTarget)) {
							callList = new ArrayList<functionCall>();
							callList.add(n);
							callsWithDeclarations.put(functionDeclTarget, callList);
						} else {
							callList = callsWithDeclarations.get(functionDeclTarget);
							callList.add(n);
						}
					}
				} else if (n.getidentifier().getIDENTIFIER().toString().equals(functionName)) {
					if (functionDecls.empty()) {
						callsWithoutDeclarations.add(n);
					} else {
						List<functionCall> callList;
						functionDeclaration currentDeclaration = functionDecls.peek();
						if (!callsWithDeclarations.containsKey(currentDeclaration)) {
							callList = new ArrayList<functionCall>();
							callList.add(n);
							callsWithDeclarations.put(currentDeclaration, callList);
						} else {
							callList = callsWithDeclarations.get(currentDeclaration);
							callList.add(n);
						}
					}
				}
			}
			return true;
		}

		
		public void endVisit(functionCall n) {
			if (ast instanceof functionCall) {
				processingFunctionCall = false;
			}
		}
		
		
		
		// If we're looking for information related to a function call,
		// and this function declaration declares a function with the same
		// name as that used in the call, then push it onto the stack of
		// relevant declarations
		public boolean visit(functionDeclaration n) {
			if (lookingForFunctionDecls && 
				n.getfunctionHeader().getidentifier().toString().equals(functionName))
			{
				functionDecls.push(n);
			}
			return true;
		}
		
		// If we're finishing with a function declaration that we've pushed
		// onto the stack of relevant declarations, then pop that declaration
		// from the stack.
		public void endVisit(functionDeclaration n) {
			if (lookingForFunctionDecls && !functionDecls.empty() && functionDecls.peek().equals(n))
				functionDecls.pop();
		}
		
		
		
		
	}
	
	
}
