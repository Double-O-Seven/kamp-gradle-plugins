package ch.leadrian.samp.kamp.textkeygen;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@NonNullApi
public class TextKeysGeneratorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        TextKeysGeneratorPluginExtension extension = project
                .getExtensions()
                .create("textKeyGenerator", TextKeysGeneratorPluginExtension.class);
        project.getTasks().create("generateTextKeys", GenerateTextKeysTask.class);
    }
}
