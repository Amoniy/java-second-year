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

    // можно завести переменную врайтера и тогда не передавать его в каждый метод
    // но тогда теряется возможность удобного ловления эксепшена в tryWithResource
    // private BufferedWriter writer;
    // private void initialiseWriter() {
    //
    // }

    private void createInterface(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null || token.getPackage() == null || token.getPackage().toString() == null) {
            throw new ImplerException();
        }

        // вообще можно бы и пробрасывать IOException
        try {
            Files.createDirectories(Paths.get(String.format("%s/%s", root,
                    token.getPackage().getName().replaceAll("\\.", "/"))));
        } catch (IOException e) {
            System.out.println("Something went wrong 1");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("%s/%s/%sImpl.java", root.toString(),
                token.getPackage().getName().replaceAll("\\.", "/"), token.getSimpleName())))) {
            writer.write(token.getPackage() + ";\n\n");
            writer.write(Modifier.toString(token.getModifiers()).replace("abstract interface", "class"));
            writer.write(String.format(" %sImpl implements %s {\n\n", token.getSimpleName(), token.getSimpleName()));

            Method[] methods = token.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                addMethod(methods[i], writer);
            }

            writer.write("\n}\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Something went wrong 2");
        }
    }

    private void addMethod(Method method, BufferedWriter writer) throws IOException {
        writer.write(Modifier.toString(method.getModifiers()).replace("abstract", "")
                .replace("transient", ""));
        writer.write(" ");
        String returnType;
        returnType = method.getReturnType().toString().replace("class", "");
        returnType = returnType.trim();
        if (returnType.charAt(0) == '[') {
            returnType = returnType.substring(2);
        }
        returnType = returnType.replace(";", "[]");

        writer.write(returnType);
        writer.write(" ");

        writer.write(method.getName());
        writer.write("(");
        for (int i = 0; i < method.getParameterCount(); i++) {
            writer.write(method.getParameters()[i].toString());
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
        if (token.isInterface()) {
        }
    }
}
