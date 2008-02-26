/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Jul 7, 2006
 */
package $PACKAGE_NAME$;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.imp.language.ILanguageService;
import $PLUGIN_PACKAGE$.$PLUGIN_CLASS$;
import $PLUGIN_PACKAGE$.$RESOURCES_CLASS$;
import org.eclipse.imp.utils.MarkerUtils;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import $AST_PKG$.*;

public class $LABEL_PROVIDER_CLASS_NAME$ implements ILabelProvider
{
    private Set<ILabelProviderListener> fListeners= new HashSet<ILabelProviderListener>();

    private static ImageRegistry sImageRegistry= $PLUGIN_CLASS$.getInstance().getImageRegistry();
    
    private static Image DEFAULT_IMAGE = sImageRegistry.get($RESOURCES_CLASS$.$CLASS_NAME_PREFIX_UPPER$_DEFAULT_IMAGE);
    private static Image FILE_IMAGE = sImageRegistry.get($RESOURCES_CLASS$.$CLASS_NAME_PREFIX_UPPER$_FILE);
    private static Image FILE_WITH_WARNING_IMAGE = sImageRegistry.get($RESOURCES_CLASS$.$CLASS_NAME_PREFIX_UPPER$_FILE_WARNING);
    private static Image FILE_WITH_ERROR_IMAGE = sImageRegistry.get($RESOURCES_CLASS$.$CLASS_NAME_PREFIX_UPPER$_FILE_ERROR);
    

    public Image getImage(Object element) {
    	if (element instanceof IFile) {
    		// TODO:  rewrite to provide more appropriate images
		    IFile file= (IFile) element;
		    int sev= MarkerUtils.getMaxProblemMarkerSeverity(file, IResource.DEPTH_ONE);
	
		    switch(sev) {
		    case IMarker.SEVERITY_ERROR: return FILE_WITH_ERROR_IMAGE;
		    case IMarker.SEVERITY_WARNING: return FILE_WITH_WARNING_IMAGE;
		    default:
			return FILE_IMAGE;
		    }
    	}
    	ASTNode n= (element instanceof ModelTreeNode) ?
	        (ASTNode) ((ModelTreeNode) element).getASTNode() : (ASTNode) element;
	    return getImageFor(n);
    }

    public static Image getImageFor(ASTNode n) {
    	// TODO:  return specific images for specific node
    	// types, as images are available and appropriate
    	return DEFAULT_IMAGE;
    }

    public String getText(Object element) {
    	ASTNode n= (element instanceof ModelTreeNode) ?
            (ASTNode) ((ModelTreeNode) element).getASTNode() :
            (ASTNode) element;

        return getLabelFor(n);
    }

    public static String getLabelFor(ASTNode n) {
		if (n instanceof IcompilationUnit)
		    return "Compilation unit";
		if (n instanceof block)
		    return "Block";
		if (n instanceof assignmentStmt) {
			assignmentStmt stmt = (assignmentStmt) n;
		    return stmt.getidentifier().toString() + "=" + stmt	.getexpression().toString();
		}
		if (n instanceof declarationStmt0) {
			declaration decl= (declaration) ((declarationStmt0)n).getdeclaration();
	        return decl.getprimitiveType() + " " + decl.getidentifier().toString();
		}
		if (n instanceof declarationStmt1) {
			declaration decl= (declaration) ((declarationStmt1)n).getdeclaration();
	        return decl.getprimitiveType() + " " + decl.getidentifier().toString();
		}
		if (n instanceof functionDeclaration) {
			functionHeader hdr = (functionHeader) ((functionDeclaration)n).getfunctionHeader();
			return hdr.getidentifier().toString();
		}
	    return "<???>";
    }

    public void addListener(ILabelProviderListener listener) {
    	fListeners.add(listener);
    }

    public void dispose() {}

    public boolean isLabelProperty(Object element, String property) {
    	return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    	fListeners.remove(listener);
    }
}
