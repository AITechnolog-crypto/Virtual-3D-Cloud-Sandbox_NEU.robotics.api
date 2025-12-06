package com.june.hidden;

import javax.tools.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * In‑Memory Java Source Compiler util.
 */
public final class SourceCompiler {
    private SourceCompiler() {}

    public static Map<String, byte[]> compileInMemory(String className, String source) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Kein InMemory Compiler verfuegbar JDK erforderlich");
        StandardJavaFileManager std = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        InMemoryFileManager mem = new InMemoryFileManager(std);
        List<JavaFileObject> units = new ArrayList<>();
        units.add(new StringJavaFileObject(className, source));
        List<String> opts = Arrays.asList("-encoding","UTF-8","-Xlint:deprecation","-Xlint:unchecked");
        JavaCompiler.CompilationTask task = compiler.getTask(null, mem, null, opts, null, units);
        boolean ok = task.call();
        if (!ok) throw new IllegalStateException("Kompilierung fehlgeschlagen");
        return mem.getClassBytes();
    }
}

/** String‑backed JavaFileObject */
class StringJavaFileObject extends SimpleJavaFileObject {
    private final String code;
    StringJavaFileObject(String className, String code) {
        super(uriForClass(className), Kind.SOURCE);
        this.code = code;
    }
    @Override public CharSequence getCharContent(boolean ignoreEncodingErrors){ return code; }
    private static URI uriForClass(String className) {
        return URI.create("string:///" + className.replace('.','/') + Kind.SOURCE.extension);
    }
}

/** Captures compiled class bytes */
class ByteArrayJavaFileObject extends SimpleJavaFileObject {
    private final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    ByteArrayJavaFileObject(String name, Kind kind) {
        super(URI.create("mem:///" + name.replace('.','/') + kind.extension), kind);
    }
    @Override public java.io.OutputStream openOutputStream() { return bos; }
    byte[] toByteArray(){ return bos.toByteArray(); }
}

/** FileManager storing compiled classes in memory */
class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, ByteArrayJavaFileObject> compiled = new HashMap<>();
    InMemoryFileManager(JavaFileManager fileManager) { super(fileManager); }
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling){
        ByteArrayJavaFileObject out = new ByteArrayJavaFileObject(className, kind);
        compiled.put(className, out);
        return out;
    }
    Map<String, byte[]> getClassBytes(){
        Map<String, byte[]> m = new HashMap<>();
        for (Map.Entry<String, ByteArrayJavaFileObject> e : compiled.entrySet()) {
            m.put(e.getKey(), e.getValue().toByteArray());
        }
        return m;
    }
}
