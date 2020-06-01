package org.eclipse.openj9.jmin.util;

import java.util.ArrayList;
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
public class SuperMap extends HashMap<String, ArrayList<String>> {

    private static final long serialVersionUID = -3099743932266613366L;

    public SuperMap() {
        super(); 
    }

    public SuperMap(int initialCapacity) { 
        super(initialCapacity); 
    }

    public SuperMap(int initialCapacity, float loadFactor) { 
        super(initialCapacity, loadFactor); 
    }

    public SuperMap(Map<String, ArrayList<String>> other) { 
        super(other); 
    }

    public SuperMap(SuperMap other) { 
        super(other); 
    }
}