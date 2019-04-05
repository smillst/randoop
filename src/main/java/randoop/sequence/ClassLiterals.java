package randoop.sequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.SimpleList;

/**
 * For a given class C, ClassLiterals maps C (if present) to a collection of literals (represented
 * as single-element sequences) that are defined in C.
 *
 * <p>These are used preferentially as arguments to methods of class C.
 */
public class ClassLiterals extends MappedSequences<ClassOrInterfaceType> {

  @Override
  public void addSequence(@Det ClassOrInterfaceType key, @Det Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (!seq.isNonreceiver()) {
      throw new IllegalArgumentException("seq is not a primitive sequence");
    }
    super.addSequence(key, seq);
  }

  private static final Map<ClassOrInterfaceType, Set<ClassOrInterfaceType>> hashedSuperClasses =
      new LinkedHashMap<>();

  @Override
  public SimpleList<Sequence> getSequences(
      @Det ClassLiterals this, @Det ClassOrInterfaceType key, @Det Type desiredType) {

    @Det Set<ClassOrInterfaceType> superClasses = hashedSuperClasses.get(key);
    if (superClasses == null) {
      superClasses = getSuperClasses(key);
      hashedSuperClasses.put(key, superClasses);
    }
    List<SimpleList<Sequence>> listOfLists = new ArrayList<>();
    listOfLists.add(super.getSequences(key, desiredType));
    for (ClassOrInterfaceType c : superClasses) {
      listOfLists.add(super.getSequences(c, desiredType));
    }
    return new ListOfLists<>(listOfLists);
  }

  /**
   * Gets superclasses for the given class. Stops at null or Object (excludes Object from result).
   *
   * @param cls the class/interface type
   * @return the superclasses for the given type
   */
  private Set<ClassOrInterfaceType> getSuperClasses(
      @Det ClassLiterals this, @Det ClassOrInterfaceType cls) {
    @Det Set<ClassOrInterfaceType> ret = new LinkedHashSet<>();
    @Det ClassOrInterfaceType sup = cls.getSuperclass();
    while (sup != null && !sup.equals(JavaTypes.OBJECT_TYPE)) {
      ret.add(sup);
      sup = sup.getSuperclass();
    }
    return ret;
  }
}
