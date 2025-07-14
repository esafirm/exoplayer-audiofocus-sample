package sample.exoplayer.audiofocus

object DemoPlaylist {
    val playlist = listOf(
        MediaData(
            name = "Cancer",
            uri1 = "https://storage.googleapis.com/esa-simple-storage-2025/mcr_cancer.mp4",
            uri2 = "https://ia902804.us.archive.org/6/items/welcometotheblackparade_201912/Cancer.mp3"
        ),
        MediaData(
            name = "Track 2",
            uri1 = "file:///android_asset/sample_audio_aac.m4a",
        ),
        MediaData(
            name = "New Light",
            uri1 = "https://storage.googleapis.com/esa-simple-storage-2025/new_light.mp3",
            uri2 = "https://storage.googleapis.com/esa-simple-storage-2025/new_light.m4a",
        ),
        MediaData(
            name = "Track 4",
            uri1 = "file:///android_asset/sample_audio.mp3",
        ),
        MediaData(
            name = "1979",
            uri1 = "https://dn720303.ca.archive.org/0/items/tsp2000-11-29.shn/sp112900_d3_t07.mp3",
            uri2 = "https://storage.googleapis.com/esa-simple-storage-2025/1979_128.mp3",
        ),
    )
}

data class MediaData(
    val name: String,
    val uri1: String,
    val uri2: String? = null,
)
