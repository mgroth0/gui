package matt.gui.studio

import matt.fx.graphics.fxthread.ensureInFXThreadInPlace
import matt.fx.graphics.studio.Studio
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.window.WindowWrapper
import matt.gui.mstage.MStage
import matt.lang.go


object DebugStudio : Studio() {

    override fun prepareRegion(region: RegionWrapperImpl<*, *>) {
        ensureInFXThreadInPlace {
            debugWindow.value.scene = snapshotScene
            snapshotSceneRoot.clear()
            debugWindow.value.show()
            snapshotSceneRoot.allSides = region
        }
    }

    private val debugWindow = lazy {
        ensureInFXThreadInPlace {
            MStage(decorated = true).apply {
                WindowWrapper.guessMainStage()?.go {
                    initOwner(it)
                }
            }
        }
    }


}