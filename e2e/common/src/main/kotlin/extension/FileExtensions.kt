package extension

import com.example.common.ResourceBase
import java.io.File

private object ResourceLoader : ResourceBase {}

fun File.contains(fileName: String) = this.listFiles()?.map {
    it.name
}?.contains(fileName) ?: false

fun File.containsCsv() = this.listFiles()?.filter { it.name.contains(".csv") }?.toList()?.isNotEmpty() ?: false

// uat.propertiesがいないと使えない
fun File.relativePathFromResources(): String =
    this.relativeTo(ResourceLoader.getFileFromResource("uat.properties").parentFile).path
