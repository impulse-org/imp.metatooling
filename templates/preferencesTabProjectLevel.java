package $PREFS_PACKAGE_NAME$;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.imp.preferences.ISafariPreferencesService;
import org.eclipse.imp.preferences.ProjectPreferencesTab;
import org.eclipse.imp.preferences.SafariPreferencesUtilities;
import org.eclipse.imp.preferences.fields.SafariFieldEditor;
import org.osgi.service.prefs.Preferences;
//TODO:  Import additional classes for specific field types from
//org.eclipse.imp.preferences.fields


public class $PREFS_CLASS_NAME$ProjectTab extends ProjectPreferencesTab {

	/*
	 * TODO:  Declare fields for field editors here.
	 * In contrast to the other tabs, with the Project tab the
	 * field editors have to be shared between two methods, so
	 * they are be declared outside of both.
	 */
	// ...	

	
	
	public $PREFS_CLASS_NAME$ProjectTab(ISafariPreferencesService prefService) {
		super(prefService);
	}

	
	
	/**
	 * Creates specific preference fields with settings appropriate to
	 * the Project preferences level.
	 * 
	 * Overrides an unimplemented method in SafariPreferencesTab.
	 * 
	 * @return	An array that contains the created preference fields
	 */
	protected SafariFieldEditor[] createFields(Composite composite) {
		
		// TODO:  Construct the specific fields, including a "details" link
		// for each field; also create "toggle" listeners between fields whose
		// editability is linked.  Add spaces, boxes, etc. as apprpriate.
		//
		// SafariPreferencesUtilities has factory-like methods for creating
		// fields and links of specific types.
		//
		// Among the various parameters that can be set for a Safari preferences
		// field, fields below the default level should generally be removable.
		// 
		// A special consideration on the project level is that preference fields
		// in general should not be enabled (or enableable) when no project is
		// selected.  If the this tab is implemented such that no project will be
		// selected when the tab is first created, then the individual fields in
		// the tab should be created with their enabled state (and editable state,
		// if applicable) set to false.
		
		
		// TODO:  Put the created fields into an array and return it
		SafariFieldEditor fields[] = new SafariFieldEditor[0];		// change length as appropriate
		// 	Add fields here ...
		
		
		return fields;
	}
	
	
	
	/**
	 * Respond to the selection of a project in the project-preferences tab.
	 * 
	 * Overrides an unimplemented method in ProjectPreferencesTab.
	 */
	public void addressProjectSelection(ISafariPreferencesService.ProjectSelectionEvent event, Composite composite)
	{
		// Check that at least one affected preference is non-null
		Preferences oldeNode = event.getPrevious();
		Preferences newNode = event.getNew();
		if (oldeNode == null && newNode == null) {
			// This is what happens for some reason when you clear the project selection,
			// but there shouldn't be anything to do (I don't think), because newNode == null
			// implies that the preferences should be cleared, disabled, etc., and oldeNode
			// == null implies that they should already be cleared, disabled, etc.
			// So, it should be okay to return from here ...
			return;
		}

		
		// If oldeNode is not null, we want to remove any preference-change listeners from it
		// (they're no longer needed, and the new project preferences node will get new listeners)
		boolean haveCurrentListeners = false;
		if (oldeNode != null && oldeNode instanceof IEclipsePreferences && haveCurrentListeners) {
			removeProjectPreferenceChangeListeners();
			haveCurrentListeners = false;
		} else {	
			//System.out.println("JsdivProjectPreferencesPage.SafariProjectSelectionListener.selection():  " +
			//	"\n\tnode is null, not of IEclipsePreferences type, or currentListener is null; not possible to add preference-change listener");
		}

		// TODO:  For each preference field, declare a composite to hold that field
		// (that is, the parent of the field).  These are not strictly necessary, because
		// the parent can always be obtained from the field, but for repeated accesses of
		// the parents these local variables are convenient and efficient.
		// ...


		
		// If we have a new project preferences node, then set up the project's preferences
		if (newNode != null && newNode instanceof IEclipsePreferences) {
			// Set project name in the selected-project field
			selectedProjectName.setStringValue(newNode.name());
			
			// If the containing composite is not disposed, then set the field
			// values and make them enabled and editable	

			// Not entirely sure why the composite could or should be disposed if we're here,
			// but it happens sometimes when selecting a project for the second time, i.e.,
			// after having selected a project once, clicked "OK", and then brought up the
			// dialog and selected a project again.  PERHAPS there is a race condition, such
			// that sometimes the project-selection dialog is still overlaying the preferences
			// tab at the time that the listeners try to update the tab.  If the project-selection
			// dialog were still up then the preferences tab would be disposed.
			
			if (!composite.isDisposed()) {
				
				// Note:  Where there are toggles between fields, it is a good idea to set the
				// properties of the dependent field here according to the values they should have
				// based on the independent field.  There should be listeners to take care of 
				// that sort of adjustment once the tab is established, but when properties are
				// first initialized here, the properties may not always be set correctly through
				// the toggle.  I'm not entirely sure why that happens, except that there may be
				// a race condition between the setting of the dependent values by the listener
				// and the setting of those values here.  If the values are set by the listener
				// first (which might be surprising, but may be possible) then they will be
				// overwritten by values set here--so the values set here should be consistent
				// with what the listener would set.
				
				// Used in setting enabled and editable status
				boolean value = false;
				
				// TODO:  For each field
				// 1) assign the parent to a local variable for handy reference (if you're using
				//    these)
				// 2) Call SafariPreferencesUtilities.setField(..) with the field (and it's parent)
				//    to set a value in the field.  (setField(..) will obtain a value from the
				//    preferences store, if there is one set there, or inherit one from a higher
				//    preferences level, if not).
				// 3) Enable the fields as appropriate.  Most fields will be enabled.  If the enabled
				//    state of one field depends on the value of another, then set the first accordingly.
				//    Note also that field may contain multiple controls that may need to be set
				//    individually.  Also, String fields have an editable state that can be set along
				//    with their enabled state.
				// ...
				
				// Since the fields are been freshly initialized,
				// remove indications of prior modifications
				clearModifiedMarksOnLabels();
				
			}
			

			// TODO:  Add a property change listener for each field, so that if the
			// preference is changed in the model the field can be updated (not all 
			// updates to preference values originate through the preference field
			// editor).  Listeners can be added using addProjectPreferenceChangeListners(..)
			// in ProjectPreferencesTab.
			// ...

			// Indicate that there are current listeners
			haveCurrentListeners = true;
		}
		
		// Or if we don't have a new project preferences node ...
		if (newNode == null || !(newNode instanceof IEclipsePreferences)) {
			// For example, when the tab is first brought up, or if the project
			// has been deselected	
			
			// Clear the project name in the selected-project field
			selectedProjectName.setStringValue("none selected");
			
			// Clear the preferences from the store
			prefService.clearPreferencesAtLevel(ISafariPreferencesService.PROJECT_LEVEL);
			
			// Disable fields and make them non-editable
			if (!composite.isDisposed()) {
				// TODO:  set enabled (and editable, if applicable) to false
				// for all fields
				// ...
			}
			
			// Remove listeners
			removeProjectPreferenceChangeListeners();
			haveCurrentListeners = false;
		}
	}
	
}
