package org.eclipse.uide.parser;

import org.eclipse.uide.core.ILanguageService;

import com.ibm.lpg.LexStream;
import com.ibm.lpg.Monitor;
import com.ibm.lpg.PrsStream;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * @author Claffra
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IParser extends ILanguageService {
    /**
     * Run the parser to create a model.
     * @param contents the contents to parse
     * @param sourceViewer the viewer that owns the text
     * @param scanOnly when <b>true</b> only run the lexical scanner to generate tokens.
     * @param scanOnly when <b>false</b> also run the parser to generate a full model.
     * @param monitor stop scanning/parsing when monitor.isCanceled() is true.
     * @return
     */
    public Ast parser(int error_repair_count, Monitor monitor);
    
	//public abstract ILexer getLexer();

	//public abstract LexStream getLexStream();

	public abstract PrsStream getParseStream();

	/**
	 * @return the token kind for the EOF token
	 */
	public int getEOFTokenKind();
	
//	/**
//	 * Minimize the size of the generated AST. Flatten nodes with no siblings into their parent nodes.
//	 * Remove nodes that have no child nodes.
//	 * @return whether or not the parser likes nodes to be flattened
//	 */
//	public abstract boolean shouldFlatten();
//
//	/**
//	 * Indicate whether a given production should be ignored, so that no AST nodes
//	 * are created for it. This will reduce the size of the generated AST. 
//	 * @param ruleName the name of the rule
//	 * @return whether to ignore this rule
//	 */
//	public abstract boolean shouldIgnoreNode(String ruleName);
	
	/**
	 * Inform the parser what language it is parsing.
	 * @param language the name of the language
	 */
	//public void setLanguage(String language);
}