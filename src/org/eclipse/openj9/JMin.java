package org.eclipse.openj9;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.jar.*;
import java.util.zip.*;

import org.eclipse.openj9.jmin.WorkItem;
import org.eclipse.openj9.jmin.analysis.ReferenceAnalyzer;
import org.eclipse.openj9.jmin.info.CallSite;
import org.eclipse.openj9.jmin.info.ClassInfo;
import org.eclipse.openj9.jmin.info.FieldSite;
import org.eclipse.openj9.jmin.info.MethodInfo;
import org.eclipse.openj9.jmin.info.ReferenceInfo;
import org.eclipse.openj9.jmin.util.HierarchyContext;
import org.eclipse.openj9.jmin.util.WorkList;
import org.eclipse.openj9.jmin.writer.FilteringClassWriterAdapter;
import org.eclipse.openj9.jmin.writer.NonLoadingClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class JMin {
    public static final String REDUCTION_MODE_PROPERTY_NAME = "org.eclipse.openj9.jmin.reduction_mode";
    public static final String INCLUSION_MODE_PROPERTY_NAME = "org.eclipse.openj9.jmin.inclusion_mode";
    public static final String TRACE_PROPERTY_NAME = "org.eclipse.openj9.jmin.trace";
    public static final String ALL_SVC_IMPLEMENTAIONS = "@LL_SVC";
    private String reductionMode;
    private boolean trace;
    private String[] jars;
    private HierarchyContext context;
    private WorkList worklist;
    private ReferenceInfo info;
    private static String[] jdkWhitelist = new String[] {
        "java/util/jar", 
        "java/util/zip", 
        "com/ibm/oti",
        "java/lang/invoke", 
        "java/lang/J9VMInternals", 
        "sun/misc/JavaUtilZipFileAccess", 
        "java/util/Collections",
        "java/util/IdentityHashMap", 
        "java/util/Properties", 
        "openj9/internal", 
        "java/io/ByteArrayInputStream",
        "java/io/ByteArrayOutputStream", 
        "java/io/File", 
        "java/io/IOException", 
        "java/io/InputStream",
        "java/io/OutputStream", 
        "java/io/PrintStream", 
        "java/nio/charset/StandardCharsets", 
        "java/nio/file/",
        "sun/nio/fs/", 
        "java/security/SecureRandom", 
        "java/util/EnumSet", 
        "java/util/Objects",
        "java/util/Properties", 
        "java/util/Random", 
        "java/util/Set", 
        "java/util/HashMap", 
        "java/net/URI",
        "java/lang/reflect/Constructor"
    };
    private static Class<?>[] preprocessorClasses = new Class[] {
        org.eclipse.openj9.jmin.plugins.QuarkusPreProcessor.class,
        org.eclipse.openj9.jmin.plugins.CaffeinePreProcessor.class,
        org.eclipse.openj9.jmin.plugins.HibernatePreProcessor.class,
        org.eclipse.openj9.jmin.plugins.ArjunaPreProcessor.class
    };

    private static Class<?>[] processorClasses = new Class<?>[] {
        org.eclipse.openj9.jmin.plugins.ArjunaAnnotationProcessor.class,
        org.eclipse.openj9.jmin.plugins.BeanAnnotationProcessor.class,
        org.eclipse.openj9.jmin.plugins.HibernateProcessor.class,
        org.eclipse.openj9.jmin.plugins.JBossLoggingAnnotationProcessor.class,
        org.eclipse.openj9.jmin.plugins.MBeanProcessor.class,
        org.eclipse.openj9.jmin.plugins.PersistenceAnnotationProcessor.class,
        org.eclipse.openj9.jmin.plugins.QuarkusAnnotationProcessor.class,
        org.eclipse.openj9.jmin.plugins.RuntimeAnnotationProcessor.class
    };
    private ClassVisitor processors;
    
    public JMin(String[] jars, String clazz, String method, String signature) throws IOException {
        this.reductionMode = System.getProperty(REDUCTION_MODE_PROPERTY_NAME);
        if (this.reductionMode == null) {
            this.reductionMode = "class";
        }
        String traceMode = System.getProperty(TRACE_PROPERTY_NAME);
        trace = traceMode != null && traceMode.equalsIgnoreCase("true");
        this.jars = jars.clone();
        context = new HierarchyContext();
        info = new ReferenceInfo();
        worklist = new WorkList(this.reductionMode, info, context);
        
        processors = null;
        for (Class<?> proc : processorClasses) {
            try {
                processors = (ClassVisitor) proc
                        .getConstructor(WorkList.class, HierarchyContext.class, ReferenceInfo.class, ClassVisitor.class)
                        .newInstance(worklist, context, info, processors);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        
        for (String jar : jars) {
            JarInputStream jin = new JarInputStream(new FileInputStream(jar));
            ZipEntry ze = jin.getNextEntry();

            while (ze != null) {
                String entryName = ze.getName();
                if (entryName.endsWith(".class") && !entryName.endsWith("module-info.class")) {
                    ClassReader cr = new ClassReader(jin);
                    cr.accept(ReferenceAnalyzer.getReferenceInfoProcessor(jar, info, context), ClassReader.SKIP_DEBUG);
                    entryName = entryName.substring(0, entryName.length() - 6);
                } else if (!ze.isDirectory() && entryName.startsWith("META-INF/services/") && !entryName.endsWith(".class")) {
                    System.out.println("Found service entry " + entryName);
                    String serviceName = entryName.substring("META-INF/services".length());
                    serviceName = serviceName.replace('.', '/');
                    byte[] bytes = readEntry(jin);
                    if (bytes != null) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(new ByteArrayInputStream(bytes)));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            line = line.replace('.', '/');
                            if (line.length() > 0 && line.charAt(0) != '#') {
                                context.addServiceMap(serviceName, line);
                            }
                        }
                        reader.close();
                    }
                }
                ze = jin.getNextEntry();
            }
            jin.close();
        }

        context.computeClosure();

        worklist.processMethod(clazz, method, signature);
    }

    private byte[] readEntry(InputStream is) {
        try {
            byte[] b = new byte[is.available()];
            int len = 0;
            while (true) {
                int n = is.read(b, len, b.length - len);
                if (n == -1) {
                    if (len < b.length) {
                        byte[] c = new byte[len];
                        System.arraycopy(b, 0, c, 0, len);
                        b = c;
                    }
                    return b;
                }
                len += n;
                if (len == b.length) {
                    int last = is.read();
                    if (last < 0) {
                        return b;
                    }
                    byte[] c = new byte[b.length + 1000];
                    System.arraycopy(b, 0, c, 0, len);
                    c[len++] = (byte) last;
                    b = c;
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void copyFile(InputStream jin, JarOutputStream jout, byte[] buffer, ZipEntry entry) throws IOException {
        ZipEntry newEntry = new ZipEntry(entry.getName());
        newEntry.setMethod(entry.getMethod());
        long size = entry.getSize();
        if (size > -1) {
            newEntry.setSize(size);
        }
        long csize = entry.getCompressedSize();
        if (csize > -1) {
            newEntry.setCompressedSize(csize);
        }
        long crc = entry.getCrc();
        if (crc != -1) {
            newEntry.setCrc(crc);
        }
        jout.putNextEntry(newEntry);
        while (jin.available() > 0) {
            int read = jin.read(buffer);
            if (read > 0) {
                jout.write(buffer, 0, read);
            }
        }
        jout.closeEntry();
    }

    public void minimizeJars() throws IOException {
        int classCount = 0;
        HashSet<String> jarsToProcess = new HashSet<String>();
        for (String clazz : context.seenClasses()) {
            if (info.isClassReferenced(clazz)) {
                jarsToProcess.add(context.getJarForClass(clazz));
            }
        }
        
        FileSystem fs = FileSystems.getDefault();
        File targetDir = new File("." + File.separator + "minimized_jars");
        targetDir.mkdirs();
        for (String jar : jarsToProcess) {
            Path path = fs.getPath(jar);
            String jarname = path.getFileName().toString();
            JarInputStream jin = new JarInputStream(new FileInputStream(jar));
            String dest = "." + File.separator + "minimized_jars" + File.separator + jarname;
            System.out.println("Create minimized jar " + dest);
            Manifest man = jin.getManifest();
            JarOutputStream jout = man != null ? new JarOutputStream(new FileOutputStream(dest), man) : new JarOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[512];
            ZipEntry entry = jin.getNextEntry();
            while (entry != null) {
                boolean isWhitelisted = false;
                for (String pattern : jdkWhitelist) {
                    if (entry.getName().startsWith(pattern)) {
                        isWhitelisted = true;
                        break;
                    }
                }
                if (isWhitelisted
                    || !entry.getName().endsWith(".class")
                    || entry.getName().equals("class-info.class")
                    || (reductionMode.equals("class") && info.isClassReferenced(entry.getName().substring(0, entry.getName().length() - 6)))) {
                    if (entry.getName().endsWith(".class") && !entry.getName().equals("class-info.class") && (reductionMode.equals("class") && info.isClassReferenced(entry.getName().substring(0, entry.getName().length() - 6)))) {
                        classCount++;
                    }
                    copyFile(jin, jout, buffer, entry);
                } else if (entry.getName().endsWith(".class") 
                           && (reductionMode.equals("method") || reductionMode.equals("field") || reductionMode.equals("class")) 
                           && info.isClassReferenced(entry.getName().substring(0, entry.getName().length() - 6))) {
                    classCount++;
                    BufferedInputStream bis = new BufferedInputStream(jin);
                    if (entry.getSize() * 2 < Integer.MAX_VALUE - 1) {
                        try {
                            bis.mark((int)(entry.getSize() * 2) + 1);
                            ClassReader cr = new ClassReader(bis);
                            ClassWriter cw = new NonLoadingClassWriter(context, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                            cr.accept(new FilteringClassWriterAdapter(reductionMode, info, cw), 0);
                            ZipEntry newEntry = new ZipEntry(entry.getName());
                            jout.putNextEntry(newEntry);
                            jout.write(cw.toByteArray());
                            jout.closeEntry();
                        } catch (Exception e) {
                            bis.reset();
                            System.out.println("Unable to minimize " + entry.getName() + " - copying whole");
                            System.out.println("Error " + e.getMessage());
                            copyFile(bis, jout, buffer, entry);
                        }
                    } else {
                        copyFile(jin, jout, buffer, entry);
                    }
                }
                entry = jin.getNextEntry();
            }
            jin.close();
            jout.close();
        }
        System.out.println("Total classes copied: " + classCount);
    }

    private void populateWorklist() throws IOException {
        worklist.instantiateClass("java/lang/J9VMInternals");
        worklist.instantiateClass("java/lang/J9VMInternals$ClassInitializationLock");
        worklist.processMethod("java/lang/J9VMInternals", "completeInitialization", "()V");
        worklist.processMethod("java/lang/J9VMInternals", "initialize", "(Ljava/lang/Class;)V");
        worklist.processMethod("java/lang/J9VMInternals$ClassInitializationLock", "<init>", "()V");

        for (Class<?> preproc : preprocessorClasses) {
            try {
                preproc.getMethod("process").invoke(
                        preproc.getConstructor(WorkList.class, HierarchyContext.class, ReferenceInfo.class).newInstance(worklist, context, info));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }

        for (String jar : jars) {
            JarInputStream jin = new JarInputStream(new FileInputStream(jar));
            ZipEntry ze = jin.getNextEntry();

            while (ze != null) {
                String entryName = ze.getName();
                if (entryName.endsWith(".class") && !entryName.endsWith("module-info.class")) {
                    entryName = entryName.substring(0, entryName.length() - 6);
                    ClassReader cr = new ClassReader(jin);
                    cr.accept(processors, ClassReader.SKIP_DEBUG);
                }
                ze = jin.getNextEntry();
            }
        }
    }

    protected void processMethod(MethodInfo minfo) {
        if (minfo.processed()) {
            return;
        }
        minfo.setProcessed();

        for (FieldSite field : minfo.getReferencedFields()) {
            if (field.desc.charAt(0) == 'L') {
                worklist.processClass(field.desc.substring(1, field.desc.length() - 1));
            }
            worklist.processField(field.clazz, field.name, field.desc);
        }

        for (String init : minfo.getInstantiatedClasses()) {
            worklist.instantiateClass(init);
        }

        for (String clazz : minfo.getReferencedClasses()) {
            worklist.processClass(clazz);
        }

        for (String annot : minfo.getAnnotations()) {
            if (context.hasRuntimeAnnotation(annot)) {
                worklist.instantiateClass(annot);
            }
        }

        for (CallSite call : minfo.getCallSites()) {
            HashSet<String> descs = new HashSet<String>();
            if (call.desc.equals("*")) {
                ClassInfo ci = info.getClassInfo(call.clazz);
                if (ci != null) {
                    for (MethodInfo mi : ci.getMethodsByNameOnly(call.name)) {
                        descs.add(mi.desc());
                    }
                }
            } else {
                descs.add(call.desc);
            }
            for (String desc : descs) {
                switch (call.kind) {
                    case VIRTUAL:
                        worklist.processVirtualMethod(call.clazz, call.name, desc);
                        break;
                    case INTERFACE:
                        worklist.processInterfaceMethod(call.clazz, call.name, desc);
                        break;
                    case SPECIAL:
                    case STATIC:
                    case DYNAMIC:
                        worklist.processMethod(call.clazz, call.name, desc);
                        break;
                    default:
                        throw new RuntimeException("Unknown call kind!");
                }
            }
        }
    }

    protected void processEntry() {
        WorkItem entry = worklist.next();
        ClassInfo cinfo = info.getClassInfo(entry.clazz);
        if (trace) {
            System.out.print("Process ");
            System.out.print(entry.toString());
        }

        MethodInfo minfo = cinfo.getMethod(entry.name, entry.desc);
        processMethod(minfo);

        if (trace) {
            System.out.println();
        }
    }

    public void processEntries() {
        while (worklist.hasNext()) {
            processEntry();
        }
    }

    public static void main(String[] args) throws IOException {
        boolean argsValid = args.length == 4;
        String reductionMode = System.getProperty(REDUCTION_MODE_PROPERTY_NAME);
        argsValid &= reductionMode == null || reductionMode.equals("class") || reductionMode.equals("method") || reductionMode.equals("field");
        String inclusionMode = System.getProperty(INCLUSION_MODE_PROPERTY_NAME);
        argsValid &= inclusionMode == null || inclusionMode.equals("reference") || inclusionMode.equals("instantiation");
        String traceMode = System.getProperty(TRACE_PROPERTY_NAME);
        argsValid &= traceMode == null || traceMode.equalsIgnoreCase("true") || traceMode.equalsIgnoreCase("false");
        if (!argsValid) {
            System.out.println("Usage: <classpath> <class> <method_name> <method_signature>");
            System.out.println("Set " + REDUCTION_MODE_PROPERTY_NAME + " to control how minimization is performed: ");
            System.out.println("  = class (remove only unused classes) [default]");
            System.out.println("  = method (remove unused classes and methods)");
            System.out.println("  = field (remove unused classes, methods and fields)");
            System.out.println("Set " + INCLUSION_MODE_PROPERTY_NAME + " to control how classes are included in the analysis: ");
            System.out.println("  = reference (overriden / implemented methods are scanned when the class is first referenced)");
            System.out.println("  = instantiation (overridden / implemented methods are scanned when the class is first instantiated) [default]");
            System.out.println("Set " + TRACE_PROPERTY_NAME + " to control output verbosity: ");
            System.out.println("  = true (verbose output will be printed to stdout)");
            System.out.println("  = false (only impotant diagnostic output will be printed to stdout) [default]");
            return;
        }
        FileSystem fs = FileSystems.getDefault();
        String[] paths = args[0].split(File.pathSeparator);
        boolean error = false;
        HashSet<String> jarFiles = new LinkedHashSet<String>();
        for (String pathStr : paths) {
            Path path = fs.getPath(pathStr);
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(entry -> {
                   if (!Files.isDirectory(entry) && entry.toString().endsWith(".jar")) {
                       jarFiles.add(entry.toString());
                   }
                });
            } else {
                if (!Files.exists(path)) {
                    error = true;
                    System.out.println("Cannot read jar " + pathStr);
                }
                jarFiles.add(pathStr);
            }
        }
        if (error) {
            return;
        }
        paths = jarFiles.toArray(new String[jarFiles.size()]);
        System.out.println("Jars to search:");
        for (String path : jarFiles) {
            System.out.println("  " + path);
        }
        
        JMin jmin = new JMin(paths, args[1], args[2], args[3]);
        jmin.populateWorklist();
        System.out.print("Processing worklist...");
        jmin.processEntries();
        //jmin.dumpInfo();
        System.out.println("done");
        jmin.minimizeJars();
    }
}