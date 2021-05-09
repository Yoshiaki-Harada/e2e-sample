package extension

import com.thoughtworks.gauge.ExecutionContext

fun ExecutionContext.getSetupDirectory(): String? {
    val split = this.currentSpecification.name.split("-")
    if (split.size < 2) return null
    return split.last().trim()
}
