package com.sifu.mysub.data.source

/**
 * Raised when a bundled `res/raw` payload will not parse.
 *
 * Exists so repositories can map a parse failure without catching Gson's own
 * types, which would leak the transport library upward.
 */
class MalformedRawJsonException(fileName: String, cause: Throwable) :
    RuntimeException("$fileName is malformed", cause)
