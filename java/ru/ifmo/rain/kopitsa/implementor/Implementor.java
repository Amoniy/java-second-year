package ru.ifmo.rain.kopitsa.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class Implementor implements JarImpler {

    /**
     * Escapes given string.
     *
     * @param stringToEscape String to escape.
     * @return {@link java.lang.String} escaped string.
     */
    private String escape(String stringToEscape) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < stringToEscape.length(); index++) {
            char currentChar = stringToEscape.charAt(index);
            if (currentChar <= 127) {
                builder.append(currentChar);
            } else {
                builder.append(String.format("\\u%04x", (int) currentChar));
            }
        }
        return builder.toString();
    }

    /**
     * Adds given method to .java file.
     *
     * @param method Method to add.
     * @param writer Given writer.
     * @throws IOException If <tt>writer</tt> throws it.
     */
    private void addMethod(Method method, BufferedWriter writer) throws IOException {
        StringBuilder annotationBuilder = new StringBuilder();
        Arrays.stream(method.getAnnotations()).forEach(annotation -> annotationBuilder.append(annotation).append("\n"));
        writer.write(escape(annotationBuilder.toString()));

        writer.write(escape("public "));

        String returnType = method.getReturnType().getCanonicalName();
        writer.write(escape(String.format("%s %s", returnType, method.getName())));

        String parameters = String.join(",", Arrays.stream(method.getParameters()).map(parameter ->
                format("%s %s", parameter.getType().getCanonicalName(), parameter.getName())).collect(toList()));
        writer.write(escape(format("(%s) ", parameters)));

        Class[] exceptions = method.getExceptionTypes();
        if (exceptions.length > 0) {
            String exceptionsNames = String.join(",", Arrays.stream(exceptions)
                    .map(Class::getCanonicalName).collect(toList()));
            writer.write(escape(String.format("throws %s", exceptionsNames)));
        }

        writer.write(escape("{\n"));

        String defaultReturn;
        if (method.getReturnType().toString().equals("boolean")) {
            defaultReturn = " false";
        } else if (method.getReturnType().toString().equals("void")) {
            defaultReturn = "";
        } else if (method.getReturnType().isPrimitive()) {
            defaultReturn = " 0";
        } else {
            defaultReturn = " null";
        }
        writer.write(escape(format("return%s;\n}\n", defaultReturn)));
    }

    /**
     * Writes given class into jar.
     *
     * @param token Class to write into jar.
     * @param root  Root folder of class.
     */
    private void assembleJar(Class<?> token, Path root) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream target =
                     new JarOutputStream(Files.newOutputStream(root.resolve(token.getName() + ".jar")), manifest)) {
            JarEntry entry = new JarEntry(format("%s/%sImpl.class",
                    token.getPackage().getName().replace('.', '/'), token.getSimpleName()));
            target.putNextEntry(entry);

            writeClass(new File(Paths.get(format("%s/%s/%sImpl.class", root.toString(),
                    token.getPackage().getName().replace('.', '/'),
                    token.getSimpleName())).toString()), target);
            target.close();
        } catch (IOException e) {
            System.out.println("Проблема с записью в .jar");
        }
    }

    /**
     * Writes given class into jar.
     *
     * @param source Class to write into jar.
     * @param target Jar output stream.
     * @throws IOException If <tt>target</tt> throws it.
     */
    private void writeClass(File source, JarOutputStream target) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source))) {
            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } catch (IOException e) {
            System.out.println("Couldn't write class");
            throw e;
        }
    }

    /**
     * Implements given interface with default return values. And assembles it into jar.
     *
     * @param token   Interface to implement.
     * @param jarFile Full expected name of jar file.
     * @throws ImplerException If <tt>token</tt> or <tt>jarFile.getParent()</tt> or
     *                         <tt>jarFile.getParent().getPackage()</tt> are null.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        implement(token, jarFile.getParent());
        ToolProvider.getSystemJavaCompiler().run(null, null, null,
                jarFile.getParent().resolve(token.getCanonicalName().replace(".", "/") + "Impl.java")
                        .toAbsolutePath().toString(), "-encoding", "CP1251");
        assembleJar(token, jarFile.getParent());
    }

    /**
     * Implements given interface with default return values.
     *
     * @param token Interface to implement.
     * @param root  Root folder of class.
     * @throws ImplerException If <tt>token</tt> or <tt>root</tt> or <tt>root.getPackage()</tt> are null.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null || token.getPackage() == null) {
            throw new ImplerException("Null pointer in root or token");
        }

        try {
            Files.createDirectories(Paths.get(format("%s/%s", root,
                    token.getPackage().getName().replace('.', File.separatorChar))));
        } catch (IOException e) {
            System.out.println("Something went wrong with creating directories");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(format("%s/%s/%sImpl.java", root.toString(),
                token.getPackage().getName().replace('.', File.separatorChar), token.getSimpleName())))) {
            writer.write(escape(token.getPackage() + ";\n\n"));

            writer.write(escape(Modifier.toString(token.getModifiers())
                    .replace("abstract interface", "class")));
            writer.write(escape(format(" %sImpl implements %s {\n\n", token.getSimpleName(), token.getSimpleName())));

            Method[] methods = token.getMethods();
            for (int i = 0; i < methods.length; i++) {
                addMethod(methods[i], writer);
            }

            writer.write(escape("\n}\n"));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Something went wrong with printing to file");
        }
    }

    public static void main(String[] args) {
        if (args.length > 2) {
            if (args[0].equals("-jar")) {
                Implementor implementor = new Implementor();
                try {
                    implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                } catch (ClassNotFoundException e) {
                    System.out.println("Class not found");
                } catch (ImplerException e) {
                    System.out.println("Exception in implementing class");
                }
            }
        }
    }
}
