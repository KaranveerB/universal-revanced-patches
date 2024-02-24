include("dummy")

rootProject.name = "universal-revanced-patches"

buildCache {
    local {
        isEnabled = !System.getenv().containsKey("CI")
    }
}
