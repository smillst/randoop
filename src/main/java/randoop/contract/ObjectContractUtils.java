package randoop.contract;

import org.checkerframework.checker.determinism.qual.Det;
import randoop.ExecutionOutcome;
import randoop.sequence.Variable;
import randoop.util.ObjectContractReflectionCode;
import randoop.util.ReflectionCode;
import randoop.util.ReflectionExecutor;

/** Utility methods for safely executing and printing {@link ObjectContract} code. */
public class ObjectContractUtils {

  private ObjectContractUtils() {
    throw new Error("Do not instantiate");
  }

  /**
   * Executes the given contract via reflection.
   *
   * @param c the contract to execute
   * @param objs the list of values to substitute for variables
   * @return the outcome from the execution
   */
  public static ExecutionOutcome execute(
      final @Det ObjectContract c, final @Det Object @Det ... objs) {
    ReflectionCode refl = new ObjectContractReflectionCode(c, objs);
    return ReflectionExecutor.executeReflectionCode(refl);
  }

  /**
   * Replace dummy variables such as "x0" in the code by their real names.
   *
   * @param str the contract code as a string with dummy variables
   * @param vars list of {@link randoop.sequence.Variable Variable} objects
   * @return the contract code with actual variable names substituted for dummy names
   */
  public static String localizeContractCode(@Det String str, @Det Variable... vars) {
    for (int i = 0; i < vars.length; i++) {
      // See documentation for ObjectContract.toCommentString().
      String pattern = "\\bx" + i + "\\b";
      str = str.replaceAll(pattern, vars[i].getName());
    }
    return str;
  }
}
