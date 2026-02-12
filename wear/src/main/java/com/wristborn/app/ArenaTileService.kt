package com.wristborn.app

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ArenaTileService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
