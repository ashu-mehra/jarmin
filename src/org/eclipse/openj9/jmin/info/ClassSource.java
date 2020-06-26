package org.eclipse.openj9.jmin.info;

public class ClassSource {
    private String jar;
    private String entry;

    public ClassSource(String jar, String entry) {
        this.jar = jar;
        this.entry = entry;
    }

    public String getJarFile() { return jar; }
    public String getJarFileEntry() { return entry; }
}