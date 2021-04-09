package randoop.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A SetMultimap that supports checkpointing and restoring to a checkpoint (that is, undoing all
 * operations up to a checkpoint, also called a "mark").
 */
public class CheckpointingMultimap<K, V> implements SetMultimap<K, V> {

  public static boolean verbose_log = false;

  private final SetMultimap<K, V> map;

  /** A stack, each element of which is a count of operations. */
  public final List<Integer> marks;

  private enum Operation {
    ADD,
    REMOVE
  }

  private final List<OpKeyVal> ops;

  private int steps;

  // A triple of an operation, a key, and a value
  private class OpKeyVal {
    final Operation op;
    final K key;
    final V val;

    OpKeyVal(final Operation op, final K key, final V val) {
      this.op = op;
      this.key = key;
      this.val = val;
    }
  }

  public CheckpointingMultimap() {
    map = HashMultimap.create();
    marks = new ArrayList<>();
    ops = new ArrayList<>();
    steps = 0;
  }

  @Override
  public boolean add(K key, V value) {
    if (verbose_log) {
      Log.logPrintf("ADD %s -> %s%n", key, value);
    }
    if (map.put(key, value)) {
      ops.add(new OpKeyVal(Operation.ADD, key, value));
      steps++;
    }
  }

  @Override
  public boolean remove(Object key, V value) {
    if (verbose_log) {
      Log.logPrintf("REMOVE %s -> %s%n", key, value);
    }
    if (map.remove(key, value)) {
      ops.add(new OpKeyVal(Operation.REMOVE, key, value));
      steps++;
      return true;
    } else {
      return false;
    }
  }

  /** Checkpoint the state of the data structure, for use by {@link #undoToLastMark()}. */
  public void mark() {
    marks.add(steps);
    steps = 0;
  }

  /** Undo changes since the last call to {@link #mark()}. */
  public void undoToLastMark() {
    if (marks.isEmpty()) {
      throw new IllegalArgumentException("No marks.");
    }
    Log.logPrintf("marks: %s%n", marks);
    for (int i = 0; i < steps; i++) {
      undoLastOp();
    }
    steps = marks.remove(marks.size() - 1);
  }

  private void undoLastOp() {
    if (ops.isEmpty()) throw new IllegalStateException("ops empty.");
    OpKeyVal last = ops.remove(ops.size() - 1);
    Operation op = last.op;
    K key = last.key;
    V val = last.val;

    if (op == Operation.ADD) {
      // Remove the mapping.
      Log.logPrintf("REMOVE %s%n", key + " ->" + val);
      map.remove(key, val);
    } else if (op == Operation.REMOVE) {
      // Add the mapping.
      Log.logPrintf("ADD %s -> %s%n", key, val);
      map.add(key, val);
    } else {
      // Really, we should never get here.
      throw new IllegalStateException("Unhandled op: " + op);
    }
  }

  @Override
  public Set<V> get(K key) {
    if (key == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.get(key);
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsKey(Object key) {
    if (key == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.containsKey(key);
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public String toString() {
    return map.toString();
  }

  @Override
  public Map<K, Collection<V>> asMap() {
    return map.asMap();
  }
}
