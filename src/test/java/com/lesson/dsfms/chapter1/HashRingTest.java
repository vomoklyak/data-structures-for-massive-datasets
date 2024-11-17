package com.lesson.dsfms.chapter1;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HashRingTest {

  @Test
  void shouldPut() {
    // Given
    final var keyOne = "keyOne";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    sut.addNode(storageNodeOne);

    // When
    final var result = sut.put(keyOne, 1); // hash 0

    // Then
    Assertions.assertThat(result).isEqualTo(1);

    Assertions.assertThat(storageNodeOne.get(keyOne)).get()
        .isEqualTo(1);
  }

  @Test
  void shouldGet() {
    // Given
    final var keyOne = "keyOne";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0

    // When
    final var result = sut.get(keyOne);

    // Then
    Assertions.assertThat(result).get()
        .isEqualTo(1);
  }

  @Test
  void shouldGetCaseNonExistentKey() {
    // Given
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    sut.addNode(storageNodeOne);
    sut.put("keyOne", 1); // hash 0

    // When
    final var result = sut.get("keyTwo");

    // Then
    Assertions.assertThat(result).isEmpty();
  }


  @Test
  void shouldRemove() {
    // Given
    final var keyOne = "keyOne";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0

    // When
    final var result = sut.remove(keyOne);

    // Then
    Assertions.assertThat(result).get()
        .isEqualTo(1);
    Assertions.assertThat(storageNodeOne.get(keyOne)).isEmpty();
  }


  @Test
  void shouldRemoveCaseNonExistentKey() {
    // Given
    final var keyOne = "keyOne";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0

    // When
    final var result = sut.remove("keyTwo");

    // Then
    Assertions.assertThat(result).isEmpty();
    Assertions.assertThat(storageNodeOne.get(keyOne)).get()
        .isEqualTo(1);
  }

  @Test
  void shouldAddNodeCaseEmptyHashRing() {
    // Given
    final var keyOne = "keyOne";
    final var keyTwo = "keyTwo";
    final var keyThree = "keyThree";
    final var keyFour = "keyFour";
    final var keyFive = "keyFive";
    final var keySix = "keySix";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    // When
    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0
    sut.put(keyTwo, 2); // hash 3
    sut.put(keyThree, 3); // hash 1
    sut.put(keyFour, 4); // hash 1
    sut.put(keyFive, 5); // hash 1
    sut.put(keySix, 6); // hash 3

    // Then
    Assertions.assertThat(sut.size()).isEqualTo(1);

    Assertions.assertThat(storageNodeOne.get(keyOne)).get()
        .isEqualTo(1);
    Assertions.assertThat(storageNodeOne.get(keyTwo)).get()
        .isEqualTo(2);
    Assertions.assertThat(storageNodeOne.get(keySix)).get()
        .isEqualTo(6);
    Assertions.assertThat(storageNodeOne.get(keyThree)).get()
        .isEqualTo(3);
    Assertions.assertThat(storageNodeOne.get(keyFour)).get()
        .isEqualTo(4);
    Assertions.assertThat(storageNodeOne.get(keyFive)).get()
        .isEqualTo(5);
  }

  @Test
  void shouldAddNodeCaseNodeHashCollision() {
    // Given
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var storageNodeTwo = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);
    sut.addNode(storageNodeOne);

    // When
    sut.addNode(storageNodeTwo);

    // Then
    Assertions.assertThat(sut.size()).isEqualTo(2);

    Assertions.assertThat(nodeHashSupplier.get()).isEqualTo(2);
  }

  @Test
  void shouldAddNodeCaseMoveKeys() {
    // Given
    final var keyOne = "keyOne";
    final var keyTwo = "keyTwo";
    final var keyThree = "keyThree";
    final var keyFour = "keyFour";
    final var keyFive = "keyFive";
    final var keySix = "keySix";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var storageNodeTwo = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    // When
    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0
    sut.put(keyTwo, 2); // hash 3
    sut.put(keyThree, 3); // hash 1
    sut.put(keyFour, 4); // hash 1
    sut.put(keyFive, 5); // hash 1
    sut.put(keySix, 6); // hash 3
    sut.addNode(storageNodeTwo);

    // Then
    Assertions.assertThat(sut.size())
        .isEqualTo(2);

    Assertions.assertThat(storageNodeOne.get(keyOne)).get()
        .isEqualTo(1);
    Assertions.assertThat(storageNodeOne.get(keyTwo)).get()
        .isEqualTo(2);
    Assertions.assertThat(storageNodeOne.get(keySix)).get()
        .isEqualTo(6);
    Assertions.assertThat(storageNodeTwo.get(keyThree)).get()
        .isEqualTo(3);
    Assertions.assertThat(storageNodeTwo.get(keyFour)).get()
        .isEqualTo(4);
    Assertions.assertThat(storageNodeTwo.get(keyFive)).get()
        .isEqualTo(5);
  }

  @Test
  void shouldRemoveNodeCaseSingleNodeHashRing() {
    // Given
    final var keyOne = "keyOne";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0

    // When
    sut.removeNode(storageNodeOne);

    // Then
    Assertions.assertThat(sut.size()).isZero();
    Assertions.assertThat(storageNodeOne.get(keyOne)).get()
        .isEqualTo(1);
  }

  @Test
  void shouldRemoveNodeCaseMoveKeys() {
    final var keyOne = "keyOne";
    final var keyTwo = "keyTwo";
    final var keyThree = "keyThree";
    final var keyFour = "keyFour";
    final var keyFive = "keyFive";
    final var keySix = "keySix";
    final var maxNumberOfNodes = 4;
    final var nodeHashSupplier = sequentialNodeHashSupplier(0, 2);
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();
    final var storageNodeTwo = new InMemoryStorageNode<String, Integer>();
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes, nodeHashSupplier);

    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0
    sut.put(keyTwo, 2); // hash 3
    sut.put(keyThree, 3); // hash 1
    sut.put(keyFour, 4); // hash 1
    sut.put(keyFive, 5); // hash 1
    sut.put(keySix, 6); // hash 3
    sut.addNode(storageNodeTwo);

    // When
    sut.removeNode(storageNodeTwo);

    // Then
    Assertions.assertThat(sut.size())
        .isEqualTo(1);

    Assertions.assertThat(storageNodeOne.get(keyOne)).get()
        .isEqualTo(1);
    Assertions.assertThat(storageNodeOne.get(keyTwo)).get()
        .isEqualTo(2);
    Assertions.assertThat(storageNodeOne.get(keySix)).get()
        .isEqualTo(6);
    Assertions.assertThat(storageNodeOne.get(keyThree)).get()
        .isEqualTo(3);
    Assertions.assertThat(storageNodeOne.get(keyFour)).get()
        .isEqualTo(4);
    Assertions.assertThat(storageNodeOne.get(keyFive)).get()
        .isEqualTo(5);

    Assertions.assertThat(storageNodeTwo.get(keyThree)).get()
        .isEqualTo(3);
    Assertions.assertThat(storageNodeTwo.get(keyFour)).get()
        .isEqualTo(4);
    Assertions.assertThat(storageNodeTwo.get(keyFive)).get()
        .isEqualTo(5);
  }

  @Test
  void shouldConstructHashRing() {
    final var keyOne = "keyOne";
    final var maxNumberOfNodes = 4;
    final var storageNodeOne = new InMemoryStorageNode<String, Integer>();

    // When
    final var sut = new HashRing<String, Integer>(maxNumberOfNodes);
    sut.addNode(storageNodeOne);
    sut.put(keyOne, 1); // hash 0

    // Then
    Assertions.assertThat(storageNodeOne.get(keyOne)).get()
        .isEqualTo(1);
  }

  private Supplier<Integer> sequentialNodeHashSupplier(Integer... hashes) {
    var nodeHashes = new ArrayList<>(List.of(hashes));
    return () -> nodeHashes.remove(0);
  }
}