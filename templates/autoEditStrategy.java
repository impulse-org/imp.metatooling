package $PACKAGE_NAME$;

import org.eclipse.imp.services.IAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

public class $AUTO_EDIT_CLASS_NAME$ implements IAutoEditStrategy {
    public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
        if (cmd.doit == false)
            return;
        // START_HERE
        if (cmd.length == 0 && cmd.text != null && isLineDelimiter(doc, cmd.text)) {
            smartIndentAfterNewline(doc, cmd);
        } else if (cmd.text.length() == 1) {
            smartIndentOnKeypress(doc, cmd);
        }
    }

    private void smartIndentAfterNewline(IDocument doc, DocumentCommand cmd) {
        // TODO Set fields of 'cmd' to reflect desired action,
        // or do nothing to proceed with cmd as is
    }

    private void smartIndentOnKeypress(IDocument doc, DocumentCommand cmd) {
        // TODO Set fields of 'cmd' to reflect desired action,
        // or do nothing to proceed with cmd as is
    }

    private boolean isLineDelimiter(IDocument doc, String text) {
        String[] delimiters= doc.getLegalLineDelimiters();
        if (delimiters != null) {
            return TextUtilities.equals(delimiters, text) > -1;
        }
        return false;
    }
}
