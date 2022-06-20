modtype = LIB

dependencies {
  implementation(projects.kj.json)
  api(projects.kj.hurricanefx)
  api(projects.k.fx.fxGraphics)
  api(projects.k.exec)
  api(projects.k.auto)
//  api(projects.k.hotkey)
//  implementation(libs.jSystemThemeDetector)
  api(libs.kotlinx.serialization.json)
  implementation(projects.k.caching)
}



plugins {
  kotlin("plugin.serialization")
}