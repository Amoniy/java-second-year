package ru.ifmo.rain.kopitsa.arrayset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    private List<E> elements;
    private Comparator<E> comparator = null;

    public ArraySet() {
        this(new ArrayList<>());
    }

    public ArraySet(Collection<? extends E> c) {
        this(c, null);
    }

    public ArraySet(Collection<? extends E> c, Comparator<E> comparator) {
        this.comparator = comparator;
        TreeSet<E> tempSet = new TreeSet<>(comparator);
        tempSet.addAll(c);
        elements = new ArrayList<>(tempSet);
    }

    private ArraySet(List<E> list, Comparator<E> comparator) {
        this.elements = list;
        this.comparator = comparator;
    }

    public ArraySet<E> headSet(E toElement) {
        return subSetInIndexes(0, getIndex(toElement));
    }

    public ArraySet<E> tailSet(E fromElement) {
        return subSetInIndexes(getIndex(fromElement), size());
    }

    public ArraySet<E> subSet(E fromElement, E toElement) {
        return subSetInIndexes(getIndex(fromElement), getIndex(toElement));
    }

    private int getIndex(E element) {
        int index = Collections.binarySearch(elements, element, comparator);
        if (index >= 0) {
            return index;
        } else {
            return (index + 1) * (-1);
        }
    }

    private ArraySet<E> subSetInIndexes(int left, int right) {
        if (left > right) {
            throw new IllegalArgumentException();
        }
        return new ArraySet<>(elements.subList(left, right), comparator);
    }

    public E first() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return elements.get(0);
    }

    public E last() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
        return elements.get(size() - 1);
    }

    public Iterator<E> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }

    public Comparator<? super E> comparator() {
        return comparator;
    }

    public int size() {
        return elements.size();
    }

    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E) o, comparator) >= 0;
    }
}
