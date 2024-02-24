package com.karanveerb.unirevanced.patches.all.manifest.icon

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import org.w3c.dom.Element
import java.io.File
import java.nio.file.Files

@Patch(
    name = "Change icon",
    description = "Changes the name of the app to specified name",
    use = false
)
@Suppress("unused")
class ChangeIconPatch : ResourcePatch() {
    private val mipmapTypes =
        arrayOf("ldpi", "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi", "xxxhdpi", "nodpi", "tvdpi", "anydpi");

    private val iconPathOption = stringPatchOption(
        key = "iconPath",
        title = "App icon",
        description = "The app icon to be used.",
        required = true
    ) {
        try {
            File(it!!)
            true
        } catch (_: Exception) {
            false
        }
    }

    private val roundIconPathOption = stringPatchOption(
        key = "roundIconPath",
        title = "Round app icon",
        description = "The round app icon to be used.",
        required = false
    ) {
        try {
            it?.let { File(it) }
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun execute(context: ResourceContext) {
        val iconPath = File(iconPathOption.value!!)
        val newIconName = "_ic_launcher"
        val roundIconPath: File? = roundIconPathOption.value?.let { File(it) }
        val newRoundIconName = "_ic_launcher_round"
        mipmapTypes.forEach { mipmapType ->
            val mipmapDir = context["res"].resolve("mipmap-$mipmapType")
            if (mipmapDir.exists()) {
                Files.write(
                    context["res"].resolve("mipmap-$mipmapType")
                        .resolve("$newIconName.${iconPath.extension}").toPath(),
                    iconPath.readBytes()
                )
                roundIconPath?.let {
                    println("we there")
                    Files.write(
                        context["res"].resolve("mipmap-$mipmapType")
                            .resolve("$newRoundIconName.${roundIconPath.extension}").toPath(),
                        roundIconPath.readBytes()
                    )
                }
            } else {
                println("INFO: skipping mipmap-$mipmapType because it does not exist")
            }
        }

        // Names are typically @mipmap/foo and @mipmap/foo_round. Make sure we don't replace both by accident.
        context["AndroidManifest.xml"].apply {
            readText().replace("${getIconElement(context)}\"", "@mipmap/$newIconName\"").let(::writeText)
        }
        roundIconPath?.let {
            println("we here")
            context["AndroidManifest.xml"].apply {
                readText().replace(getIconElement(context), "@mipmap/$newRoundIconName").let(::writeText)
            }
        }
    }

    private fun getIconElement(context: ResourceContext): String {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val manifest = editor.file.getElementsByTagName("manifest").item(0) as Element
            val application = manifest.getElementsByTagName("application").item(0) as Element
            return application.getAttribute("android:icon")
        }
    }
}