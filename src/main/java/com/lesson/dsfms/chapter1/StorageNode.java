package com.lesson.dsfms.chapter1;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface StorageNode<K, V> {

  String id();

  V put(K key, V value);

  Optional<V> remove(K key);

  Optional<V> get(K key);

  void forEach(BiConsumer<K, V> keyValueConsumer);
}