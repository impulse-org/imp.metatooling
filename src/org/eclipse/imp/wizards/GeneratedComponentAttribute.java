/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.wizards;

import java.io.PrintWriter;

import org.eclipse.pde.internal.core.ischema.ISchema;
import org.eclipse.pde.internal.core.ischema.ISchemaAttribute;
import org.eclipse.pde.internal.core.ischema.ISchemaObject;
import org.eclipse.pde.internal.core.ischema.ISchemaSimpleType;

public class GeneratedComponentAttribute implements ISchemaAttribute, ISchemaObject
{
	public GeneratedComponentAttribute() {}
	
	String 			basedOn = null;
	boolean			deprecated = false;
	String			description = null;
	int				kind = -1;
	String			name = null;
	ISchemaObject 	parent = null;
	ISchema		 	schema = null;
	boolean			translatable = false;
	ISchemaSimpleType type = null;
	int				use = -1;
	Object			value = null;
	

	// ISchemaAttribute
	
	public ISchemaSimpleType getType() { return type; }
	public void setType(ISchemaSimpleType type) {
		this.type = type;
	}
	
	public int getUse() { return use; }
	public void setUse(int use) {
		this.use = use;
	}
	
	public Object getValue() { return value; }
	public void setValue(Object value) {
		this.value = value;
	}
	
	
	
	// IMetaAttribute
	
	public String getBasedOn() { return basedOn; }
	public void setBasedOn(String basedOn) {
		this.basedOn = basedOn;
	}
	
	public boolean isTranslatable() { return translatable; }
	public void setTranslatable(boolean translatable) {
		this.translatable = translatable;
	}
	
	public boolean isDeprecated() { return deprecated; }
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}
	
	public int getKind() { return kind; }
	public void setKind(int kind) {
		this.kind = kind;
	}
	
	
	// ISchemaObject
	
	public String getDescription() { return description; }
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() { return name; }
	public void setName(String name) {
		this.name = name;
	}
	
	public ISchemaObject getParent() { return parent; }
	public void setParent(ISchemaObject parent) {
		this.parent = parent;
	}
	
	public ISchema getSchema() { return schema; }
	public void setSchema(ISchema schema) {
		this.schema = schema;
	}
	
	
	// IAdaptable
	
	public Object getAdapter(Class cls) { return null; }	// punt
	
	
	// IWritable
	
	public void write(String what, PrintWriter where) {
		where.print(what);
	}
}
