package randoop.reflection;

import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.util.ClassFileConstants;

/**
 * {@code ClassLiteralExtractor} is a {@link ClassVisitor} that extracts literals from the bytecode
 * of each class visited, adding a sequence for each to a map associating a sequence with a type.
 *
 * @see OperationModel
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  private SetMultimap<ClassOrInterfaceType, Sequence> literalMap;

  ClassLiteralExtractor(SetMultimap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  @Override
  public void visitBefore(Class<?> c) {
    Collection<ClassFileConstants.ConstantSet> constList =
        Collections.singletonList(ClassFileConstants.getConstants(c.getName()));
    SetMultimap<Class<?>, NonreceiverTerm> constantMap = ClassFileConstants.toMap(constList);
    for (Class<?> constantClass : constantMap.keySet()) {
      ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(constantClass);
      for (NonreceiverTerm term : constantMap.get(constantClass)) {
        Sequence seq =
            new Sequence()
                .extend(
                    TypedOperation.createNonreceiverInitialization(term),
                    new ArrayList<Variable>());
        literalMap.put(constantType, seq);
      }
    }
  }
}
