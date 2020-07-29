package org.eclipse.openj9.jmin.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarFileUtils {
    private static JarInputStream getInnerMostJarInputStream(String jarFile) {
        JarInputStream is = null;
        String[] parts = jarFile.split("!/");
        try {
            is = new JarInputStream(new FileInputStream(parts[0]));
            for (int i = 1; i < parts.length; i++) {
                String entryName = parts[i];
                JarEntry entry = is.getNextJarEntry();
                while (entry != null && !entry.getName().equals(entryName)) {
                    entry = is.getNextJarEntry();
                }
                assert entry != null : "Failed to find jar entry " + entryName + "in jar file " + jarFile;
                is = new JarInputStream(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    public static InputStream getJarEntryInputStream(String jarFile, String jarEntry) {
        JarInputStream jin = getInnerMostJarInputStream(jarFile);
        try {
            JarEntry entry = jin.getNextJarEntry();
            while (entry != null && !entry.getName().equals(jarEntry)) {
                entry = jin.getNextJarEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jin;
    }
}
