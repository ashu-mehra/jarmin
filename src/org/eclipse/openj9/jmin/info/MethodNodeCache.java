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

    public void cacheMethodNodes(String clazz) {
        ClassSource source = context.getSourceForClass(clazz);
        if (source != null) {
            try (InputStream is = JarFileUtils.getJarEntryInputStream(source.getJarFile(), source.getJarFileEntry())) {
                ClassReader cr = new ClassReader(is);
                cr.accept(new MethodNodeLocator(clazz), ClassReader.SKIP_DEBUG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createKey(String clazz, String name, String desc) {
        return clazz+name+desc;
    }

    public MethodNode getMethodNode(MethodInfo minfo) {
        String key = createKey(minfo.clazz(), minfo.name(), minfo.desc());
        if (!methodNameToNodeMap.containsKey(key)) {
            cacheMethodNodes(minfo.clazz());
        }
        return methodNameToNodeMap.get(key);
    }

    private class MethodNodeLocator extends ClassNode {
        private String clazz;

        MethodNodeLocator(String clazz) {
            super(ASM8);
            this.clazz = clazz;
        }
        public void visitEnd() {
            for (MethodNode mn : methods) {
                methodNameToNodeMap.put(createKey(clazz, mn.name, mn.desc), mn);
            }
            super.visitEnd();
        }
    }
}
