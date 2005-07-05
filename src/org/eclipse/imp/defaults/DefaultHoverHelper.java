package org.eclipse.uide.defaults;

import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.editor.IHoverHelper;
import org.eclipse.uide.parser.Ast;
import org.eclipse.uide.parser.IModel;
import org.eclipse.uide.parser.IToken;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 *  @author Chris Laffra
 */
public class DefaultHoverHelper implements IHoverHelper {
	
    public void setLanguage(String language) {
        ErrorHandler.reportError("No Hoverhelper defined for \""+language+"\"");
    }
    
	public String getHoverHelpAt(IModel model, int offset) {
		try {
//			Ast ast = model.getAst();
			IToken token = model.getTokenAtCharacter(offset);
			if (token == null)
				return null;
			Ast node = token.getAst();
		    String answer = 
                "This is the default hover helper. Add your own using the UIDE wizard" +
                "\nSee class 'org.eclipse.uide.defaults.DefaultContentProposer'." +
                "\nNow, what can I say about: "+model.getString(token)+"?"+
		    	"\nIt is a token of kind "+token.getTokenKindName()+
		    	"\nAST tree: ";
		    while (node != null) {
				answer += "> "+model.getString(node);
		    	node = node.parent;
		    }
		    answer += "\nDuring parsing, "+model.getTokenCount()+" tokens were created.";
		    IToken lastErrorToken = model.getLastErrorToken();
			if (lastErrorToken != null) {
				int startOffset = lastErrorToken.getStartOffset();
//				int endOffset = lastErrorToken.getEndOffset();
//				String tokenKindName = lastErrorToken.getTokenKindName();
				String value = model.getString(lastErrorToken);
				answer += "\n\nSyntax error at \""+value+"\" at offset "+startOffset;
		    }
		    return answer;
		} catch (Throwable e) {
			ErrorHandler.reportError("Cannot get hover help...", e);
			return "Oops: "+e;
		}
	}

}

