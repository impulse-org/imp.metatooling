package org.eclipse.uide.defaults;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.parser.Ast;
import org.eclipse.uide.parser.IModel;
import org.eclipse.uide.parser.IParser;
import org.eclipse.uide.parser.IToken;
import org.eclipse.uide.parser.ParseError;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 */
public class DefaultParser implements IParser, ILanguageService {

    protected static String message;

    IToken dummy = new DummyToken();
    
    public void setLanguage(String language) {
        ErrorHandler.reportError("No Parser defined for \""+language+"\"");
    }
        
    public IModel parse(String contents, boolean scanOnly, IProgressMonitor monitor) {
        return null;
    }
    public boolean shouldFlatten() {
        return false;
    }
    public boolean shouldIgnoreNode(String ruleName) {
        return false;
    }

    class DefaultModel implements IModel {
        public Ast getAst() {
            return null;
        }
        public IToken getTokenAtCharacter(int offset) {
            return dummy;
        }
        public String getString(IToken token) {
            return message;
        }
        public String getString(Ast node) {
            return message;
        }
        public IToken getLastErrorToken() {
            return dummy;
        }
        public int getTokenCount() {
            return 0;
        }
        public char[][] getKeywords() {
            return null;
        }
        public boolean isKeywordStart(char c) {
            return false;
        }
        public int getTokenIndexAtCharacter(int i) {
            return 0;
        }
        public IToken getTokenAt(int n) {
            return null;
        }
        public boolean isSpace(IToken token) {
            return false;
        }
        public boolean isKeyword(IToken token) {
            return false;
        }
        public boolean hasErrors() {
            return false;
        }
        public List getErrors() {
            ArrayList result = new ArrayList();
            result.add(new ParseError(message, null));
            return result;
        }      
        public void setContents(char[] contents) {
        }
        public char[] getContents() {
            return null;
        }
    }
    
    static class DummyToken implements IToken {
        public Ast getAst() {
             return null;
        }
        public void setAst(Ast child) {
        }
        public void setStartOffset(int offset) {
        }
        public int getStartOffset() {
            return 0;
        }
        public void setEndOffset(int offset) {
        }
        public int getEndOffset() {
            return 0;
        }
        public String toString(char[] contents) {
            return message;
        }
        public boolean equals(char[] contents, String string) {
            return false;
        }
        public boolean equalsIgnoreCase(char[] contents, String string) {
            return false;
        }
        public String getTokenKindName() {
            return null;
        }
        public int getKind() {
            return 0;
        }
    }
}
