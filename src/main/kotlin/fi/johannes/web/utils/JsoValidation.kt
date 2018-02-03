package fi.johannes.web.utils

import io.vertx.core.json.JsonObject
import java.util.*

/**
 * Johannes on 3.2.2018.
 */
object JsonValidation {

  fun validateAgainstKeys(obj: JsonObject,
                          onError: (o: JsonObject) -> Unit,
                          vararg expectedKeys: String): Boolean {
    if (!Arrays.stream(expectedKeys).allMatch({ obj.containsKey(it) })) {
      onError(obj)
      return false
    }
    return true
  }
}
