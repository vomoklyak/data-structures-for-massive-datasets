package com.lesson.dsfms.chapter1;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.MurmurHash3;

@Slf4j
public class HashRing<K, V> {

  private final int maxNumberOfNodes;
  private final Supplier<Integer> nodeHashSupplier;
  private final Map<String, HashRingNode<K, V>> nodeIdToNode;
  private HashRingNode<K, V> head;

  public HashRing(int maxNumberOfNodes) {
    this(maxNumberOfNodes, () -> new Random().nextInt(maxNumberOfNodes));
  }

  public HashRing(int maxNumberOfNodes, Supplier<Integer> nodeHashSupplier) {
    validateMaxNumberOfNodes(maxNumberOfNodes);
    validateNodeHashSupplier(nodeHashSupplier);

    this.maxNumberOfNodes = maxNumberOfNodes;
    this.nodeHashSupplier = nodeHashSupplier;
    this.nodeIdToNode = new HashMap<>();
  }

  private static void validateMaxNumberOfNodes(int maxNumberOfNodes) {
    if (maxNumberOfNodes < 1) {
      throw new IllegalArgumentException(String.format(
          "Max number of nodes should be positive: maxNumberOfNodes=%s", maxNumberOfNodes));
    }
  }

  private static void validateNodeHashSupplier(Supplier<Integer> randomHashSupplier) {
    if (randomHashSupplier == null) {
      throw new IllegalArgumentException("Node hash supplier cannot be null");
    }
  }

  public void addNode(StorageNode<K, V> storageNode) {
    validateStorageNode(storageNode);
    validateNewStorageNode(storageNode);
    validateHasRingSize();

    log.info("Add storage node: id={}}", storageNode.id());
    var newNodeHash = nodeFreeHash();
    var newNode = new HashRingNode<>(storageNode, newNodeHash);
    if (numberOfNodes() == 0) {
      head = newNode;
      head.prev = newNode;
      head.next = newNode;
    } else {
      var newNodeNext = findNode(newNodeHash).orElseThrow();
      newNode.next = newNodeNext;
      newNode.prev = newNodeNext.prev;
      newNodeNext.prev.next = newNode;
      newNodeNext.prev = newNode;
      moveKeys(newNode.next, newNode);
    }
    nodeIdToNode.put(newNode.storageNode.id(), newNode);
  }

  private void validateNewStorageNode(StorageNode<K, V> storageNode) {
    if (nodeIdToNode.containsKey(storageNode.id())) {
      throw new IllegalArgumentException(String.format(
          "HashRing already contains storage node: id=%s", storageNode.id()));
    }
  }

  private void validateHasRingSize() {
    if (numberOfNodes() == maxNumberOfNodes - 1) {
      throw new IllegalArgumentException(String.format(
          "HashRing max number of nodes reached: size=%s, maxNumberOfNodes=%s",
          numberOfNodes(), maxNumberOfNodes));
    }
  }

  private void moveKeys(HashRingNode<K, V> sourceNode, HashRingNode<K, V> destinationNode) {
    sourceNode.storageNode.forEach((key, value) -> {
      var keyHash = keyHash(key);
      if (hashRingDistance(keyHash, sourceNode.hash)
          > hashRingDistance(keyHash, destinationNode.hash)) {
        destinationNode.storageNode.put(key, value);
      }
    });
    destinationNode.storageNode.forEach((key, value) -> sourceNode.storageNode.remove(key));
  }

  public void removeNode(StorageNode<K, V> storageNode) {
    validateExistingStorageNode(storageNode);
    validateExistingStorageNode(storageNode);

    log.info("Remove storage node: id={}}", storageNode.id());
    var node = nodeIdToNode.remove(storageNode.id());
    if (numberOfNodes() == 0) {
      head = null;
    } else {
      node.storageNode.forEach(node.next.storageNode::put);
      node.prev.next = node.next;
      node.next.prev = node.prev;
    }
  }

  private void validateStorageNode(StorageNode<K, V> storageNode) {
    if (storageNode == null) {
      throw new IllegalArgumentException("Storage node cannot be null");
    }
    if (storageNode.id() == null) {
      throw new IllegalArgumentException("Storage node id cannot be null");
    }
  }

  private void validateExistingStorageNode(StorageNode<K, V> storageNode) {
    if (!nodeIdToNode.containsKey(storageNode.id())) {
      throw new IllegalArgumentException(String.format(
          "HashRing does not contain storage node: id=%s", storageNode.id()));
    }
  }

  public int size() {
    return numberOfNodes();
  }

  private int numberOfNodes() {
    return nodeIdToNode.size();
  }

  public V put(K key, V value) {
    validateKey(key);
    validateValue(value);

    var keyHash = keyHash(key);
    log.info("Put key: key={}, keyHash={}}", key, keyHash);
    return findNode(keyHash)
        .map(node -> node.storageNode.put(key, value))
        .orElseThrow(() -> new IllegalArgumentException("Cannot put: hash ring is empty"));
  }

  public Optional<V> get(K key) {
    validateKey(key);

    var keyHash = keyHash(key);
    log.info("Get key: key={}, keyHash={}}", key, keyHash);
    return findNode(keyHash(key))
        .flatMap(node -> node.storageNode.get(key));
  }

  public Optional<V> remove(K key) {
    validateKey(key);

    var keyHash = keyHash(key);
    log.info("Remove key: key={}, keyHash={}}", key, keyHash);
    return findNode(keyHash)
        .flatMap(node -> node.storageNode.remove(key));
  }

  private void validateKey(K key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
  }

  private void validateValue(V value) {
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }
  }

  private Optional<HashRingNode<K, V>> findNode(int hash) {
    if (numberOfNodes() == 0) {
      return Optional.empty();
    }
    var currentNode = head;
    while (hashRingDistance(currentNode.hash, hash)
        > hashRingDistance(currentNode.next.hash, hash)) {
      currentNode = currentNode.next;
    }
    return Optional.of(currentNode.hash == hash ? currentNode : currentNode.next);
  }

  private int hashRingDistance(int leftHash, int rightHash) {
    return leftHash <= rightHash ?
        rightHash - leftHash : rightHash - leftHash + maxNumberOfNodes;
  }

  private int nodeFreeHash() {
    var existingNodeHashes =
        nodeIdToNode.values().stream().map(node -> node.hash).collect(Collectors.toSet());
    var nodeFreeHash = nodeHashSupplier.get();
    while (existingNodeHashes.contains(nodeFreeHash)) {
      nodeFreeHash = (nodeFreeHash + 1) % maxNumberOfNodes;
    }
    return nodeFreeHash;
  }

  private int keyHash(Object object) {
    return hash(object, 0);
  }

  @SneakyThrows
  private int hash(Object object, int seed) {
    try (var byteArrayOutputStream = new ByteArrayOutputStream();
        var objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
      objectOutputStream.writeObject(object);
      var bytes = byteArrayOutputStream.toByteArray();
      var murmurHash3 = MurmurHash3.hash32x86(bytes, 0, bytes.length, seed);
      return Math.abs(murmurHash3 % maxNumberOfNodes);
    }
  }

  private static class HashRingNode<K, V> {

    private final StorageNode<K, V> storageNode;
    private final int hash;
    private HashRingNode<K, V> next;
    private HashRingNode<K, V> prev;

    HashRingNode(StorageNode<K, V> node, int hash) {
      this.storageNode = node;
      this.hash = hash;
    }

    public String toString() {
      return String.format("HashRingNode[id=%s, hash=%s]", storageNode.id(), hash);
    }
  }
}