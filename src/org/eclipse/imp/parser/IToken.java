package org.eclipse.uide.parser;
/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


public interface IToken {
	public abstract Ast getAst();

	public abstract void setAst(Ast child);

	public abstract int getStartOffset();

	public abstract int getEndOffset();

	public abstract String toString(char contents[]);

	public abstract boolean equals(char[] contents, String string);

	public abstract boolean equalsIgnoreCase(char[] contents, String string);

	public abstract String getTokenKindName();

	public abstract int getKind();

    public abstract void setStartOffset(int i);

    public abstract void setEndOffset(int i);

}