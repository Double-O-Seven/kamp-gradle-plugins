package ch.leadrian.samp.kamp.textkeygen;

import java.util.Set;

public class TextKeysGeneratorPluginExtension {

    private Object resourcesDirectory;
    private Set<String> packages;

    public Object getResourcesDirectory() {
        return resourcesDirectory;
    }

    public void setResourcesDirectory(Object resourcesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
    }

    public Set<String> getPackages() {
        return packages;
    }

    public void setPackages(Set<String> packages) {
        this.packages = packages;
    }
}
