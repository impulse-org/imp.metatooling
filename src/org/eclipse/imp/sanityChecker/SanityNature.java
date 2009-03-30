package org.eclipse.imp.sanityChecker;

import org.eclipse.core.resources.IProjectNature;
import org.eclipse.imp.WizardPlugin;
import org.eclipse.imp.builder.ProjectNatureBase;
import org.eclipse.imp.runtime.IPluginLog;

public class SanityNature extends ProjectNatureBase implements IProjectNature {
    public static final String NATURE_ID= "org.eclipse.imp.metatooling.sanityNature";

    public SanityNature() {}

    @Override
    public String getBuilderID() {
        return SanityChecker.BUILDER_ID;
    }

    @Override
    public IPluginLog getLog() {
        return WizardPlugin.getInstance();
    }

    @Override
    public String getNatureID() {
        return NATURE_ID;
    }

    @Override
    protected void refreshPrefs() {
    }
}
