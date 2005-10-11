package org.eclipse.uide.parser;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.core.ILanguageService;

public interface IParseController extends ILanguageService
{
    IParser getParser();
    ILexer getLexer();
    Ast getCurrentAst();
    boolean isKeyword(int kind);
    char [][] getKeywords();
    public int getTokenIndexAtCharacter(int offset);
    IASTNodeLocator getNodeLocator();
    boolean hasErrors();
    List getErrors();
    Ast parse (String input, boolean scanOnly, IProgressMonitor monitor);
}