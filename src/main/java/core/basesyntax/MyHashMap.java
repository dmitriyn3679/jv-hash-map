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
        // Resize if needed before insert to keep invariant
        if (size >= threshold) {
            resize();
        }
        int hash = hash(key);
        int idx = indexFor(hash, table.length);

        // If bucket empty, drop node in
        Node<K, V> head = table[idx];
        if (head == null) {
            table[idx] = new Node<>(hash, key, value, null);
            size++;
            return;
        }

        // Walk chain: update if same key, else append at head (or tail)
        Node<K, V> current = head;
        while (true) {
            if (current.hash == hash && Objects.equals(current.key, key)) {
                current.value = value; // overwrite existing
                return;
            }
            if (current.next == null) {
                current.next = new Node<>(hash, key, value, null);
                size++;
                return;
            }
            current = current.next;
        }
    }

    @Override
    public V getValue(K key) {
        Node<K, V> node = getNode(key);
        return node == null ? null : node.value;
    }

    @Override
    public int getSize() {
        return size;
    }

    /* ----------------- internals ----------------- */

    private Node<K, V> getNode(K key) {
        int hash = hash(key);
        Node<K, V>[] tab = table;
        int idx = indexFor(hash, tab.length);
        Node<K, V> e = tab[idx];
        while (e != null) {
            if (e.hash == hash && Objects.equals(e.key, key)) {
                return e;
            }
            e = e.next;
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
        int newCap = oldCap << 1; // double
        int newThr = (int) (newCap * loadFactor);
        //noinspection unchecked
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];

        // Rehash nodes into new table
        for (Node<K, V> head : oldTab) {
            Node<K, V> e = head;
            while (e != null) {
                Node<K, V> next = e.next;
                int newIdx = indexFor(e.hash, newCap);
                // Insert at head of new bucket (simple, fast)
                e.next = newTab[newIdx];
                newTab[newIdx] = e;
                e = next;
            }
        }
        table = newTab;
        threshold = newThr;
    }

    // Spread bits to reduce collisions for poor hashCodes (like JDK)
    private static int hash(Object key) {
        int h = (key == null) ? 0 : key.hashCode();
        return h ^ (h >>> 16);
    }

    private static int indexFor(int hash, int length) {
        // length is power of two; mask is faster than %
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

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public int getHash() {
            return hash;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Node<K, V> getNext() {
            return next;
        }

        public void setNext(Node<K, V> next) {
            this.next = next;
        }
    }
}
