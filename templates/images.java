package $PACKAGE_NAME$;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.imp.services.IOutlineImage;


public class $CLASS_NAME_PREFIX$Images implements IOutlineImage
{
	private $CLASS_NAME_PREFIX$Images() { }
	
	private static IOutlineImage image = null;
	
	public static IOutlineImage get$CLASS_NAME_PREFIX$Images() {
		if (image == null) {
			image = new $CLASS_NAME_PREFIX$Images();
		}
		return image;
	}

	public static final String IMAGE_ROOT= "icons";

	public static ImageDescriptor OUTLINE_ITEM_DESC= AbstractUIPlugin.imageDescriptorFromPlugin(
		"$PLUGIN_ID$", IMAGE_ROOT + "/outline_item.gif");
	public static Image OUTLINE_ITEM_IMAGE= OUTLINE_ITEM_DESC.createImage();

	
	public String getImageRoot() {
		return IMAGE_ROOT;
	}

	public ImageDescriptor getOutlineItemDesc() {
		return OUTLINE_ITEM_DESC;
	}

	public Image getOutlineItemImage() {
		return OUTLINE_ITEM_IMAGE;
	}
	
}
