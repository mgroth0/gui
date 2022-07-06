dependencies {
  implementation(projects.k.json)
  api(projects.k.hurricanefx)
  api(projects.k.fx.graphics)
  api(projects.k.exec)
  api(projects.k.auto)
//  api(projects.k.hotkey)
//  implementation(libs.jSystemThemeDetector)
  api(libs.kotlinx.serialization.json)
  implementation(projects.k.caching)
//  implementation(libs.javafxsvg)
}



plugins {
  kotlin("plugin.serialization")
}