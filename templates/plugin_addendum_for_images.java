   
	// Definitions for image management

	public static final org.eclipse.core.runtime.IPath ICONS_PATH=
		new org.eclipse.core.runtime.Path("icons/"); //$NON-NLS-1$("icons/"); //$NON-NLS-1$

    protected void initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry reg) {
    	org.eclipse.core.runtime.IPath path= ICONS_PATH.append("$CLASS_NAME_PREFIX_LOWER$_default_image.gif");//$NON-NLS-1$
    	org.eclipse.jface.resource.ImageDescriptor imageDescriptor= createImageDescriptor(getInstance().getBundle(), path);
        reg.put(I$CLASS_NAME_PREFIX$Resources.$CLASS_NAME_PREFIX_UPPER$_DEFAULT_IMAGE, imageDescriptor);

        path= ICONS_PATH.append("$CLASS_NAME_PREFIX_LOWER$_default_outline_item.gif");//$NON-NLS-1$
        imageDescriptor= createImageDescriptor(getInstance().getBundle(), path);
        reg.put(I$CLASS_NAME_PREFIX$Resources.$CLASS_NAME_PREFIX_UPPER$_DEFAULT_OUTLINE_ITEM, imageDescriptor);

        path= ICONS_PATH.append("$CLASS_NAME_PREFIX_LOWER$_file.gif");//$NON-NLS-1$
        imageDescriptor= createImageDescriptor(getInstance().getBundle(), path);
        reg.put(I$CLASS_NAME_PREFIX$Resources.$CLASS_NAME_PREFIX_UPPER$_FILE, imageDescriptor);

        path= ICONS_PATH.append("$CLASS_NAME_PREFIX_LOWER$_file_warning.gif");//$NON-NLS-1$
        imageDescriptor= createImageDescriptor(getInstance().getBundle(), path);
        reg.put(I$CLASS_NAME_PREFIX$Resources.$CLASS_NAME_PREFIX_UPPER$_FILE_WARNING, imageDescriptor);

        path= ICONS_PATH.append("$CLASS_NAME_PREFIX_LOWER$_file_error.gif");//$NON-NLS-1$
        imageDescriptor= createImageDescriptor(getInstance().getBundle(), path);
        reg.put(I$CLASS_NAME_PREFIX$Resources.$CLASS_NAME_PREFIX_UPPER$_FILE_ERROR, imageDescriptor);
    }

    public static org.eclipse.jface.resource.ImageDescriptor createImageDescriptor(
    		org.osgi.framework.Bundle bundle, 
    		org.eclipse.core.runtime.IPath path)
    {
        java.net.URL url= org.eclipse.core.runtime.FileLocator.find(bundle, path, null);
        if (url != null) {
            return org.eclipse.jface.resource.ImageDescriptor.createFromURL(url);
        }
        return null;
    }

    // Definitions for image management end
    
}
