package ch.leadrian.samp.kamp.textkeygen;

import java.util.HashSet;
import java.util.Set;

public class TextKeysGeneratorPluginExtension {

    private Object outputDirectory;
    private Object resourcesDirectory;
    private Set<String> packageNames = new HashSet<>();

    public Object getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Object outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Object getResourcesDirectory() {
        return resourcesDirectory;
    }

    public void setResourcesDirectory(Object resourcesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
    }

    public Set<String> getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(Set<String> packageNames) {
        this.packageNames = packageNames;
    }
}
