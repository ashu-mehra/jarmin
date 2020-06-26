package org.eclipse.openj9.jmin.util;

import org.eclipse.openj9.jmin.info.ClassSource;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple Map wrapper used to track the superclass chain of a given class.
 * 
 * The {@code ArrayList<String>} is ordered from immediate parent to most distant.
 * 
 * @version 1.0
 * @since 1.0
 * @see java.util.HashMap
 */
public class JarMap extends HashMap<String, ClassSource> {

    private static final long serialVersionUID = -3099743932266613366L;

    public JarMap() {
        super(); 
    }

    public JarMap(int initialCapacity) { 
        super(initialCapacity); 
    }

    public JarMap(int initialCapacity, float loadFactor) { 
        super(initialCapacity, loadFactor); 
    }

    public JarMap(Map<String, ClassSource> other) {
        super(other); 
    }

    public JarMap(JarMap other) { 
        super(other); 
    }
}