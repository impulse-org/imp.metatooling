package org.eclipse.uide.editor;

import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.parser.IParseController;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * This interface is to be implemented by contributors to the org.eclipse.uide.hoverHelp extension point.
 * The Universal IDE Editor will locate a suitable parser for the language being edited.
 * The result of the parser, an Ast describing the syntactical elements in the input, is cached
 * and used when the mouse is hovered over elements in the editor.
 * 
 * @author Claffra
 * @see org.eclipse.uide.defaults.DefaultHoverHelper
 */
public interface IHoverHelper extends ILanguageService {
    
    /**
     * Provide language-specific hover help for the token at the given offset.
     * 
     * @param model	the result from the parser (contains an Ast)
     * @param offset		the offset in the original input stream
     * @return a human-readable text string describing what the element at the given offset
     */
	public String getHoverHelpAt(IParseController parseController, int offset);
}
