package com.draco.pdnsqs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

private const val SETTINGS_PRIVATE_DNS_MODE = "private_dns_mode"
private const val SETTINGS_PRIVATE_DNS_DEFAULT_MODE = "private_dns_default_mode"

private const val PDNS_ON = "hostname"
private const val PDNS_AUTO = "opportunistic"
private const val PDNS_OFF = "off"

enum class PDNS {
    AUTO, ON, OFF
}

class SecureSettings(private val context: Context) {
    fun granted() = context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    fun setMode(mode: PDNS) {
        Settings.Global.putString(context.contentResolver, SETTINGS_PRIVATE_DNS_MODE, when (mode) {
            PDNS.AUTO -> PDNS_AUTO
            PDNS.ON   -> PDNS_ON
            PDNS.OFF  -> PDNS_OFF
        })
    }

    fun mode(): PDNS {
        var mode = Settings.Global.getString(context.contentResolver, SETTINGS_PRIVATE_DNS_MODE)
        if (mode.isNullOrBlank())
            mode = Settings.Global.getString(context.contentResolver, SETTINGS_PRIVATE_DNS_DEFAULT_MODE)

        return when (mode) {
            PDNS_AUTO -> PDNS.AUTO
            PDNS_ON   -> PDNS.ON
            PDNS_OFF  -> PDNS.OFF
            else      -> PDNS.AUTO
        }
    }
}