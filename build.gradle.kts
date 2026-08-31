// Root build file. Plugins are declared here with `apply false` so that the
// versions are shared, and applied inside the individual modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
