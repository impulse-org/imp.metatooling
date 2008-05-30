/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.imp.extensionsmodel;

import java.io.PrintWriter;

import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.plugin.AbstractExtensions;
import org.eclipse.pde.internal.core.plugin.Extensions;
import org.eclipse.pde.internal.core.plugin.ImpAbstractExtensions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImpExtensions extends ImpAbstractExtensions {
	
	private static final long serialVersionUID = 1L;
	
	private boolean fValid;
	private boolean fIsFragment;

	public ImpExtensions() {
	}
	
	// SMS 1 May 2008:  tried during backporting from 3.4 but
	// there's no such constructor in 3.3
//	// SMS 10 Apr 2008
//	public ImpExtensions(boolean readonly) {
//		super(readonly);
//	}
	

	void load(Extensions srcPluginBase) {
		super.load(srcPluginBase);
		fValid = hasRequiredAttributes();
	}

	void load(Node node, String schemaVersion) {
		fSchemaVersion = schemaVersion;
		if (node == null)
			return;
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				processChild(child);
			}
		}
		fValid = hasRequiredAttributes();
	}
	
//	void load(PDEState state, long bundleID) {
//		Node[] nodes = state.getAllExtensions(bundleID);
//		for (int i = 0; i < nodes.length; i++) {
//			Node child = nodes[i];
//			if (child.getNodeType() == Node.ELEMENT_NODE) {
//				processChild(child);
//			}
//		}
//		fValid = hasRequiredAttributes();	
//		fSchemaVersion = state.getSchemaVersion(bundleID);
//	}

	public void reset() {
		super.reset();
		fValid=false;
	}
	
	public boolean isValid() {
		return fValid;
	}

	public void write(String indent, PrintWriter writer) {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
		if (fSchemaVersion != null)
			writer.println("<?eclipse version=\"" + fSchemaVersion + "\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.println(fIsFragment ? "<fragment>" : "<plugin>"); //$NON-NLS-1$ //$NON-NLS-2$
	
		String firstIndent = "   "; //$NON-NLS-1$
	
		Object [] children = getExtensionPoints();
		if (children.length > 0)
			writer.println();
		for (int i = 0; i < children.length; i++) {
			((IPluginExtensionPoint) children[i]).write(firstIndent, writer);
		}
	
		// add extensions
		children = getExtensions();
		if (children.length > 0)
			writer.println();
		for (int i = 0; i < children.length; i++) {
			((IPluginExtension) children[i]).write(firstIndent, writer);
		}
		writer.println();
		writer.println(fIsFragment ? "</fragment>" : "</plugin>"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void setIsFragment(boolean isFragment) {
		fIsFragment = isFragment;
	}
}
