package $PACKAGE_NAME$;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class $CLASS_NAME_PREFIX$Images {
	public static final String IMAGE_ROOT= "icons";

	public static ImageDescriptor OUTLINE_ITEM_DESC= AbstractUIPlugin.imageDescriptorFromPlugin("$LANG_NAME$", IMAGE_ROOT + "/outline_item.gif");

	public static Image OUTLINE_ITEM_IMAGE= OUTLINE_ITEM_DESC.createImage();
}