package com.karanveerb.patches.manifest.appname

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import org.w3c.dom.Element

@Patch(
    name = "Change app name",
    description = "Changes the name of the app to foobar",
    use = false
)
@Suppress("unused")
class ChangeAppNamePatch : ResourcePatch() {
    /**
     * String of one or more characters.
     */
    private val validRegex = Regex("^.+\$")
    private val appNameOption = stringPatchOption(
        key = "appName",
        title = "App name",
        description = "The name to rename the app to",
        required = true
    ) {
        it!!.matches(validRegex)
    }
    override fun execute(context: ResourceContext) {
        val newAppName = this.appNameOption.value!!
        context["AndroidManifest.xml"].apply {
            readText().replace(getLabelElement(context), newAppName).let(::writeText)
        }
    }

    private fun getLabelElement(context: ResourceContext): String {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val manifest = editor.file.getElementsByTagName("manifest").item(0) as Element
            val application = manifest.getElementsByTagName("application").item(0) as Element
            return application.getAttribute("android:label")
        }
    }
}