package org.eclipse.uide.internal.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.editor.IHoverHelper;
import org.eclipse.uide.internal.util.ExensionPointFactory;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IModel;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 */
public class HoverHelpController implements ITextHover, IModelListener {
    private IModel parseResult;
    private IHoverHelper hoverHelper;
    public HoverHelpController() {
    }
    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		try {
			IDocument document = textViewer.getDocument();
			if (parseResult != null)
			    return hoverHelper.getHoverHelpAt(parseResult, hoverRegion.getOffset());
		}
		catch (Throwable e) {
		    ErrorHandler.reportError("Universal Editor Error", e); 
		}
		return null; 
	}
    public void update(IModel parseResult, IProgressMonitor monitor) {
        this.parseResult = parseResult;
    }
    public void setLanguage(Language language) {
		hoverHelper = (IHoverHelper) ExensionPointFactory.createExtensionPoint(language, 
		        "org.eclipse.uide", "hoverHelper");
    }
}