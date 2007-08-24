/*
 * Created on Oct 27, 2006
 */
package org.eclipse.imp.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;	

import org.eclipse.imp.lpg.parser.ASTUtils;
import org.eclipse.imp.lpg.parser.LPGParser.JikesPG;
import org.eclipse.imp.lpg.parser.LPGParser.terminal;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TokenStyleDialog extends Dialog {
    private final JikesPG fJikesPG;

    /**
     * The map from token image strings to developer-specified font descriptors.
     * This is safe to pass around freely, since FontData structures don't consume
     * any O.S. resources.
     */
    private final Map<String,FontData> fStyleMap= new HashMap<String,FontData>();

    /**
     * The map from token image strings to developer-specified color descriptors.
     * This is safe to pass around freely, since RGB structures don't consume any
     * O.S. resources.
     */
    private final Map<String,RGB> fRGBMap= new HashMap<String,RGB>();

    List<Label> fTokenLabels= new ArrayList<Label>();
    List<FontData> fTokenStyle= new ArrayList<FontData>();
    List<RGB> fTokenColor= new ArrayList<RGB>();

    /**
     * An internal data structure that maps terminal label widgets onto Font structures.
     * Used solely to keep track of Font objects in order to dispose of them properly once
     * they're no longer needed. Important, since Font structures consume O.S. resources.
     */
    private Map<Label,Font> fFontMap;

    /**
     * An internal data structure that maps terminal label widgets onto Color structures.
     * Used solely to keep track of Color objects in order to dispose of them properly once
     * they're no longer needed. Important, since Color structures consume O.S. resources.
     */
    private Map<Label,Color> fColorMap;

    public TokenStyleDialog(JikesPG jikesPG, Shell parentShell) {
	super(parentShell);
	fJikesPG= jikesPG;
    }

    public TokenStyleDialog(JikesPG jikesPG, IShellProvider parentShell) {
	super(parentShell);
	fJikesPG= jikesPG;
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Change Token Styles");
    }

    @Override
    public boolean close() {
        boolean result= super.close();
        for(Iterator iter= fFontMap.keySet().iterator(); iter.hasNext(); ) {
	    Label label= (Label) iter.next();
	    fFontMap.get(label).dispose();
	}
        for(Iterator iter= fColorMap.keySet().iterator(); iter.hasNext(); ) {
	    Label label= (Label) iter.next();
	    fColorMap.get(label).dispose();
	}
        return result;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
	final Composite area= (Composite)super.createDialogArea(parent);
	// TODO Treat all keywords as a single token kind
	List<terminal> terminals= ASTUtils.getTerminals(fJikesPG);
	fFontMap= new HashMap<Label,Font>();
	fColorMap= new HashMap<Label,Color>();
	GridLayout layout= new GridLayout(3, true);
	area.setLayout(layout);
	Label termColumn= new Label(area, SWT.NULL);
	termColumn.setText("Token");
	termColumn.setFont(new Font(parent.getDisplay(), new FontData("", 10, SWT.BOLD)));
	Label styleColumn= new Label(area, SWT.NULL);
	styleColumn.setText("Style");
	styleColumn.setFont(new Font(parent.getDisplay(), new FontData("", 10, SWT.BOLD)));

	/*Label dummyColumn=*/ new Label(area, SWT.NULL);

	for(Iterator iter= terminals.iterator(); iter.hasNext(); ) {
	    final terminal term= (terminal) iter.next();
	    final Label termLabel= new Label(area, SWT.NULL);
	    final String termString= term.getterminal_symbol().toString();

	    termLabel.setText(termString);

	    final Label termStyle= new Label(area, SWT.NULL);

	    termStyle.setText("sample");

	    Button termStyleButton= new Button(area, SWT.PUSH);
	    termStyleButton.setText("Change...");
	    termStyleButton.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
		    FontDialog dlog= new FontDialog(area.getShell());
		    if (fStyleMap.containsKey(termString)) {
			dlog.setFontList(new FontData[] { fStyleMap.get(termString) });
			dlog.setRGB(fRGBMap.get(termString));
		    }
		    FontData fd= dlog.open();

		    if (fd != null) {
			final Font font= new Font(area.getDisplay(), fd);
			final RGB rgb= dlog.getRGB();
			final Color color= new Color(area.getDisplay(), rgb);
			if (fFontMap.containsKey(termStyle)) {
			    fFontMap.get(termStyle).dispose();
			    fColorMap.get(termStyle).dispose();
			}
			// Update sample text with new font + color
			termStyle.setFont(font);
			termStyle.setForeground(color);
			// Keep track of structures that consume O.S. resources
			fFontMap.put(termStyle, font);
			fColorMap.put(termStyle, color);
			// Maintain structures we return to the client of this dialog
			fStyleMap.put(termString, fd);
			fRGBMap.put(termString, rgb);
			// Figure out how big the sample text ought to be in the new font style
			Rectangle bounds= termStyle.getBounds();
			GC gc= new GC(area);
			gc.setFont(font);
			Point labelSize= gc.stringExtent(termStyle.getText());
			gc.dispose();
			bounds.width= Math.min(32, labelSize.x + 4);
			bounds.height= Math.min(12, labelSize.y + 4);
			termStyle.setBounds(bounds);
			// Re-layout and resize the dialog to accommodate any change in the sample text
			area.layout();
			area.getShell().pack();
		    }
		}
	    });
	}
//	getButton(IDialogConstants.OK_ID).setText("&Ok");
	return area;
    }

    public Map<String, FontData> getStyleMap() {
        return fStyleMap;
    }

    public Map<String, RGB> getColorMap() {
        return fRGBMap;
    }
}
