package ru.ifmo.rain.kopitsa.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class StudentDB implements StudentGroupQuery {

    private Comparator<Student> studentNameComparator = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Student::getId);

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName).collect(toList());
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return students.stream().map(Student::getLastName).collect(toList());
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return students.stream().map(Student::getGroup).collect(toList());
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return students.stream().map(student -> format("%s %s", student.getFirstName(), student.getLastName()))
                .collect(toList());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName).distinct().sorted().collect(toSet());
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Comparator.comparingInt(Student::getId)).get().getFirstName();
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream().sorted(Comparator.comparingInt(Student::getId)).collect(toList());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream().sorted(studentNameComparator).collect(toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return students.stream().filter(student -> student.getFirstName().equals(name))
                .sorted(studentNameComparator).collect(toList());
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return students.stream().filter(student -> student.getLastName().equals(name))
                .sorted(studentNameComparator).collect(toList());
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return students.stream().filter(student -> student.getGroup().equals(group))
                .sorted(studentNameComparator).collect(toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream().filter(student -> student.getGroup().equals(group))
                .collect(toCollection(() -> new TreeSet<>(Comparator.comparing(Student::getFirstName)))).stream()
                .collect(toMap(Student::getLastName, Student::getFirstName));
    }

    private Stream<List<Student>> putStudentsIntoListsByGroup(Collection<Student> students) {
        return students.stream().map(student -> students.stream()
                .filter(innerStudent -> innerStudent.getGroup().equals(student.getGroup()))
                .collect(toList())).distinct();
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return putStudentsIntoListsByGroup(students).map(listOfStudents -> new Group(listOfStudents.get(0).getGroup(),
                listOfStudents.stream().sorted(Comparator.comparing(Student::getLastName)
                        .thenComparing(Student::getFirstName).thenComparing(Student::getId)).collect(toList())))
                .sorted(Comparator.comparing(Group::getName)).collect(toList());
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return putStudentsIntoListsByGroup(students).map(listOfStudents -> new Group(listOfStudents.get(0).getGroup(),
                listOfStudents.stream().sorted(Comparator.comparing(Student::getId)).collect(toList())))
                .sorted(Comparator.comparing(Group::getName)).collect(toList());
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return putStudentsIntoListsByGroup(students)
                .map(listOfStudents -> new Group(listOfStudents.get(0).getGroup(),
                        listOfStudents.stream().sorted(Comparator.comparing(Student::getId)).collect(toList())))
                .min(Comparator.comparing((Group group) -> group.getStudents().size()).reversed()
                        .thenComparing(Group::getName)).get().getName();
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return putStudentsIntoListsByGroup(students)
                .map(listOfStudents -> new Group(listOfStudents.get(0).getGroup(),
                        listOfStudents.stream().sorted(Comparator.comparing(Student::getId)).collect(toList())))
                .min(Comparator.comparing((Group group) -> group.getStudents().stream()
                        .map(Student::getFirstName).distinct().collect(toList()).size()).reversed()
                        .thenComparing(Group::getName)).get().getName();
    }
}
