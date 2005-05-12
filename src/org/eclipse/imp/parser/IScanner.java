package org.eclipse.uide.parser;
/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

import java.io.IOException;


public interface IScanner {
	public abstract IToken getNextToken() throws IOException;
}