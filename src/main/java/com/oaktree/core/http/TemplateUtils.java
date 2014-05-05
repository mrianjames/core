package com.oaktree.core.http;

import com.oaktree.core.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 26/08/12
 * Time: 08:14
 * To change this template use File | Settings | File Templates.
 */
public class TemplateUtils {

    private final static Logger logger = LoggerFactory.getLogger(TemplateUtils.class);

    public static class JavaTemplate {
        public static final String APPLICATION = "\\$\\{application\\}";
        public static final String JAVA_EXE = "\\$\\{java.exe\\}";
        public static final String JAVA_VM_ARGS = "\\$\\{java.vm.args\\}";
        public static final String CLASSPATH = "\\$\\{classpath\\}";
        public static final String APP_HOME = "\\$\\{application.home\\}";
        public static final String ARGS = "\\$\\{application.args\\}";
        public static final String APP_MAIN = "\\$\\{java.main\\}";
        public static final String DATE_TIME = "\\$\\{date.time\\}";
    }

    public static class MavenTemplate {
        public static final String APPLICATION = "\\$\\{application\\}";
        public static final String GROUP = "\\$\\{group\\}";
        public static final String REPO = "\\$\\{repo\\}";
        public static final String ARTIFACT = "\\$\\{artifact\\}";
        public static final String VERSION = "\\$\\{version\\}";
    }

    public static class NativeTemplate {
        public static final String DATE_TIME = "\\$\\{date.time\\}";
        public static final String APPLICATION = "\\$\\{application\\}";
        public static final String APP_EXE = "\\$\\{application.exe\\}";
        public static final String APP_HOME = "\\$\\{application.home\\}";
        public static final String ARGS = "\\$\\{application.args\\}";
    }

    /**
     * Reads a file, replaces any text found with the substitutions and then
     * writes the file to target location.
     * @param template
     * @param target
     * @param replacements
     */
    public static void readReplaceAndWrite(String template,  String target,Map<String,String> replacements) {
        String templateText = readTemplateText(template);
        templateText = replaceText(templateText,replacements);
        writeText(target,templateText);
    }

    private static String replaceText(String templateText, Map<String, String> replacements) {
        for (Map.Entry<String,String> entry:replacements.entrySet()) {
            String value = entry.getValue();
            if (value==null) {
                logger.warn("null value: " + entry.getKey());
            } else {
                templateText = templateText.replaceAll(entry.getKey(), escape(value));
            }
        }
        return templateText;
    }

    public static String escape(String str) {
        return str.replaceAll("\\\\", "\\\\\\\\");
    }

    public static void writeText(String target, String templateText) {
        try {
            File f = new File(target);
            f.setExecutable(true);
            //try
            try {
                Runtime.getRuntime().exec("chmod 700 "+target);
            } catch (Exception e) {
                logger.warn("Failure to set permissions: " + e.getMessage());
            }
            FileWriter w = new FileWriter(f);
            w.write(templateText);
            w.close();
            chmod(target,0777);
        }catch (Exception e) {
            Log.exception(logger,e);
        }
    }

    private static int chmod(String filename, int mode) {
        try {
            Class<?> fspClass = Class.forName("java.util.prefs.FileSystemPreferences");
            Method chmodMethod = fspClass.getDeclaredMethod("chmod", String.class, Integer.TYPE);
            chmodMethod.setAccessible(true);
            int result = (Integer)chmodMethod.invoke(null, filename, mode);
            logger.info("Permissions set to " + mode + " on " + filename + ". Result: " + result);
            return result;
        } catch (Throwable ex) {
            logger.error(ex.getMessage());
            return -1;
        }
    }

    public static String readTemplateText(String templateFile) {
        //first off we need to make a fake pom for the app - this can then be used by us to run
        //maven commands.
        File template = new File(templateFile);
        try {
            FileReader reader = new FileReader(template);
            BufferedReader breader = new BufferedReader(reader);
            StringBuilder b = new StringBuilder(2056);
            String line = breader.readLine();
            while (line != null) {
                b.append(line);
                line = breader.readLine();
            }
            return b.toString();
        } catch (Exception e) {
            Log.exception(logger, e);
        }
        return null;
    }


}
