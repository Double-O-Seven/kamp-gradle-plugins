package ch.leadrian.samp.kamp.textkeygen;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectories;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
        File resourcesDirectoryFile = getFileFromObject(extension.getResourcesDirectory());
        return extension
                .getPackageNames()
                .stream()
                .flatMap(packageName -> getStringsPropertiesFiles(resourcesDirectoryFile, packageName).stream())
                .collect(toList());
    }

    @OutputDirectories
    public List<File> getOutputDirectories() {
        return getExtension().getPackageNames().stream().map(this::getOutputDirectory).collect(toList());
    }

    private void generateTextKeys() {
        TextKeysGeneratorPluginExtension extension = getExtension();
        File resourcesDirectoryFile = getFileFromObject(extension.getResourcesDirectory());
        extension.getPackageNames().forEach(packageName -> {
            List<File> stringsPropertiesFiles = getStringsPropertiesFiles(resourcesDirectoryFile, packageName);
            try {
                generateTextKeys(packageName, stringsPropertiesFiles);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void generateTextKeys(String packageName, List<File> stringsPropertiesFiles) throws IOException {
        Set<String> stringPropertyNames = stringsPropertiesFiles
                .stream()
                .map(File::toPath)
                .map(this::loadProperties)
                .map(Properties::stringPropertyNames)
                .flatMap(Set::stream)
                .collect(toSet());
        TextKeysGenerator textKeysGenerator = new TextKeysGenerator();
        Path outputDirectory = getOutputDirectory(packageName).toPath();

        Files.createDirectories(outputDirectory);
        try (Writer writer = Files.newBufferedWriter(outputDirectory.resolve("TextKeys.java"), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            textKeysGenerator.generateTextKeyClasses("TextKeys", packageName, stringPropertyNames, writer);
        }
    }

    private List<File> getStringsPropertiesFiles(File resourcesDirectoryFile, String packageName) {
        String packagePath = packageNameToPath(packageName);
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

    private File getFileFromObject(Object fileObject) {
        requireNonNull(fileObject);
        if (fileObject instanceof Path) {
            return ((Path) fileObject).toFile();
        } else if (fileObject instanceof File) {
            return (File) fileObject;
        } else {
            return new File(fileObject.toString());
        }
    }

    private TextKeysGeneratorPluginExtension getExtension() {
        return getProject().getExtensions().getByType(TextKeysGeneratorPluginExtension.class);
    }

    private File getOutputDirectory(String packageName) {
        return new File(getFileFromObject(getExtension().getOutputDirectory()), packageNameToPath(packageName));
    }

    @NotNull
    private String packageNameToPath(String packageName) {
        return packageName.replace('.', File.separatorChar);
    }

}
