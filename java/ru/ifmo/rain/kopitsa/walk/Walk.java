package ru.ifmo.rain.kopitsa.walk;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Walk {

    public static void main(String[] args) {
        try {
            String inputFilePath = args[0];
            String outputFilePath = args[1];
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), "UTF8"));
                 FileWriter writer = new FileWriter(outputFilePath)) {
                String filename;
                while ((filename = br.readLine()) != null) {
                    writer.write(getHash(filename));
                }
            } catch (FileNotFoundException e) {
                System.out.println("File was not found");
            } catch (UnsupportedEncodingException e) {
                System.out.println("This can't happen or utf-8 is broken");
            } catch (IOException e) {
                System.out.println("Something is wrong in the names file");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No output or input file name was given");
        }
    }

    private static String getHash(String fileName) {
        int hash;
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            hash = 0x811c9dc5;
            byte[] bytes = new byte[1024];
            int len;
            while ((len = fileInputStream.read(bytes)) != -1) {
                for (int i = 0; i < len; i++) {
                    hash = (hash * 0x01000193) ^ (bytes[i] & 0xff);
                }
            }
        } catch (FileNotFoundException e) {
            hash = 0; // not needed
            System.out.println("Input file could not be found");
        } catch (IOException e) {
            hash = 0;
            System.out.println("Something wrong in input file");
        }
        return String.format("%08x %s\n", hash, fileName);
    }
}
