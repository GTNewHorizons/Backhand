package net.tclproject.mysteriumlib.asm.core;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassWriter;

/** General purpose class with utilities. */
public class MiscUtils {

    /**
     * ClassWriter with a custom implementation of getCommonSuperClass. When it's used, no class loading occurs.
     * But, the loading of classes itself is rarely the problem, because the init of the class (the calling of static
     * blocks) doesn't happen when class loading occurs. The problems occur, when fixes are inserted into
     * classes dependent on each other, then the standard implementation fails with ClassCircularityError.
     */
    public class SafeCommonSuperClassWriter extends ClassWriter {

        /** An instance of MetaReader. */
        private final MetaReader metaReader;

        public SafeCommonSuperClassWriter(MetaReader metaReader, int flags) {
            super(flags);
            this.metaReader = metaReader;
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            ArrayList<String> superClasses1 = metaReader.getSuperClasses(type1); // super classes of type1
            ArrayList<String> superClasses2 = metaReader.getSuperClasses(type2); // super classes of type2
            int size = Math.min(superClasses1.size(), superClasses2.size()); // the maximum time both can be super'd
                                                                             // until one reaches one of classes passed
                                                                             // in
            int i;

            for (i = 0; i < size && superClasses1.get(i)
                .equals(superClasses2.get(i)); i++); // For every super class, 1 is added.

            if (i == 0) {
                return "java/lang/Object"; // If there are no common classes, Object is automatically returned
            } else {
                return superClasses1.get(i - 1); // e.g: you have 1 common class so "i" is going to be 1. But list
                                                 // indices start with 0, that's why it takes away 1.
            } // The common class returned is the most specific one, e.g. if we have 'class a extends b extends c', b is
              // returned
        }
    }

    public interface LogHelper {

        /** Used for debugging information. */
        void debug(String message);

        /**
         * Used for general information. Does not appear in the game console, in order to see it, one must open up
         * fml-server/client-latest.log.
         */
        void info(String message);

        /** Used for not-so-important warnings. */
        void warning(String message);

        /** Used for severe warnings. */
        void severe(String message);

        /** Used for severe warnings. Prints the stack trace. */
        void severe(String message, Throwable cause);

        /** Used for fatal warnings. */
        void fatal(String message);

        /** Used for fatal warnings. Prints the stack trace. */
        void fatal(String message, Throwable cause);
    }

    public class SystemLogHelper implements LogHelper {

        @Override
        public void debug(String message) {
            System.out.println("[DEBUG] " + message);
        }

        @Override
        public void warning(String message) {
            System.out.println("[WARNING] " + message);
        }

        @Override
        public void severe(String message) {
            System.out.println("[SEVERE] " + message);
        }

        @Override
        public void severe(String message, Throwable cause) {
            severe(message);
            cause.printStackTrace();
        }

        @Override
        public void info(String message) {
            System.out.println("[INFORMATION] " + message);
        }

        @Override
        public void fatal(String message) {
            System.out.println("[---------------[!!!FATAL!!!]---------------]");
            System.out.println(message);
            System.out.println("[---------------[!!!FATAL!!!]---------------]");

        }

        @Override
        public void fatal(String message, Throwable cause) {
            fatal(message);
            cause.printStackTrace();

        }
    }

    public class MinecraftLogHelper implements LogHelper {

        private Logger logger;

        public MinecraftLogHelper(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void debug(String message) {
            logger.log(Level.FINE, message);
        }

        /** Used for detailed log messages. */
        public void detailed(String message) {
            logger.log(Level.FINEST, message);
        }

        /** Used for log messages related to how the user configured something. */
        public void configInfo(String message) {
            logger.log(Level.CONFIG, message);
        }

        @Override
        public void warning(String message) {
            logger.log(Level.WARNING, message);
        }

        @Override
        public void severe(String message) {
            logger.log(Level.SEVERE, message);
        }

        @Override
        public void severe(String message, Throwable cause) {
            logger.log(Level.SEVERE, message, cause);
        }

        @Override
        public void info(String message) {
            logger.log(Level.INFO, message);
        }

        @Override
        public void fatal(String message) {
            logger.log(Level.SEVERE, "[---------------[!!!FATAL!!!]---------------]");
            logger.log(Level.SEVERE, message);
            logger.log(Level.SEVERE, "[---------------[!!!FATAL!!!]---------------]");

        }

        @Override
        public void fatal(String message, Throwable cause) {
            logger.log(Level.SEVERE, "[---------------[!!!FATAL!!!]---------------]");
            logger.log(Level.SEVERE, message, cause);
            logger.log(Level.SEVERE, "[---------------[!!!FATAL!!!]---------------]");
        }
    }

    /**
     * Generates a methods.bin file for the use in MysteriumASMLib out of a methods.csv file.
     * methods.csv is located inside mcp/conf/. (mcp is the folder you unzipped mcp into).
     * If you didn't know, forge uses mcp to provide you with readable method names.
     * Inside this folder, there are ready-to-go methods.bin files for 1.6.4, 1.7.10, and 1.8.
     * <p/>
     * It is still highly recommended to generate methods.bin for your own version of mcp,
     * otherwise errors like "can't find target method of fix" will most likely occur at some point.
     *
     * @throws Exception if something went wrong, e.g. the program doesn't have the permissions to access the file or
     *                   folder it's in.
     */
    public static void generateMethodsDictionary() throws Exception {
        List<String> lines = FileUtils.readLines(new File("methods.csv"));
        lines.remove(0);
        HashMap<Integer, String> map = new HashMap<>();
        for (String str : lines) {
            String[] splitted = str.split(",");
            int first = splitted[0].indexOf('_');
            int second = splitted[0].indexOf('_', first + 1);
            int id = Integer.valueOf(splitted[0].substring(first + 1, second));
            map.put(id, splitted[1]);
        }

        DataOutputStream out = new DataOutputStream(new FileOutputStream("methods.bin"));
        out.writeInt(map.size());

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            out.writeInt(entry.getKey());
            out.writeUTF(entry.getValue());
        }

        out.close();

    }

}
