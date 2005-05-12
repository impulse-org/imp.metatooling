package org.eclipse.uide.parser;

import java.util.List;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */



/**
 * @author Claffra
 *
 */
public interface IModel {
	
	public abstract Ast getAst();

	public abstract IToken getTokenAtCharacter(int offset);

	public abstract String getString(IToken token);

	public abstract String getString(Ast node);

	public abstract IToken getLastErrorToken();

	public abstract int getTokenCount();
	
	public abstract char[][] getKeywords();

	public abstract boolean isKeywordStart(char c);

    public abstract int getTokenIndexAtCharacter(int i);

    public abstract IToken getTokenAt(int n);

    public abstract boolean isSpace(IToken token);

    public abstract boolean isKeyword(IToken token);

    public abstract List getErrors();
    
    public abstract boolean hasErrors();

    public abstract void setContents(char contents[]);

    public abstract char[] getContents();
	
}