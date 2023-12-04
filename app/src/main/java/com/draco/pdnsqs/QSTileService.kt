package com.draco.pdnsqs

import android.os.Build
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

private const val ADB_COMMAND = "adb shell pm grant ${BuildConfig.APPLICATION_ID} android.permission.WRITE_SECURE_SETTINGS"

class QSTileService : TileService() {
    private lateinit var secureSettings: SecureSettings
    private lateinit var dialog: AlertDialog

    private lateinit var clipboardManager: ClipboardManager

    override fun onBind(intent: Intent?): IBinder? {
        secureSettings = SecureSettings(applicationContext)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        dialog = AlertDialog.Builder(applicationContext)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.permission_dismiss) { _, _ -> }
            .setNeutralButton(R.string.permission_copy) { _, _ ->
                val clipData = ClipData.newPlainText(getString(R.string.permission_title), ADB_COMMAND)
                clipboardManager.setPrimaryClip(clipData)
            }
            .create()

        return super.onBind(intent)
    }

    private fun updateTile() {
        qsTile.state = when (secureSettings.mode()) {
            PDNS.ON -> Tile.STATE_ACTIVE
            else    -> Tile.STATE_INACTIVE
        }

        val mode = when (secureSettings.mode()) {
            PDNS.AUTO -> "Auto"
            PDNS.ON   -> "On"
            PDNS.OFF  -> "Off"
        }

        if (Build.VERSION.SDK_INT >= 29) {
            qsTile.subtitle = mode
        } else {
            // Android < 10 does not support subtitle (secondary label), so use label instead
            qsTile.label = "PDNS (${mode})"
        }

        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        if (!secureSettings.granted()) {
            showDialog(dialog)
            return
        }

        secureSettings.setMode(when (secureSettings.mode()) {
            PDNS.AUTO -> PDNS.ON
            PDNS.ON   -> PDNS.OFF
            PDNS.OFF  -> PDNS.AUTO
        })
        updateTile()
    }
}