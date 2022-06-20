modtype = LIB

dependencies {
  implementation(projects.kj.json)
  api(projects.kj.hurricanefx)
  api(projects.kj.fx.fxGraphics)
  api(projects.kj.exec)
  api(projects.k.auto)
//  api(projects.kj.hotkey)
//  implementation(libs.jSystemThemeDetector)
  api(libs.kotlinx.serialization.json)
  implementation(projects.k.caching)
}



plugins {
  kotlin("plugin.serialization")
}