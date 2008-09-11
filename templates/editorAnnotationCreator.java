package $PACKAGE_NAME$;

import java.util.Iterator;

import org.eclipse.imp.editor.UniversalEditor;	// Only used for default annotation type
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;

public class $EDITOR_ANOTATION_CREATOR_CLASS_NAME$ implements IMessageHandler {
    private final ITextEditor fEditor;
    private final String fAnnotationType;

    public $EDITOR_ANOTATION_CREATOR_CLASS_NAME$(ITextEditor textEditor, String annotationType) {
        fEditor= textEditor;
        if (annotationType == null)
        	// TODO:  Set the default value for the annotation type,
        	// using whatever source is appropriate
        	fAnnotationType = UniversalEditor.PARSE_ANNOTATION_TYPE;
        else 
        	fAnnotationType= annotationType;
    }

    public void startMessageGroup(String groupName) { }
    public void endMessageGroup() { }

    public void handleSimpleMessage(String message, int startOffset, int endOffset,
            int startCol, int endCol,
            int startLine, int endLine) {

        IAnnotationModel model= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
        Annotation annotation= new Annotation(fAnnotationType, false, message);
        
        Position pos= new Position(startOffset, endOffset - startOffset + 1);

        model.addAnnotation(annotation, pos);
    }

    public void removeAnnotations() {
        IAnnotationModel model= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());

        if (model == null)
            return;

        for(Iterator i= model.getAnnotationIterator(); i.hasNext();) {
            Annotation a= (Annotation) i.next();

            if (a.getType().equals(fAnnotationType))
                model.removeAnnotation(a);
        }
    }

    public void clearMessages() {
        removeAnnotations();
    }
}
