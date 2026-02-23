# 🌊 WaveLift

**WaveLift** is a high-performance desktop application designed to extract and convert YouTube media into high-quality MP3 files. Built with **Compose Multiplatform**, it delivers a native, seamless experience across both macOS and Windows with a modern Material 3 interface.

> ⚡ **Developed with VibeCoding:** This project was built leveraging the **VibeCoding** methodology—prioritizing high-level AI collaboration and rapid prototyping to turn creative ideas into production-ready code with speed and precision.



## ✨ Key Features

- 🚀 **Batch & Single Processing:** Download entire YouTube playlists or individual videos with a single click.
- 🎼 **High-Fidelity Audio:** Choose your preferred quality — 128kbps, 192kbps, or 320kbps for crystal clear sound.
- 🖼 **Auto-Metadata & Cover Art:** Automatically embeds album covers (thumbnails) and ID3 tags (Artist, Title) into your MP3 files.
- 🎨 **Modern Material 3 UI:** A beautiful, responsive interface featuring both Dark and Light mode support.
- 📂 **Custom Output Control:** Easily select your destination folder with new folder creation support.
- 🌐 **Multi-Language Support:** Full Turkish and English language support with one-click switching.
- ⏹ **Download Control:** Cancel active downloads at any time with the stop button.
- ⚠️ **Smart Error Handling:** User-friendly, localized error messages for private videos, unavailable content, and more.

## 🛠 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- **Methodology:** **VibeCoding** (AI-Assisted Development)
- **Extraction Engine:** [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- **Audio Processing:** [FFmpeg](https://ffmpeg.org/)
- **Concurrency:** Kotlin Coroutines

## 🚀 Getting Started

### Installation (Pre-built Binaries)
For the easiest experience, visit the [Releases](https://github.com/doseyenc/WaveLift/releases) page and download the version for your OS:
- **macOS:** Download the `.dmg` file (Supports Intel and Apple Silicon).
- **Windows:** Download the `.msi` or `.exe` installer.

### For Developers
To build and run the project locally:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/doseyenc/WaveLift.git
   ```
2. **Open in IntelliJ IDEA.**
3. **Run the application:**
   Navigate to the Gradle tool window and run `run` under `compose desktop`.
   Or use the terminal:
   ```bash
   ./gradlew :composeApp:run
   ```

## 🤝 Contributing
Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## ☕ Support the Project
I am a software developer with a deep-rooted passion for the mobile and desktop ecosystem. After years of crafting seamless experiences for Android and iOS, I’ve ventured into building multiplatform tools like **WaveLift**.

If WaveLift has made your digital life a bit easier, I’d be honored if you’d consider fueling my next lines of code with a coffee.

[![Buy Me A Coffee](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/doseyenc)

## 📄 License
Distributed under the **MIT License**. See `LICENSE` for more information.

---
Developed with ❤️ by [Çağrı](https://github.com/doseyenc)

