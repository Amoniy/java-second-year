package ru.ifmo.rain.kopitsa.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class Implementor implements Impler {

    private void createInterface(Class<?> token, Path root) throws ImplerException {
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
            writer.write(token.getPackage() + ";\n\n");

            writer.write(Modifier.toString(token.getModifiers()).replace("abstract interface", "class"));
            writer.write(format(" %sImpl implements %s {\n\n", token.getSimpleName(), token.getSimpleName()));

            Method[] methods = token.getMethods();
            for (int i = 0; i < methods.length; i++) {
                addMethod(methods[i], writer);
            }

            writer.write("\n}\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Something went wrong with printing to file");
        }
    }

    private void addMethod(Method method, BufferedWriter writer) throws IOException {
        StringBuilder annotationBuilder = new StringBuilder();
        Arrays.stream(method.getAnnotations()).forEach(annotation -> annotationBuilder.append(annotation).append("\n"));
        writer.write(annotationBuilder.toString());

        writer.write("public ");

        String returnType = method.getReturnType().getCanonicalName();
        writer.write(returnType);
        writer.write(" ");

        writer.write(method.getName());

        String parameters = String.join(",", Arrays.stream(method.getParameters()).map(parameter ->
                format("%s %s", parameter.getType().getCanonicalName(), parameter.getName())).collect(toList()));
        writer.write(format("(%s) {\n", parameters));

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
        writer.write(format("return%s;", defaultReturn));
        writer.write("\n}\n");
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        createInterface(token, root);
    }
}
