package ru.ifmo.rain.kopitsa.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {

    private void createInterface(Class<?> token, Path root) {
        BufferedWriter writer;
        try {
            Files.createDirectories(Paths.get(String.format("%s/%s", root, token.getPackage().toString().substring(8).replaceAll("\\.", "/"))));
//            Files.createFile( Paths.get("test/test/Test.java"));
            Files.createFile(Paths.get(String.format("%s/%s/%sImpl.java", root.toString(), token.getPackage().getName().replaceAll("\\.", "/"), token.getSimpleName())));
            writer = Files.newBufferedWriter(Paths.get(String.format("%s/%s/%sImpl.java", root.toString(), token.getPackage().getName().replaceAll("\\.", "/"), token.getSimpleName())));
            writer.write(String.format("%s;\npublic class %sImpl implements %s { }", token.getPackage(), token.getSimpleName(), token.getSimpleName()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Something went wrong");
            return;
        }

    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
//        System.out.println(root.toString());
//        System.out.println(token.getSimpleName());
//        System.out.println(token.getCanonicalName());
//        System.out.println(token.getTypeName());
//        System.out.println(token.getPackage());
//        System.out.println(token.getPackage().toString().substring(8).replaceAll("\\.", "/"));
        createInterface(token, root);

        if (token.isInterface()) {
        }
    }
}
