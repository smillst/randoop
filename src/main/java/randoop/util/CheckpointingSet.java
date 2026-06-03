package randoop.util;

import java.util.AbstractSet;
import java.util.Iterator;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.mustcall.qual.MustCallUnknown;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * A Set that supports settingcheckpoints (also called "marks") and restoring the data structure's
 * state to them.
 *
 * @param <E> the type of elements
 */
@SuppressWarnings("modifiability:annotation.unverified")
public class CheckpointingSet<E extends @Signed Object> extends AbstractSet<E> {

  // This uses a MultiMap just because that is an existing checkpointing data structure.
  // The value is always true in this mapping, never false.
  public final CheckpointingMultiMap<E, Boolean> map;

  @SuppressWarnings("modifiability:super.invocation") // calls `super`
  public @Modifiable CheckpointingSet() {
    this.map = new CheckpointingMultiMap<>();
  }

  @Override
  public boolean add(@Growable CheckpointingSet<E> this, E elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    if (contains(elt)) throw new IllegalArgumentException("set already contains elt " + elt);
    return map.add(elt, true);
  }

  @Override
  public boolean contains(@MustCallUnknown @UnknownSignedness Object elt) {
    if (elt == null) throw new IllegalArgumentException("arg cannot be null.");
    return map.containsKey(elt);
  }

  @Override
  public boolean remove(
      @Shrinkable CheckpointingSet<E> this, @MustCallUnknown @UnknownSignedness Object elt) {
    if (elt == null) {
      throw new IllegalArgumentException("arg cannot be null.");
    }

    @SuppressWarnings({
      "unchecked",
      "signedness:cast.unsafe" // unchecked cast
    })
    E eltCasted = (E) elt;
    return map.remove(eltCasted, true);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  @SuppressWarnings({
    "modifiability:method.invocation", // cannot verify that CheckpointingSet.this is @Shrinkable.
    "modifiability:override.receiver", // JLS bug: can't write receiver annotation on method of
    // anonymous class
  })
  public Iterator<E> iterator() {
    Iterator<E> underlying = map.keySet().iterator();
    return new Iterator<E>() {
      private E current;

      @Override
      public boolean hasNext() {
        return underlying.hasNext();
      }

      @Override
      public E next() {
        current = underlying.next();
        return current;
      }

      @Override
      public void remove(/* @Shrinkable Iterator<PptTopLevel> this */ ) {
        // Delegate to CheckpointingSet.remove() to preserve checkpointing
        if (current == null) {
          throw new IllegalStateException();
        }
        CheckpointingSet.this.remove(current);
        current = null;
      }
    };
  }

  /** Checkpoint the state of the data structure, for use by {@link #undoToLastMark()}. */
  public void mark() {
    map.mark();
  }

  /** Undo changes since the last call to {@link #mark()}. */
  public void undoToLastMark() {
    map.undoToLastMark();
  }

  @Override
  public String toString() {
    return map.keySet().toString();
  }
}
