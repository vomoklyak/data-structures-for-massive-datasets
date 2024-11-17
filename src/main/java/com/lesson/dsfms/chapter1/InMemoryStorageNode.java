package com.lesson.dsfms.chapter1;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public class InMemoryStorageNode<K, V> implements StorageNode<K, V> {

  private final String id = UUID.randomUUID().toString();
  private final Map<K, V> keyToValue = new HashMap<>();

  @Override
  public String id() {
    return id;
  }

  @Override
  public V put(K key, V value) {
    keyToValue.put(key, value);
    return value;
  }

  @Override
  public Optional<V> remove(K key) {
    return Optional.ofNullable(keyToValue.remove(key));
  }

  @Override
  public Optional<V> get(K key) {
    return Optional.ofNullable(keyToValue.get(key));
  }

  @Override
  public void forEach(BiConsumer<K, V> keyValueConsumer) {
    keyToValue.forEach(keyValueConsumer);
  }
}