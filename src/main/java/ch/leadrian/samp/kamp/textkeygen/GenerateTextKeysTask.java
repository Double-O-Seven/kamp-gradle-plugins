package ch.leadrian.samp.kamp.textkeygen;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectories;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class GenerateTextKeysTask extends DefaultTask {

    private static final Pattern STRINGS_FILE_PATTERN = compile("strings(_[a-z]{2}(_[A-Z]{2})?)?\\.properties");

    public GenerateTextKeysTask() {
        doLast(task -> generateTextKeys());
    }

    @InputFiles
    public List<File> getInputFiles() {
        TextKeysGeneratorPluginExtension extension = getExtension();
        File resourcesDirectoryFile = getResourcesDirectoryFile(extension.getResourcesDirectory());
        return extension
                .getPackages()
                .stream()
                .flatMap(packageName -> getStringsPropertiesFiles(resourcesDirectoryFile, packageName).stream())
                .collect(toList());
    }

    @OutputDirectories
    public List<File> getOutputDirectories() {
        return getExtension().getPackages().stream().map(this::getOutputDirectory).collect(toList());
    }

    private void generateTextKeys() {
        TextKeysGeneratorPluginExtension extension = getExtension();
        File resourcesDirectoryFile = getResourcesDirectoryFile(extension.getResourcesDirectory());
        extension.getPackages().forEach(packageName -> {
            List<File> stringsPropertiesFiles = getStringsPropertiesFiles(resourcesDirectoryFile, packageName);
            generateTextKeys(packageName, stringsPropertiesFiles);
        });
    }

    private void generateTextKeys(String packageName, List<File> stringsPropertiesFiles) {
        Set<String> stringPropertyNames = stringsPropertiesFiles
                .stream()
                .map(File::toPath)
                .map(this::loadProperties)
                .map(Properties::stringPropertyNames)
                .flatMap(Set::stream)
                .collect(toSet());
        TextKeysGeneratorPluginExtension extension = getExtension();
        TextKeysGenerator textKeysGenerator = new TextKeysGenerator();
        Path outputDirectory = getOutputDirectory(packageName).toPath();
        textKeysGenerator.generateTextKeyClasses(packageName, outputDirectory, stringPropertyNames);
    }

    private List<File> getStringsPropertiesFiles(File resourcesDirectoryFile, String packageName) {
        String packagePath = packageName.replaceAll("\\.", File.separator);
        try {
            return Files
                    .list(resourcesDirectoryFile.toPath().resolve(packagePath))
                    .filter(Files::isRegularFile)
                    .filter(path -> STRINGS_FILE_PATTERN.matcher(path.getFileName().toString()).matches())
                    .map(Path::toFile)
                    .collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Properties loadProperties(Path path) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }

    private File getResourcesDirectoryFile(Object resourcesDirectory) {
        requireNonNull(resourcesDirectory);
        if (resourcesDirectory instanceof Path) {
            return ((Path) resourcesDirectory).toFile();
        } else if (resourcesDirectory instanceof File) {
            return (File) resourcesDirectory;
        } else {
            return new File(resourcesDirectory.toString());
        }
    }

    private TextKeysGeneratorPluginExtension getExtension() {
        return getProject().getExtensions().getByType(TextKeysGeneratorPluginExtension.class);
    }

    private File getOutputDirectory(String packageName) {
        return new File(getProject().getBuildDir(), packageName.replaceAll("\\.", File.separator));
    }

}
