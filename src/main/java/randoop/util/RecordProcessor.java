package randoop.util;

import java.util.List;
import org.checkerframework.checker.determinism.qual.Det;

/** Processes a single record given by RecordListReader. */
public interface RecordProcessor {

  /**
   * Parse the given lines that comprise a record.
   *
   * @param record the lines of a record
   */
  void processRecord(@Det RecordProcessor this, List<String> record);
}
