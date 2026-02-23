package com.doseyenc.wavelift.ui.i18n

enum class Language(val code: String, val displayName: String) {
    TURKISH("tr", "Türkçe"),
    ENGLISH("en", "English")
}

data class Strings(
    // Header
    val appTitle: String,
    val appSubtitle: String,

    // URL Input
    val urlSectionTitle: String,
    val urlPlaceholder: String,
    val analyzeButton: String,

    // Quality
    val qualitySectionTitle: String,
    val qualityLow: String,
    val qualityMedium: String,
    val qualityHigh: String,

    // Settings
    val settingsSectionTitle: String,
    val embedThumbnailTitle: String,
    val embedThumbnailDesc: String,
    val addMetadataTitle: String,
    val addMetadataDesc: String,

    // Output
    val outputSectionTitle: String,
    val outputChangeButton: String,
    val directoryPickerTitle: String,

    // Download
    val downloadButton: String,
    val downloadsSectionTitle: String,

    // States
    val stateIdle: String,
    val stateAnalyzing: String,
    val stateDownloading: String,
    val stateConverting: String,
    val stateCompleted: String,

    // Messages
    val emptyUrlError: String,
    val invalidUrlError: String,
    val downloadComplete: String,
    val playlistAnalyzing: String,
    val singleVideoDetected: String,
    val playlistFailed: String,
    val songsFound: String,
    val ytDlpNotFound: String,
    val unexpectedError: String,
    val exitCodeError: String,
    val downloadError: String,
    val analysisError: String,
    val linkAnalyzing: String,

    // Notifications
    val notificationTitle: String,
    val notificationMessage: String
)

val TurkishStrings = Strings(
    appTitle = "🌊 WaveLift",
    appSubtitle = "YouTube → MP3 Dönüştürücü",
    urlSectionTitle = "Video / Playlist URL",
    urlPlaceholder = "https://youtube.com/watch?v=... veya playlist linki",
    analyzeButton = "Analiz Et",
    qualitySectionTitle = "Ses Kalitesi",
    qualityLow = "Normal",
    qualityMedium = "Yüksek",
    qualityHigh = "En İyi",
    settingsSectionTitle = "Seçenekler",
    embedThumbnailTitle = "Kapak Fotoğrafı Göm",
    embedThumbnailDesc = "MP3 dosyasına thumbnail ekler",
    addMetadataTitle = "Metadata Ekle",
    addMetadataDesc = "Başlık, sanatçı bilgilerini gömer",
    outputSectionTitle = "Kayıt Yeri",
    outputChangeButton = "Değiştir",
    directoryPickerTitle = "Kayıt Yeri Seçin",
    downloadButton = "⬇️  İndir",
    downloadsSectionTitle = "İndirmeler",
    stateIdle = "Sırada bekliyor",
    stateAnalyzing = "Analiz ediliyor...",
    stateDownloading = "İndiriliyor",
    stateConverting = "MP3'e dönüştürülüyor...",
    stateCompleted = "Tamamlandı ✓",
    emptyUrlError = "Lütfen bir URL girin.",
    invalidUrlError = "Geçersiz URL formatı.",
    downloadComplete = "İndirme tamamlandı! 🎵",
    playlistAnalyzing = "Playlist analiz ediliyor...",
    singleVideoDetected = "Tekli video tespit edildi.",
    playlistFailed = "Playlist analizi başarısız oldu.",
    songsFound = "şarkı bulundu, indirmeye hazır!",
    ytDlpNotFound = "yt-dlp bulunamadı! Lütfen yüklü olduğundan emin olun.\nmacOS: brew install yt-dlp\nWindows: winget install yt-dlp",
    unexpectedError = "Beklenmeyen hata",
    exitCodeError = "yt-dlp işlemi hata kodu ile sonlandı",
    downloadError = "İndirme hatası",
    analysisError = "Analiz hatası",
    linkAnalyzing = "Bağlantı analiz ediliyor...",
    notificationTitle = "WaveLift",
    notificationMessage = "İndirme tamamlandı!"
)

val EnglishStrings = Strings(
    appTitle = "🌊 WaveLift",
    appSubtitle = "YouTube → MP3 Converter",
    urlSectionTitle = "Video / Playlist URL",
    urlPlaceholder = "https://youtube.com/watch?v=... or playlist link",
    analyzeButton = "Analyze",
    qualitySectionTitle = "Audio Quality",
    qualityLow = "Normal",
    qualityMedium = "High",
    qualityHigh = "Best",
    settingsSectionTitle = "Options",
    embedThumbnailTitle = "Embed Thumbnail",
    embedThumbnailDesc = "Adds thumbnail to the MP3 file",
    addMetadataTitle = "Add Metadata",
    addMetadataDesc = "Embeds title, artist information",
    outputSectionTitle = "Save Location",
    outputChangeButton = "Change",
    directoryPickerTitle = "Select Save Location",
    downloadButton = "⬇️  Download",
    downloadsSectionTitle = "Downloads",
    stateIdle = "Waiting in queue",
    stateAnalyzing = "Analyzing...",
    stateDownloading = "Downloading",
    stateConverting = "Converting to MP3...",
    stateCompleted = "Completed ✓",
    emptyUrlError = "Please enter a URL.",
    invalidUrlError = "Invalid URL format.",
    downloadComplete = "Download complete! 🎵",
    playlistAnalyzing = "Analyzing playlist...",
    singleVideoDetected = "Single video detected.",
    playlistFailed = "Playlist analysis failed.",
    songsFound = "songs found, ready to download!",
    ytDlpNotFound = "yt-dlp not found! Please make sure it's installed.\nmacOS: brew install yt-dlp\nWindows: winget install yt-dlp",
    unexpectedError = "Unexpected error",
    exitCodeError = "yt-dlp exited with error code",
    downloadError = "Download error",
    analysisError = "Analysis error",
    linkAnalyzing = "Analyzing link...",
    notificationTitle = "WaveLift",
    notificationMessage = "Download complete!"
)

fun getStrings(language: Language): Strings = when (language) {
    Language.TURKISH -> TurkishStrings
    Language.ENGLISH -> EnglishStrings
}
