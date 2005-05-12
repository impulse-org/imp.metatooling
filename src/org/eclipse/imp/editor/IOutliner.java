package org.eclipse.uide.editor;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.parser.IModel;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * This interface is to be implemented by contributors to the org.eclipse.uide.outliner extension point.
 * The Universal IDE Editor will locate a suitable parser for the language being edited.
 * The result of the parser, an Ast describing the syntactical elements in the input, is cached
 * and used to show an outline view of the elements in the editor.
 * 
 * @author Claffra
 * @see org.eclipse.uide.defaults.DefaultHoverHelper
 */
public interface IOutliner extends ILanguageService {
    
    /**
     * Create a language-specific outline presentation for the parse result.
     * 
     * @param model	the result from the parser (contains an Ast)
     * @param offset		current offset of the caret in the editor
     */
	public void createOutlinePresentation(IModel model, int offset);
	
	/** 
	 * Set the editor that currently controls the outline view
	 * @param editor
	 */
	public abstract void setEditor(UniversalEditor editor);

    
	/**
	 * Set the tree widget that contains the outline view. The tree is fully managed by this IOutliner instance.
	 * @param tree
	 */
	public abstract void setTree(Tree tree);

	
}