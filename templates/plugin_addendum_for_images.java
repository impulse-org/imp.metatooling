   
	// Definitions for image management

	public static final org.eclipse.core.runtime.IPath ICONS_PATH= new Path("icons/"); //$NON-NLS-1$

    protected void initializeImageRegistry(ImageRegistry reg) {
        IPath path= ICONS_PATH.append("$CLASS_NAME_PREFIX_LOWER$_default_image.gif");//$NON-NLS-1$
        ImageDescriptor imageDescriptor= createImageDescriptor(getInstance().getBundle(), path);
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

    public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
        java.net.URL url= FileLocator.find(bundle, path, null);
        if (url != null) {
            return ImageDescriptor.createFromURL(url);
        }
        return null;
    }

    // Definitions for image management end
    
}
