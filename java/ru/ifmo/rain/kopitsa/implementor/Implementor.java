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
        String returnType;
        returnType = method.getReturnType().getCanonicalName();
        switch (returnType.substring(0, 2)) {
            case "[Z": {// добавить проверку на isPrimitive
                returnType = "boolean;";
                break;
            }
            case "[B": {
                returnType = "byte;";
                break;
            }
            case "[C": {
                returnType = "char;";
                break;
            }
            case "[D": {
                returnType = "double;";
                break;
            }
            case "[F": {
                returnType = "float;";
                break;
            }
            case "[I": {
                returnType = "int;";
                break;
            }
            case "[J": {
                returnType = "long;";
                break;
            }
            case "[S": {
                returnType = "short;";
                break;
            }
        }
        if (returnType.charAt(0) == '[') {
            returnType = returnType.substring(2);
        }
        returnType = returnType.replace(";", "[]");

        writer.write(returnType);
        writer.write(" ");

        writer.write(method.getName());
        writer.write("(");
        for (int i = 0; i < method.getParameterCount(); i++) {
            writer.write(method.getParameterTypes()[i].getCanonicalName());
            writer.write(String.format(" arg%d", i));
            if (i < method.getParameterCount() - 1) {
                writer.write(", ");
            }
        }
        writer.write(") {\n");
        switch (method.getReturnType().toString()) {
            case "int": {
                writer.write("return 0;");
                break;
            }
            case "char": {
                writer.write("return 0;");
                break;
            }
            case "byte": {
                writer.write("return 0;");
                break;
            }
            case "short": {
                writer.write("return 0;");
                break;
            }
            case "long": {
                writer.write("return 0;");
                break;
            }
            case "double": {
                writer.write("return 0;");
                break;
            }
            case "float": {
                writer.write("return 0;");
                break;
            }
            case "void": {
                break;
            }
            case "boolean": {
                writer.write("return false;");
                break;
            }
            default: {
                writer.write("return null;");
                break;
            }
        }

        writer.write("\n}\n");
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        createInterface(token, root);
    }
}
