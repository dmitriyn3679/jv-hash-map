package core.basesyntax;

import java.util.Objects;

public class MyHashMap<K, V> implements MyMap<K, V> {
    // Tunables
    private static final int DEFAULT_INITIAL_CAPACITY = 16; // must be power of two
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAX_CAPACITY = 1 << 30;

    // Table buckets
    private Node<K, V>[] table;
    private int size;
    private int threshold; // resize trigger
    private final float loadFactor;

    public MyHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal loadFactor");
        }
        this.loadFactor = loadFactor;
        int cap = tableSizeFor(initialCapacity);
        //noinspection unchecked
        this.table = (Node<K, V>[]) new Node[cap];
        this.threshold = (int) (cap * loadFactor);
    }

    @Override
    public void put(K key, V value) {
        if (size >= threshold) {
            resize();
        }
        int hash = hash(key);
        int idx = indexFor(hash, table.length);

        Node<K, V> head = table[idx];
        if (head == null) {
            table[idx] = new Node<>(hash, key, value, null);
            size++;
            return;
        }

        Node<K, V> current = head;
        while (true) {
            if (current.getHash() == hash && Objects.equals(current.getKey(), key)) {
                current.setValue(value); // overwrite
                return;
            }
            if (current.getNext() == null) {
                current.setNext(new Node<>(hash, key, value, null));
                size++;
                return;
            }
            current = current.getNext();
        }
    }

    @Override
    public V getValue(K key) {
        Node<K, V> node = getNode(key);
        return node == null ? null : node.getValue();
    }

    @Override
    public int getSize() {
        return size;
    }

    /* ----------------- internals ----------------- */

    private Node<K, V> getNode(K key) {
        int hash = hash(key);
        int idx = indexFor(hash, table.length);
        Node<K, V> e = table[idx];
        while (e != null) {
            if (e.getHash() == hash && Objects.equals(e.getKey(), key)) {
                return e;
            }
            e = e.getNext();
        }
        return null;
    }

    private void resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = oldTab.length;
        if (oldCap >= MAX_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        int newCap = oldCap << 1;
        int newThr = (int) (newCap * loadFactor);
        //noinspection unchecked
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];

        for (Node<K, V> head : oldTab) {
            Node<K, V> e = head;
            while (e != null) {
                Node<K, V> next = e.getNext();
                int newIdx = indexFor(e.getHash(), newCap);

                e.setNext(newTab[newIdx]);  // insert at head
                newTab[newIdx] = e;
                e = next;
            }
        }
        table = newTab;
        threshold = newThr;
    }

    // Spread bits
    private static int hash(Object key) {
        int h = (key == null) ? 0 : key.hashCode();
        return h ^ (h >>> 16);
    }

    private static int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    private static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        n = (n < 0) ? 1 : (n >= MAX_CAPACITY) ? MAX_CAPACITY : n + 1;
        return Math.max(1, n);
    }

    // Singly-linked node
    private static final class Node<K, V> {
        private final int hash;
        private final K key;
        private V value;
        private Node<K, V> next;

        private Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        private int getHash() {
            return hash;
        }

        private K getKey() {
            return key;
        }

        private V getValue() {
            return value;
        }

        private void setValue(V value) {
            this.value = value;
        }

        private Node<K, V> getNext() {
            return next;
        }

        private void setNext(Node<K, V> next) {
            this.next = next;
        }
    }
}
