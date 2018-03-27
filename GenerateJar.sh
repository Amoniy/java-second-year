#!/bin/bash
javac -cp /Users/antonkopitsa/StudioProjects/java-advanced-2018/artifacts/JarImplementorTest.jar \
/Users/antonkopitsa/StudioProjects/java-advanced-2018/java/ru/ifmo/rain/kopitsa/implementor/Implementor.java -d .

jar emcf ru.ifmo.rain.kopitsa.implementor.Implementor META-INF/MANIFEST.MF Implementor.jar ru/ifmo/rain/kopitsa/implementor/Implementor.class
