package ru.ifmo.rain.kopitsa.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {

    private void createInterface(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null || token.getPackage() == null) {
            throw new ImplerException("Null pointer in root or token");
        }

        try {
            Files.createDirectories(Paths.get(String.format("%s/%s", root,
                    token.getPackage().getName().replaceAll("\\.", "/"))));
        } catch (IOException e) {
            System.out.println("Something went wrong with creating directories");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("%s/%s/%sImpl.java", root.toString(),
                token.getPackage().getName().replaceAll("\\.", "/"), token.getSimpleName())))) {
            writer.write(token.getPackage() + ";\n\n");

            writer.write(Modifier.toString(token.getModifiers()).replace("abstract interface", "class"));
            writer.write(String.format(" %sImpl implements %s {\n\n", token.getSimpleName(), token.getSimpleName()));

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
        writer.write("public ");

        String returnType = method.getReturnType().getCanonicalName();
        writer.write(returnType);
        writer.write(" ");

        writer.write(method.getName());

        writer.write("(");
        for (int i = 0; i < method.getParameterCount(); i++) { // может на стрим?
            writer.write(method.getParameterTypes()[i].getCanonicalName());
            writer.write(String.format(" arg%d", i));
            if (i < method.getParameterCount() - 1) {
                writer.write(", ");
            }
        }
        writer.write(") {\n");

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
        writer.write(String.format("return%s;", defaultReturn));
        writer.write("\n}\n");
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        createInterface(token, root);
    }
}
