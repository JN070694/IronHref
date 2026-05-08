# IronHref

A simple, ad-free Android app to store and open URLs. Tap a URL to open it in your default browser.

## Features
- Add URLs manually with a title and URL
- Import from a `.txt` or `.csv` file
- Long press any entry to delete it
- Opens URLs in your default browser via Android Intent
- Zero network permissions — the app itself never touches the internet
- No ads, no analytics, no tracking

## Compatibility
- Android 8.0+ (API 26+)
- Works on GrapheneOS, stock Android, and all Android forks

## Import Format
Create a plain text file with one entry per line:

**CSV format (title + URL):**
```
GitHub,https://github.com
Google,https://google.com
```

**Raw URL format:**
```
https://github.com
https://google.com
```

## Build Instructions
1. Install [Android Studio](https://developer.android.com/studio)
2. Clone this repo
3. Open in Android Studio
4. Build → Generate Signed APK (or run directly on device)

## Sideloading on GrapheneOS
1. Build a signed APK from Android Studio (Build → Generate Signed Bundle/APK)
2. Transfer the APK to your device
3. Enable "Install unknown apps" for your file manager in Settings
4. Tap the APK to install

## Permissions
- None required by the app
- File access is requested at runtime only when you choose to import

## License
GPLv3
