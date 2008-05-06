/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
//package org.eclipse.pde.internal.core.plugin;
package org.eclipse.pde.internal.core.plugin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.internal.registry.ExtensionHandle;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.registry.Handle;
import org.eclipse.core.internal.registry.IObjectManager;
import org.eclipse.core.internal.registry.KeyedHashSet;
import org.eclipse.core.internal.registry.ReadWriteMonitor;
import org.eclipse.core.internal.registry.RegistryObjectManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.ISharedPluginModel;
import org.eclipse.pde.internal.core.PDECoreMessages;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.plugin.PluginExtension;
import org.eclipse.pde.internal.core.plugin.PluginExtensionPoint;
import org.eclipse.pde.internal.core.plugin.PluginHandler;
import org.eclipse.pde.internal.core.plugin.PluginObject;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class ImpAbstractExtensions extends PluginObject implements IExtensions {

	private static final long serialVersionUID = 1L;

	protected String fSchemaVersion;

	protected List fExtensions = null;
	protected List fExtensionPoints = null;
	boolean fCache = false;

	// SMS 2 May 2008
	public ImpAbstractExtensions() {
		this(false);
	}
	
	public ImpAbstractExtensions(boolean readOnly) {
		fCache = !readOnly;
	}

	public void add(IPluginExtension extension) throws CoreException {
		ensureModelEditable();
		getExtensionsList().add(extension);
		((PluginExtension) extension).setInTheModel(true);
		((PluginExtension) extension).setParent(this);
		fireStructureChanged(extension, IModelChangedEvent.INSERT);
	}

	public void add(IPluginExtensionPoint extensionPoint) throws CoreException {
		ensureModelEditable();
		getExtensionPointsList().add(extensionPoint);
		((PluginExtensionPoint) extensionPoint).setInTheModel(true);
		((PluginExtensionPoint) extensionPoint).setParent(this);
		fireStructureChanged(extensionPoint, IModelChangedEvent.INSERT);
	}

	public IPluginExtensionPoint[] getExtensionPoints() {
		List extPoints = getExtensionPointsList();
		return (IPluginExtensionPoint[]) extPoints.toArray(new IPluginExtensionPoint[extPoints.size()]);
	}

	public IPluginExtension[] getExtensions() {
		List extensions = getExtensionsList();
		return (IPluginExtension[]) extensions.toArray(new IPluginExtension[extensions.size()]);
	}

	public void restoreProperty(String name, Object oldValue, Object newValue) throws CoreException {
		if (name.equals(P_EXTENSION_ORDER)) {
			swap((IPluginExtension) oldValue, (IPluginExtension) newValue);
			return;
		}
		super.restoreProperty(name, oldValue, newValue);
	}

	public void load(IExtensions srcExtensions) {
		addArrayToVector(getExtensionsList(), srcExtensions.getExtensions());
		addArrayToVector(getExtensionPointsList(), srcExtensions.getExtensionPoints());
	}

	protected void addArrayToVector(List vector, Object[] array) {
		for (int i = 0; i < array.length; i++) {
			Object obj = array[i];
			if (obj instanceof PluginObject)
				((PluginObject) obj).setParent(this);
			vector.add(obj);
		}
	}

	public void remove(IPluginExtension extension) throws CoreException {
		ensureModelEditable();
		getExtensionsList().remove(extension);
		((PluginExtension) extension).setInTheModel(false);
		fireStructureChanged(extension, IModelChangedEvent.REMOVE);
	}

	public void remove(IPluginExtensionPoint extensionPoint) throws CoreException {
		ensureModelEditable();
		getExtensionPointsList().remove(extensionPoint);
		((PluginExtensionPoint) extensionPoint).setInTheModel(false);
		fireStructureChanged(extensionPoint, IModelChangedEvent.REMOVE);
	}

	public void reset() {
		resetExtensions();
	}

	public void resetExtensions() {
		fExtensions = null;
		fExtensionPoints = null;
	}

	public int getExtensionCount() {
		return getExtensionsList().size();
	}

	public int getIndexOf(IPluginExtension e) {
		return getExtensionsList().indexOf(e);
	}

	public void swap(IPluginExtension e1, IPluginExtension e2) throws CoreException {
		ensureModelEditable();
		List extensions = getExtensionsList();
		int index1 = extensions.indexOf(e1);
		int index2 = extensions.indexOf(e2);
		if (index1 == -1 || index2 == -1)
			throwCoreException(PDECoreMessages.AbstractExtensions_extensionsNotFoundException);
		extensions.set(index2, e1);
		extensions.set(index2, e2);
		firePropertyChanged(this, P_EXTENSION_ORDER, e1, e2);
	}

	protected void writeChildren(String indent, String tag, Object[] children, PrintWriter writer) {
		writer.println(indent + "<" + tag + ">"); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < children.length; i++) {
			IPluginObject obj = (IPluginObject) children[i];
			obj.write(indent + "   ", writer); //$NON-NLS-1$
		}
		writer.println(indent + "</" + tag + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected boolean hasRequiredAttributes() {
		// validate extensions
		List extensions = getExtensionsList();
		int size = extensions.size();
		for (int i = 0; i < size; i++) {
			IPluginExtension extension = (IPluginExtension) extensions.get(i);
			if (!extension.isValid())
				return false;
		}
		// validate extension points
		List extPoints = getExtensionPointsList();
		size = extPoints.size();
		for (int i = 0; i < size; i++) {
			IPluginExtensionPoint expoint = (IPluginExtensionPoint) extPoints.get(i);
			if (!expoint.isValid())
				return false;
		}
		return true;
	}

	public String getSchemaVersion() {
		if (fSchemaVersion == null) {
			// since schema version is only needed on workspace models in very few situations, reading information from the file should suffice
			ISharedPluginModel model = getModel();
			if (model != null) {
				org.eclipse.core.resources.IResource res = model.getUnderlyingResource();
				if (res != null && res instanceof IFile) {
					try {
						InputStream stream = new BufferedInputStream(((IFile) res).getContents(true));
						PluginHandler handler = new PluginHandler(true);
						SAXParserFactory.newInstance().newSAXParser().parse(stream, handler);
						return handler.getSchemaVersion();
					} catch (CoreException e) {
					} catch (SAXException e) {
					} catch (IOException e) {
					} catch (ParserConfigurationException e) {
					}
				}
			}
		}
		return fSchemaVersion;
	}

	public void setSchemaVersion(String schemaVersion) throws CoreException {
		ensureModelEditable();
		String oldValue = fSchemaVersion;
		fSchemaVersion = schemaVersion;
		firePropertyChanged(IPluginBase.P_SCHEMA_VERSION, oldValue, schemaVersion);
	}

	protected List getExtensionsList() {
		if (fExtensions == null) {
			IPluginBase base = getPluginBase();
			
			if (base != null) {
				if (fCache) {
					fExtensions = new ArrayList(Arrays.asList(findExtensionsForPlugin(base.getPluginModel())));
				} else {
					return Arrays.asList(findExtensionsForPlugin(base.getPluginModel()));
				}
			} else {
				return Collections.EMPTY_LIST;
			}
			
			// Original
//			if (base != null) {
//				if (fCache)
//					fExtensions = new ArrayList(Arrays.asList(PDECore.getDefault().getExtensionsRegistry().findExtensionsForPlugin(base.getPluginModel())));
//				else
//					return Arrays.asList(PDECore.getDefault().getExtensionsRegistry().findExtensionsForPlugin(base.getPluginModel()));
//			} else {
//				return Collections.EMPTY_LIST;
//			}
		}
		return fExtensions;
	}

	

	// PDEExtensionRegistry
	public IPluginExtension[] findExtensionsForPlugin(IPluginModelBase base) {
//		IContributor contributor = fStrategy.createContributor(base);
//		if (contributor == null)
//			return new IPluginExtension[0];
//		IExtension[] extensions = getRegistry().getExtensions(fStrategy.createContributor(base));
		IContributor contributor = createContributor(base);
		if (contributor == null)
			return new IPluginExtension[0];
		//IExtension[] extensions = getRegistry().getExtensions(createContributor(base));	
		IExtension[] extensions = getExtensions(createContributor(base));
		
		ArrayList list = new ArrayList();
		for (int i = 0; i < extensions.length; i++) {
			PluginExtension extension = new PluginExtension();	//PluginExtension(extensions[i]);
			try {
				// Not entirely sure what parts of the extension might
				// be needed here
				extension.setId(extensions[i].getUniqueIdentifier());
				extension.setPoint(((PluginExtension)extensions[i]).getPoint());
			} catch (CoreException e) {
				continue;
			}
			
			extension.setModel(getExtensionsModel(base));
			extension.setParent(base.getExtensions());
			list.add(extension);
		}
		return (IPluginExtension[]) list.toArray(new IPluginExtension[list.size()]);
	}
	
	
	public IContributor createContributor(IPluginModelBase base) {
		BundleDescription desc = base == null ? null : base.getBundleDescription();
		// return null if the IPluginModelBase does not have a BundleDescription (since then we won't have a valid 'id')
		if (desc == null)
			return null;
		String name = desc.getSymbolicName();
		String id = Long.toString(desc.getBundleId());
		String hostName = null;
		String hostId = null;

		HostSpecification host = desc.getHost();
		// make sure model is a singleton.  If it is a fragment, make sure host is singleton
		if (host != null && host.getBundle() != null && !host.getBundle().isSingleton() || host == null && !desc.isSingleton())
			return null;
		if (host != null) {
			BundleDescription hostDesc = host.getBundle();
			hostName = hostDesc.getSymbolicName();
			hostId = Long.toString(hostDesc.getBundleId());
		}
		return new RegistryContributor(id, name, hostId, hostName);
	}

	
	// EstensionRegistry
	// used to enforce concurrent access policy for readers/writers
	private ReadWriteMonitor access = new ReadWriteMonitor();
	
	
	// ExtensionRegistry
	public IExtension[] getExtensions(IContributor contributor) {
		if (!(contributor instanceof RegistryContributor))
			throw new IllegalArgumentException(); // should never happen
		String contributorId = ((RegistryContributor) contributor).getActualId();
		access.enterRead();
		try {
//			return registryObjects.getExtensionsFromContributor(contributorId);
			return getExtensionsFromContributor(contributorId);
		} finally {
			access.exitRead();
		}
	}
	
	// PDEExtensionRegistry
	private ISharedPluginModel getExtensionsModel(IPluginModelBase base) {
		if (base instanceof IBundlePluginModelBase)
			return ((IBundlePluginModelBase) base).getExtensionsModel();
		return base;
	}
	
	
	// RegistryObjectManager
	public ExtensionHandle[] getExtensionsFromContributor(String contributorId) {
		int[] ids = getExtensionsFrom(contributorId); // never null
		return (ExtensionHandle[]) getHandles(ids, RegistryObjectManager.EXTENSION);
	}

	// RegistryObjectManager
	//Those two data structures are only used when the addition or the removal of a plugin occurs.
	//They are used to keep track on a contributor basis of the extension being added or removed
	private KeyedHashSet newContributions; //represents the contributers added during this session.
	private Object formerContributions; //represents the contributers encountered in previous sessions. This is loaded lazily.
	
	// RegistryObjectManager
	synchronized int[] getExtensionsFrom(String contributorId) {
//		KeyedElement tmp = newContributions.getByKey(contributorId);
//		if (tmp == null)
//			tmp = getFormerContributions().getByKey(contributorId);
//		if (tmp == null)
//			return EMPTY_INT_ARRAY;
//		return ((Contribution) tmp).getExtensions();
		
		// SMS just returning the empty array since (as stated in comment above)
		// this result should be appropriate except when a plug-in is added or
		// removed--which is not our circumstance
		return new int[0];
	}
	

	// RegistryObjectManager
	//Constants used to get the objects and their handles
	static public final byte CONFIGURATION_ELEMENT = 1;
	static public final byte EXTENSION = 2;
	static public final byte EXTENSION_POINT = 3;
	static public final byte THIRDLEVEL_CONFIGURATION_ELEMENT = 4;
	
	
	// SMS:  added for use in getHandles(..)
	IObjectManager objMan = new RegistryObjectManager(new ExtensionRegistry(null, this, this));
	
	// RegistryObjectManager
	public Handle[] getHandles(int[] ids, byte type) {
		Handle[] results = null;
		int nbrId = ids.length;
		switch (type) {
//			case EXTENSION_POINT :
//				if (nbrId == 0)
//					return ExtensionPointHandle.EMPTY_ARRAY;
//				results = new ExtensionPointHandle[nbrId];
//				for (int i = 0; i < nbrId; i++) {
//					results[i] = new ExtensionPointHandle(this, ids[i]);
//				}
//				break;

			case EXTENSION :
				if (nbrId == 0)
					return new ExtensionHandle[0];						//ExtensionHandle.EMPTY_ARRAY;
				results = new ExtensionHandle[nbrId];
				for (int i = 0; i < nbrId; i++) {
					results[i] = new ExtensionHandle(objMan, ids[i]);	//this, ids[i]);
				}
				break;

//			case CONFIGURATION_ELEMENT :
//				if (nbrId == 0)
//					return ConfigurationElementHandle.EMPTY_ARRAY;
//				results = new ConfigurationElementHandle[nbrId];
//				for (int i = 0; i < nbrId; i++) {
//					results[i] = new ConfigurationElementHandle(this, ids[i]);
//				}
//				break;
//
//			case THIRDLEVEL_CONFIGURATION_ELEMENT :
//				if (nbrId == 0)
//					return ConfigurationElementHandle.EMPTY_ARRAY;
//				results = new ThirdLevelConfigurationElementHandle[nbrId];
//				for (int i = 0; i < nbrId; i++) {
//					results[i] = new ThirdLevelConfigurationElementHandle(this, ids[i]);
//				}
//				break;
		}
		return results;
	}



	
	// Back to the originally scheduled code ...
	
	protected List getExtensionPointsList() {
		if (fExtensionPoints == null) {
			IPluginBase base = getPluginBase();
			
			return new ArrayList();
			
			// Original
//			if (base != null) {
//				if (fCache)
//					fExtensionPoints = new ArrayList(Arrays.asList(PDECore.getDefault().getExtensionsRegistry().findExtensionPointsForPlugin(base.getPluginModel())));
//				else
//					return Arrays.asList(PDECore.getDefault().getExtensionsRegistry().findExtensionPointsForPlugin(base.getPluginModel()));
//			} else {
//				return Collections.EMPTY_LIST;
//			}
		}
		return fExtensionPoints;
	}

	/*
	 * If this function is used to load the model, the extension registry cache will not be used when querying model.
	 */
	protected void processChild(Node child) {
		String name = child.getNodeName();
		if (fExtensions == null)
			fExtensions = new ArrayList();
		if (fExtensionPoints == null)
			fExtensionPoints = new ArrayList();

		if (name.equals("extension")) { //$NON-NLS-1$
			ImpPluginExtension extension = new ImpPluginExtension();
			extension.setModel(getModel());
			extension.setParent(this);
			fExtensions.add(extension);
			extension.setInTheModel(true);
			extension.load(child);
		} else if (name.equals("extension-point")) { //$NON-NLS-1$
			PluginExtensionPoint point = new PluginExtensionPoint();
			point.setModel(getModel());
			point.setParent(this);
			point.setInTheModel(true);
			fExtensionPoints.add(point);
			point.load(child);
		}
	}
}

	
	

