package matt.gui.sound

import matt.caching.cache.LRUCache
import matt.klib.commons.SOUND_FOLDER
import matt.klib.commons.get
import matt.klib.dmap.withStoringDefault
import java.util.concurrent.Semaphore
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import kotlin.concurrent.thread
import kotlin.math.roundToInt

/*
fun SoundMedia(file: java.io.matt.klib.file.File): Media = Media(file.toPath().toUri().toURL().toString())
fun Sound(file: String): Media {
  var s = file
  if ("." !in s) {
	s = SoundFolder.listFiles().first { it.name.substringBefore(".") == s }.name
  }
  return SoundMedia(SoundFolder[s])
}
*/

/*fun play(media: Media): MediaPlayer {*/
/*println("playing media: ${media.source}")
return MediaPlayer(media).apply {
  println("status = ${this.status}")
  isAutoPlay = true
  thread {
	while (this.status !in listOf(Status.READY, Status.PLAYING, Status.DISPOSED)) {
	  println("status: ${this.status}")
	  sleep(1000)
	}
  }

  setOnReady {
	println("mediaplayer ready")
	this.play()
  }
  setOnEndOfMedia {
	this.dispose()
  }
}*/


/*}*/

private val soundsBytes = LRUCache<Pair<String, Number?>, Pair<AudioFormat, ByteArray>>(100)
        .withStoringDefault {
            var s = it.first
            if ("." !in s) {
                s = SOUND_FOLDER.listFiles()!!.first { it.name.substringBefore(".") == s }.name
            }
            val f = SOUND_FOLDER[s]
            val audioInputStream = AudioSystem.getAudioInputStream(f.absoluteFile)
            val format = audioInputStream.format

            val framesPerSec = audioInputStream.format.frameRate
            val bytesPerFrame = audioInputStream.format.frameSize

            if (it.second != null) {
                val lastFrame = (it.second!!.toDouble() * framesPerSec).roundToInt()
                format to audioInputStream.readNBytes(lastFrame * bytesPerFrame)
            } else {
                format to audioInputStream.readBytes()
            }
        }

private val readyClips = LRUCache<Pair<String, Number?>, Clip>(100).withStoringDefault {
    val clip = AudioSystem.getClip()
    val s = soundsBytes[it.first to it.second]
    clip.open(s.first, s.second, 0, s.second.size)
    clip
}

val soundSem = Semaphore(1)

fun prepSoundsInThread(vararg ss: Pair<String, Number?>) {
    thread {
        soundSem.acquire()
        ss.forEach {
//            println("preparing sound: ${it.first}")
            readyClips[it] /*do nothing, just make it*/
        }
        soundSem.release()
    }
}

fun playSound(file: String, sec: Number? = null) {
    soundSem.acquire()
    readyClips[file to sec].start()
    readyClips.remove(file to sec)
    soundSem.release()
}


