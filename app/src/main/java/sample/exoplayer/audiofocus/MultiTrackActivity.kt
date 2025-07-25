package sample.exoplayer.audiofocus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.SilenceMediaSource
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
class MultiTrackActivity : ComponentActivity() {

    private lateinit var player1: ExoPlayer
    private lateinit var player2: ExoPlayer

    private var selectedMedia by mutableStateOf<MediaData?>(null)
    private var activeTrack by mutableIntStateOf(1)

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            selectedMedia = if (mediaItem != null) {
                DemoPlaylist.playlist
                    .find { it.uri1 == mediaItem.localConfiguration?.uri.toString() }
            } else {
                null
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            player2.playWhenReady = playWhenReady
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                player2.playbackParameters = PlaybackParameters(
                    player1.playbackParameters.speed,
                    player1.playbackParameters.pitch
                )
                player2.seekTo(player1.currentPosition)
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (oldPosition.mediaItemIndex == newPosition.mediaItemIndex) return
            player2.seekTo(newPosition.mediaItemIndex, newPosition.positionMs)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            player2.playbackParameters = playbackParameters
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPlayers()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MediaPlayerScreen(
                    player = player1,
                    selectedMedia = selectedMedia,
                    activeTrack = activeTrack,
                    onTrackSelected = ::onTrackSelected,
                    onPlay = ::play,
                    onPause = ::pause
                )
            }
        }
    }

    private fun setupPlayers() {
        val mediaClock = Clock.DEFAULT
        player1 = ExoPlayer.Builder(this).setClock(mediaClock).build()
        player2 = ExoPlayer.Builder(this).setClock(mediaClock).build()
    }

    private fun onTrackSelected(track: Int) {
        this.activeTrack = track
        if (track == 1) {
            player2.seekTo(player1.currentPosition)
            player1.volume = 1f
            player2.volume = 0f
        } else {
            player1.seekTo(player2.currentPosition)
            player1.volume = 0f
            player2.volume = 1f
        }
    }

    private fun setupPlaylist() {
        val playlist = DemoPlaylist.playlist
        val firstPlayerMediaItems = playlist.map { MediaItem.fromUri(it.uri1.toUri()) }
        val secondPlayerMediaSources = playlist.map {
            val mediaItem = it.uri2?.let { uri -> MediaItem.fromUri(uri.toUri()) }
            if (mediaItem != null) {
                ProgressiveMediaSource.Factory(DefaultDataSource.Factory(this))
                    .createMediaSource(mediaItem)
            } else {
                SilenceMediaSource(1)
            }
        }

        player1.addListener(playerListener)

        player1.setMediaItems(firstPlayerMediaItems)
        player2.setMediaSources(secondPlayerMediaSources)

        player1.prepare()
        player2.prepare()

        // Default to player 1 being active
        onTrackSelected(1)
    }

    private fun play() {
        if (player1.mediaItemCount == 0) {
            setupPlaylist()
        }
        player1.play()
        player2.play()
    }

    private fun pause() {
        player1.pause()
        player2.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player1.release()
        player2.release()
    }
}

@Composable
fun MediaPlayerScreen(
    player: Player,
    selectedMedia: MediaData?,
    activeTrack: Int,
    onTrackSelected: (Int) -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player } },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        selectedMedia?.let { media ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Now Playing: ${media.name}",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = activeTrack == 1,
                        onClick = { onTrackSelected(1) }
                    )
                    Text("Track 1")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = activeTrack == 2,
                        onClick = { onTrackSelected(2) },
                        enabled = media.uri2 != null
                    )
                    Text("Track 2" + if (media.uri2 == null) " (Silent)" else "")
                }
            }
        }

        Row {
            Button(onClick = onPlay) { Text("Play") }
            Button(
                onClick = onPause,
                modifier = Modifier.padding(start = 8.dp)
            ) { Text("Pause") }
        }
    }
}
