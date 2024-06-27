package net.tclproject.mysteriumlib.asm.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Class that enables Mysterium ASMLib to work outside of minecraft.
 * It has a main method that must be executed first.
 * <p/>
 * IMPORTANT: This is not a ready-to-go class, for it to work, you need to implement
 * some form of interaction with the modified classes, but that is up to you.
 */
public class FileASMLib {

    /** public static void main. For now, just calls the process() method. */
    public static void main(String[] args) throws IOException {
        new FileASMLib().transform();
    }

    /** Directory with the original unchanged classes. */
    File originalClasses = new File("classes");
    /** Directory with classes containing fix methods. */
    File fixesDir = new File("fixes");

    /**
     * Registers classes with fix methods, and runs the transformer through the original classes to insert the fixes.
     */
    void transform() throws IOException {
        TargetClassTransformer transformer = new TargetClassTransformer();
        for (File file : getFiles(".class", fixesDir)) {
            transformer.registerClassWithFixes(FileUtils.readFileToByteArray(file));
            // That just registered all the classes in the 'fixes' folder as classes with fix methods.
        }
        for (File file : getFiles(".class", originalClasses)) {
            byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
            String className = ""; // we need to get the class name e.g. com.example.ExampleMod out of a path
            byte[] newBytes = transformer.transform(className, bytes);

            // IMPORTANT: Now you need to do something with newBytes[]. Perhaps, look at how forge loads modified
            // minecraft classes.
        }
    }

    /**
     * Gets a list of files in a directory and all it's subdirectories that end with a specific string.
     *
     * @param extension The string with which the files have to end with to match.
     * @param directory the directory in which to search for files.
     * @return A list of files.
     */
    private static List<File> getFiles(String extension, File directory) throws IOException {
        ArrayList<File> files = new ArrayList<>();
        File[] filesArray = directory.listFiles();
        if (filesArray != null) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    files.addAll(getFiles(extension, file));
                } else if (file.getName()
                    .toLowerCase()
                    .endsWith(extension)) {
                        files.add(file);
                    }
            }
        }
        return files;
    }

}
