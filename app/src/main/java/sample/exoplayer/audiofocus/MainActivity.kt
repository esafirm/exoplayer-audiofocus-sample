package sample.exoplayer.audiofocus

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val playerView = findViewById<PlayerView>(R.id.player)

        val buttonPlayAudio = findViewById<Button>(R.id.btnPlayAudio)
        val buttonPlayVideo = findViewById<Button>(R.id.btnPlayVideo)

        val audioPlayer = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            addListener(LogListener("AudioPlayer"))
        }
        val videoPlayer = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            addListener(LogListener("VideoPlayer"))
        }

        val uriBasePath = "file:///android_asset"
        val sampleAudio = "$uriBasePath/sample_audio.mp3".toUri()
        val sampleVideo = "$uriBasePath/sample_video.mp4".toUri()

        buttonPlayAudio.setOnClickListener {
            val mediaItem = MediaItem.fromUri(sampleAudio)
            audioPlayer.run {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }

        buttonPlayVideo.setOnClickListener {
            playerView.player = videoPlayer

            val mediaItem = MediaItem.fromUri(sampleVideo)
            videoPlayer.run {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }
    }

    class LogListener(private val playerName: String) : Player.Listener {

        init {
            log("Log listener initialized for $playerName")
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            log("onIsLoadingChanged: $isLoading")
        }

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            log("onEvents: $events")
        }

        private fun log(message: String) {
            android.util.Log.d("LogListener", "$playerName => $message")
        }
    }
}
