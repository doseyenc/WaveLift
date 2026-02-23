package com.doseyenc.wavelift.engine

import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

/**
 * Desktop notification utility using Java AWT SystemTray.
 */
object NotificationManager {

    fun showNotification(title: String, message: String) {
        try {
            if (!SystemTray.isSupported()) return

            val tray = SystemTray.getSystemTray()
            val image = Toolkit.getDefaultToolkit().createImage("") // Minimal icon
            val trayIcon = TrayIcon(image, "WaveLift")
            trayIcon.isImageAutoSize = true

            tray.add(trayIcon)
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)

            // Remove tray icon after a short delay to avoid clutter
            Thread {
                Thread.sleep(5000)
                tray.remove(trayIcon)
            }.start()
        } catch (e: Exception) {
            // Notification is best-effort, don't crash
            println("Notification failed: ${e.message}")
        }
    }
}
