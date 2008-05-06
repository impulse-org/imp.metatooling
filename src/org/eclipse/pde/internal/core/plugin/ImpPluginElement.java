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
package org.eclipse.pde.internal.core.plugin;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.ischema.ISchemaElement;
import org.eclipse.pde.internal.core.plugin.PluginAttribute;
import org.eclipse.pde.internal.core.plugin.PluginElement;
import org.eclipse.pde.internal.core.plugin.PluginExtension;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImpPluginElement extends /*PluginParent*/ PluginElement implements IPluginElement {
	private static final long serialVersionUID = 1L;

	static final String ATTRIBUTE_SHIFT = "      "; //$NON-NLS-1$

	static final String ELEMENT_SHIFT = "   "; //$NON-NLS-1$

	private transient ISchemaElement fElementInfo;

	protected String fText;

	protected Hashtable fAttributes;

	private IConfigurationElement fElement = null;

	public ImpPluginElement() {
	}

	public ImpPluginElement(IConfigurationElement element) {
		fElement = element;
	}

	ImpPluginElement(ImpPluginElement element) {
		setModel(element.getModel());
		setParent(element.getParent());
		fName = element.getName();
		IPluginAttribute[] atts = element.getAttributes();
		for (int i = 0; i < atts.length; i++) {
			PluginAttribute att = (PluginAttribute) atts[i];
			getAttributeMap().put(att.getName(), att.clone());
		}
		fText = element.getText();
		fElementInfo = (ISchemaElement) element.getElementInfo();
		fElement = element.fElement;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof IPluginElement) {
			IPluginElement target = (IPluginElement) obj;
			if (target.getModel().equals(getModel()))
				return false;
			if (target.getAttributeCount() != getAttributeCount())
				return false;
			IPluginAttribute tatts[] = target.getAttributes();
			for (int i = 0; i < tatts.length; i++) {
				IPluginAttribute tatt = tatts[i];
				IPluginAttribute att = (IPluginAttribute) getAttributeMap().get(tatt.getName());
				if (att == null || att.equals(tatt) == false)
					return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	public IPluginElement createCopy() {
		return new ImpPluginElement(this);
	}

	public IPluginAttribute getAttribute(String name) {
		return (IPluginAttribute) getAttributeMap().get(name);
	}

	public IPluginAttribute[] getAttributes() {
		Collection values = getAttributeMap().values();
		IPluginAttribute[] result = new IPluginAttribute[values.size()];
		return (IPluginAttribute[]) values.toArray(result);
	}

	public int getAttributeCount() {
		// if attributes are initialized, don't load the entire map to find the # of elements
		if (fAttributes == null && fElement != null)
			return fElement.getAttributeNames().length;
		return getAttributeMap().size();
	}

	public Object getElementInfo() {
		if (fElementInfo != null) {
			ISchema schema = fElementInfo.getSchema();
			if (schema.isDisposed()) {
				fElementInfo = null;
			}
		}
		if (fElementInfo == null) {
			IPluginObject parent = getParent();
			while (parent != null && !(parent instanceof IPluginExtension)) {
				parent = parent.getParent();
			}
			if (parent != null) {
				PluginExtension extension = (PluginExtension) parent;
				ISchema schema = (ISchema) extension.getSchema();
				if (schema != null) {
					fElementInfo = schema.findElement(getName());
				}
			}
		}
		return fElementInfo;
	}

	public String getText() {
		if (fText == null && fElement != null)
			fText = fElement.getValue();
		return fText;
	}

	void load(Node node) {
		fName = node.getNodeName();
		if (fAttributes == null)
			fAttributes = new Hashtable();
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			IPluginAttribute att = getModel().getFactory().createAttribute(this);
			ImpPluginAttribute att2 = new ImpPluginAttribute(att);
			((ImpPluginAttribute) att2).load(attribute);
			((ImpPluginAttribute) att2).setInTheModel(true);
			this.fAttributes.put(attribute.getNodeName(), att2);
		}

		if (fChildren == null)
			fChildren = new ArrayList();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				ImpPluginElement childElement = new ImpPluginElement();
				childElement.setModel(getModel());
				childElement.setInTheModel(true);
				this.fChildren.add(childElement);
				childElement.setParent(this);
				childElement.load(child);
			} else if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue() != null) {
				String text = child.getNodeValue();
				text = text.trim();
				if (isNotEmpty(text))
					this.fText = text;
			}
		}
	}
	
	
	// SMS 2 May 2008
	static boolean isNotEmpty(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (Character.isWhitespace(text.charAt(i)) == false)
				return true;
		}
		return false;
	}
	

	public void removeAttribute(String name) throws CoreException {
		ensureModelEditable();
		PluginAttribute att = (PluginAttribute) getAttributeMap().remove(name);
		String oldValue = att.getValue();
		if (att != null) {
			att.setInTheModel(false);
		}
		firePropertyChanged(P_ATTRIBUTE, oldValue, null);
	}

	public void setAttribute(String name, String value) throws CoreException {
		ensureModelEditable();
		if (value == null) {
			removeAttribute(name);
			return;
		}
		IPluginAttribute attribute = getAttribute(name);
		if (attribute == null) {
			attribute = getModel().getFactory().createAttribute(this);
			attribute.setName(name);
			getAttributeMap().put(name, attribute);
			((PluginAttribute) attribute).setInTheModel(true);
		}
		attribute.setValue(value);
	}

	public void setElementInfo(ISchemaElement newElementInfo) {
		fElementInfo = newElementInfo;
		if (fElementInfo == null) {
			for (Enumeration atts = getAttributeMap().elements(); atts.hasMoreElements();) {
				PluginAttribute att = (PluginAttribute) atts.nextElement();
				att.setAttributeInfo(null);
			}
		}
	}

	public void setText(String newText) throws CoreException {
		ensureModelEditable();
		String oldValue = fText;
		fText = newText;
		firePropertyChanged(P_TEXT, oldValue, fText);

	}

	public void write(String indent, PrintWriter writer) {
		writer.print(indent);
		writer.print("<" + getName()); //$NON-NLS-1$
		String newIndent = indent + ATTRIBUTE_SHIFT;
		if (getAttributeMap().isEmpty() == false) {
			writer.println();
			for (Iterator iter = getAttributeMap().values().iterator(); iter.hasNext();) {
				IPluginAttribute attribute = (IPluginAttribute) iter.next();
				attribute.write(newIndent, writer);
				if (iter.hasNext())
					writer.println();
			}
		}
		writer.println(">"); //$NON-NLS-1$
		newIndent = indent + ELEMENT_SHIFT;
		IPluginObject[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			IPluginElement element = (IPluginElement) children[i];
			element.write(newIndent, writer);
		}
		if (getText() != null) {
			writer.println(newIndent + getWritableString(getText()));
		}
		writer.println(indent + "</" + getName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected Hashtable getAttributeMap() {
		if (fAttributes == null) {
			fAttributes = new Hashtable();
			if (fElement != null) {
				String[] names = fElement.getAttributeNames();
				for (int i = 0; i < names.length; i++) {
					IPluginAttribute attr = createAttribute(names[i], fElement.getAttribute(names[i]));
					if (attr != null)
						fAttributes.put(names[i], attr);
				}
			}
		}
		return fAttributes;
	}

	private IPluginAttribute createAttribute(String name, String value) {
		if (name == null || value == null)
			return null;
		try {
			IPluginAttribute attr = getPluginModel().getFactory().createAttribute(this);
			if (attr instanceof PluginAttribute)
				((ImpPluginAttribute) attr).load(name, value);
			else {
				attr.setName(name);
				attr.setValue(value);
			}
			return attr;
		} catch (CoreException e) {
		}
		return null;
	}

	protected ArrayList getChildrenList() {
		if (fChildren == null) {
			fChildren = new ArrayList();
			if (fElement != null) {
				IConfigurationElement[] elements = fElement.getChildren();
				for (int i = 0; i < elements.length; i++) {
					ImpPluginElement element = new ImpPluginElement(elements[i]);
					element.setModel(getModel());
					element.setParent(this);
					fChildren.add(element);
				}
			}
		}
		return fChildren;
	}

	public String getName() {
		if (fName == null && fElement != null) {
			fName = fElement.getName();
		}
		return fName;
	}
	
}
