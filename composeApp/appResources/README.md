# WaveLift App Resources

Bu dizine platform-specific binary'ler yerleştirilmelidir:

## macOS (Apple Silicon)
```bash
# yt-dlp
curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos -o macos-arm64/yt-dlp
chmod +x macos-arm64/yt-dlp

# ffmpeg (static build)
# https://evermeet.cx/ffmpeg/ adresinden indirin
```

## macOS (Intel)
```bash
curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos -o macos-x64/yt-dlp
chmod +x macos-x64/yt-dlp
```

## Windows
```powershell
# https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe
# https://www.gyan.dev/ffmpeg/builds/ adresinden ffmpeg.exe indirin
```

> **Not:** Binary'ler .gitignore'a eklenmiştir. Her geliştirici kendi ortamında indirmelidir.
