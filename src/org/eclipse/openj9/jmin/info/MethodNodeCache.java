package org.eclipse.openj9.jmin.info;

import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.JarFileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM8;

public class MethodNodeCache {
    private Map<String, MethodNode> methodNameToNodeMap;
    private HierarchyContext context;

    public MethodNodeCache(HierarchyContext context) {
        this.context = context;
        methodNameToNodeMap = new HashMap<String, MethodNode>();
    }

    public void cacheMethodNodes(String clazz, ClassSource classSource) {
        try (InputStream is = JarFileUtils.getJarEntryInputStream(classSource.getJarFile(), classSource.getJarFileEntry())) {
            ClassReader cr = new ClassReader(is);
            cr.accept(new MethodNodeLocator(clazz, classSource), ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createKey(String jarFile, String jarFileEntry,String clazz, String name, String desc) {
        return jarFile+jarFileEntry+clazz+name+desc;
    }

    public MethodNode getMethodNode(MethodInfo minfo, ClassSource classSource) {
        String key = createKey(classSource.getJarFile(), classSource.getJarFileEntry(), minfo.clazz(), minfo.name(), minfo.desc());
        if (!methodNameToNodeMap.containsKey(key)) {
            cacheMethodNodes(minfo.clazz(), classSource);
        }
        return methodNameToNodeMap.get(key);
    }

    private class MethodNodeLocator extends ClassNode {
        private String clazz;
        private ClassSource classSource;

        MethodNodeLocator(String clazz, ClassSource classSource) {
            super(ASM8);
            this.clazz = clazz;
            this.classSource = classSource;
        }
        public void visitEnd() {
            for (MethodNode mn : methods) {
                methodNameToNodeMap.put(createKey(classSource.getJarFile(), classSource.getJarFileEntry(), clazz, mn.name, mn.desc), mn);
            }
            super.visitEnd();
        }
    }
}
