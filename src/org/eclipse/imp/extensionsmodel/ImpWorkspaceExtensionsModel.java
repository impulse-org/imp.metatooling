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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.internal.core.NLResourceHelper;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelProvider;
import org.eclipse.pde.internal.core.plugin.PluginHandler;
import org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;
import org.xml.sax.SAXException;

public class ImpWorkspaceExtensionsModel
	extends WorkspaceExtensionsModel
	implements IEditableModel, IBundlePluginModelProvider
	{
	private static final long serialVersionUID = 1L;
	private IFile fUnderlyingResource;
	private boolean fDirty;
	private boolean fEditable = true;
	private transient IBundlePluginModelBase fBundleModel;


	protected NLResourceHelper createNLResourceHelper() {
		return new NLResourceHelper("plugin", getNLLookupLocations()); //$NON-NLS-1$
	}
	
	public URL getNLLookupLocation() {
		try {
			return new URL("file:" + getInstallLocation() + "/"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public ImpWorkspaceExtensionsModel(IFile file) {
		super(file);
		fUnderlyingResource = file;
	}
	
	public void fireModelChanged(IModelChangedEvent event) {
		fDirty = true;
		super.fireModelChanged(event);
	}

	public String getContents() {
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		save(writer);
		writer.flush();
		try {
			swriter.close();
		} catch (IOException e) {
		}
		return swriter.toString();
	}
	
	public String getInstallLocation() {
		return fUnderlyingResource.getLocation().removeLastSegments(1).addTrailingSeparator().toOSString();
	}
	
	public IResource getUnderlyingResource() {
		return fUnderlyingResource;
	}

	public boolean isInSync() {
		if (fUnderlyingResource == null)
			return true;
		IPath path = fUnderlyingResource.getLocation();
		if (path == null)
			return false;
		return super.isInSync(path.toFile());
	}

	public boolean isDirty() {
		return fDirty;
	}
	public boolean isEditable() {
		return fEditable;
	}

	public void load() {
		if (fUnderlyingResource == null)
			return;
		if (fUnderlyingResource.exists()) {
			try {
				InputStream stream = fUnderlyingResource.getContents(true);
				load(stream, false);
				stream.close();
			} catch (Exception e) {
				PDECore.logException(e);
			}
		} else {
			getExtensions(true);		
			setLoaded(true);
		}
	}
	
	protected void updateTimeStamp() {
		updateTimeStamp(fUnderlyingResource.getLocation().toFile());
	}

	public void save() {
		if (fUnderlyingResource == null)
			return;
		try {
			String contents = getContents();
			ByteArrayInputStream stream =
				new ByteArrayInputStream(contents.getBytes("UTF8")); //$NON-NLS-1$
			if (fUnderlyingResource.exists()) {
				fUnderlyingResource.setContents(stream, false, false, null);
			} else {
				fUnderlyingResource.create(stream, false, null);
			}
			stream.close();
		} catch (CoreException e) {
			PDECore.logException(e);
		} catch (IOException e) {
		}
	}
	public void save(PrintWriter writer) {
		if (isLoaded()) {
			fExtensions.write("", writer); //$NON-NLS-1$
		}
		fDirty = false;
	}
	public void setDirty(boolean dirty) {
		fDirty = dirty;
	}
	public void setEditable(boolean editable) {
		fEditable = editable;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.plugin.AbstractExtensionsModel#createExtensions()
	 */
//	protected ImpExtensions createExtensions() {
//		ImpExtensions extensions = super.createExtensions();
//		extensions.setIsFragment(fUnderlyingResource.getName().equals("fragment.xml")); //$NON-NLS-1$
//		return extensions;
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return fUnderlyingResource.getName();
	}
	
	public void setBundleModel(IBundlePluginModelBase model) {
		fBundleModel = model;
	}
	
	public IBundlePluginModelBase getBundlePluginModel() {
		return fBundleModel;
	}
	
	
	/////////////////////////////////////////////////////
	
	// Copied down from AbstractExtensionsModel,
	// adapted to use ImpExtensions instead of Extensions,
	// and made to instantiate PluginHandler with "false"
	
	protected ImpExtensions fExtensions;
	
	public synchronized void load(InputStream stream, boolean outOfSync)
	throws CoreException
	{
		if (fExtensions == null) {
			fExtensions = new ImpExtensions();	//createExtensions();
			fExtensions.setModel(this);
			fExtensions.setIsFragment(fUnderlyingResource.getName().equals("fragment.xml")); //$NON-NLS-1$
		}
		fExtensions.reset();
		setLoaded(false);
		try {
			SAXParser parser = getSaxParser();
	
			// Instantiated with "true" in the original, which leads to
			// a reading of truncated models, which we're trying to avoid
			PluginHandler handler = new PluginHandler(false);
			
			parser.parse(stream, handler);
			fExtensions.load(handler.getDocumentElement(), handler.getSchemaVersion());
			setLoaded(true);
			if (!outOfSync)
				updateTimeStamp();
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (FactoryConfigurationError e) {
		} catch (IOException e) {
		}
	}
	
	public IExtensions getExtensions() {
		return getExtensions(true);
	}
	
	public IExtensions getExtensions(boolean createIfMissing) {
		if (fExtensions == null && createIfMissing) {
			fExtensions = createImpExtensions();
			setLoaded(true);
		}
		return fExtensions;
	}

	
	protected ImpExtensions createImpExtensions() {
		ImpExtensions extensions = new ImpExtensions();
		extensions.setModel(this);
		return extensions;
	}
	
}
