package randoop.util;

import org.checkerframework.checker.determinism.qual.Det;
import randoop.util.RandoopSecurityManager.Status;

/**
 * Wraps a method or constructor together with its arguments. Can be run only once. {@link
 * #hasRun()} indicates whether it has been run.
 *
 * <p>Implemented by parts of Randoop that want to execute reflection code via ReflectionExecutor.
 */
public abstract class ReflectionCode {

  /** Has this started execution? */
  private boolean hasStarted;

  /** Has this been executed already? */
  private boolean hasRun;

  // Before runReflectionCodeRaw is executed, both of these fields are null. After
  // runReflectionCodeRaw is executed, if exceptionThrown is null, then retval is the returned value
  // (which might be null).
  protected Object retval;
  protected Throwable exceptionThrown;

  public final boolean hasStarted() {
    return hasStarted;
  }

  public final boolean hasRun() {
    return hasRun;
  }

  protected final void setHasStarted() {
    if (hasStarted) {
      throw new ReflectionCodeException("cannot run this twice");
    }
    hasStarted = true;
  }

  protected final void setHasRun() {
    if (hasRun) {
      throw new ReflectionCodeException("cannot run this twice");
    }
    hasRun = true;
  }

  /**
   * Runs the reflection code that this object represents.
   *
   * <ol>
   *   <li>If System.getSecurityManager() returns a RandoopSecurityManager, this method sets the
   *       security manager's status to ON.
   *   <li>This method calls {@link #runReflectionCodeRaw()} to perform the actual work. {@link
   *       #runReflectionCodeRaw()} sets the {@code .retVal} or {@code exceptionThrown} field, or
   *       throws an exception if there is a bug in Randoop.
   *   <li>This method sets the security manager's status to its status before this call.
   * </ol>
   *
   * @throws ReflectionCodeException if execution results in conflicting error and success states;
   *     this results from a bug in Randoop
   */
  public final void runReflectionCode(@Det ReflectionCode this) throws ReflectionCodeException {

    this.setHasStarted();

    // if there is a RandoopSecurityManager installed, record its status.
    RandoopSecurityManager randoopsecurity;
    RandoopSecurityManager.Status oldStatus;
    {
      SecurityManager security = System.getSecurityManager();
      if (security instanceof RandoopSecurityManager) {
        randoopsecurity = (RandoopSecurityManager) security;
        oldStatus = randoopsecurity.status;
        randoopsecurity.status = Status.ON;
      } else {
        randoopsecurity = null;
        oldStatus = null;
      }
    }

    try {
      runReflectionCodeRaw();
      this.setHasRun();
    } finally {
      // If a RandoopSecurityManager was installed, restore its status to its original status.
      if (randoopsecurity != null) {
        randoopsecurity.status = oldStatus;
      }
    }
  }

  /**
   * Execute the reflection code. All Randoop implementation errors must be thrown as
   * ReflectionCodeException because everything else is caught.
   *
   * @throws ReflectionCodeException if execution results in conflicting error and success states;
   *     this results from a bug in Randoop
   */
  protected abstract void runReflectionCodeRaw(@Det ReflectionCode this)
      throws ReflectionCodeException;

  public Object getReturnValue(@Det ReflectionCode this) {
    if (!hasRun()) {
      throw new IllegalStateException("run first, then ask");
    }
    return retval;
  }

  public @Det Throwable getExceptionThrown(@Det ReflectionCode this) {
    if (!hasRun()) {
      throw new IllegalStateException("run first, then ask");
    }
    return exceptionThrown;
  }

  /**
   * A suffix to be called by toString().
   *
   * @return the status of the command
   */
  protected String status(@Det ReflectionCode this) {
    if (!hasStarted() && !hasRun()) {
      return " not run yet";
    } else if (hasStarted() && !hasRun()) {
      return " failed to run";
    } else if (!hasStarted() && hasRun()) {
      return " ILLEGAL STATE";
    } else if (exceptionThrown == null) {
      return " returned: " + retval;
    } else {
      return " threw: " + exceptionThrown;
    }
  }

  /** Indicates a bug in the ReflectionCode class. */
  static final class ReflectionCodeException extends IllegalStateException {
    private static final long serialVersionUID = -7508201027241079866L;

    ReflectionCodeException(String msg) {
      super(msg);
    }

    ReflectionCodeException(String msg, Throwable cause) {
      super(msg, cause);
    }

    ReflectionCodeException(Throwable cause) {
      super(cause);
    }
  }
}
