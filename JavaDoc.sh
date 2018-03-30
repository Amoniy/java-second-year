#!/bin/bash
javadoc \
-cp "/Users/antonkopitsa/StudioProjects/java-advanced-2018/artifacts/JarImplementorTest.jar:/Users/antonkopitsa/StudioProjects/java-advanced-2018/lib/*:/Users/antonkopitsa/StudioProjects/java-advanced-2018/out/production/java-advanced-2018" \
-link https://docs.oracle.com/javase/8/docs/api/ \
-sourcepath java \
-d docs \
-private ru.ifmo.rain.kopitsa.implementor info.kgeorgiy.java.advanced.implementor