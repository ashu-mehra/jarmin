package org.eclipse.openj9.jmin.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A simple Map wrapper used to track the sub-classes of a given class.
 * 
 * The sub-classes are not ordered in any way
 * 
 * @version 1.0
 * @since 1.0
 * @see java.util.HashMap
 */
public class SubMap extends HashMap<String, HashSet<String>> {

    private static final long serialVersionUID = -4065054826018918784L;

    public SubMap() {
        super();
    }

    public SubMap(int initialCapacity) {
        super(initialCapacity);
    }

    public SubMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public SubMap(Map<String, HashSet<String>> other) {
        super(other);
    }

    public SubMap(SubMap other) {
        super(other);
    }
}