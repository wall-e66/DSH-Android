package io.github.jackwener.dsh

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL

/**
 * 启动前的网络验证：GET http://wall-e.cn/DNFDA/RenZheng/admin.txt
 * 2xx → 进入 MainActivity；404 / 超时 / 无网络 → 显示“系统维护中”并提供重试。
 */
class GateActivity : Activity() {

    private val main = Handler(Looper.getMainLooper())
    private var checking = false
    private lateinit var status: TextView
    private lateinit var retry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(Color.parseColor("#0E0F12"))
        root.setPadding(64, 64, 64, 64)

        val title = TextView(this)
        title.text = "Dnfda"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        status = TextView(this)
        status.textSize = 16f
        status.setTextColor(Color.parseColor("#9AA0A6"))
        status.gravity = Gravity.CENTER
        status.setPadding(0, 40, 0, 40)

        retry = Button(this)
        retry.text = "重试"
        retry.visibility = View.GONE
        retry.setOnClickListener { verify() }

        root.addView(title)
        root.addView(status)
        root.addView(retry)
        setContentView(root)

        verify()
    }

    private fun verify() {
        if (checking) return
        checking = true
        status.text = "正在验证…"
        retry.visibility = View.GONE

        Thread {
            val ok = runCatching {
                val conn = URL(GATE_URL).openConnection() as HttpURLConnection
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.requestMethod = "GET"
                conn.setRequestProperty("Cache-Control", "no-cache")
                val code = conn.responseCode
                conn.disconnect()
                code in 200..299
            }.getOrDefault(false)

            main.post {
                checking = false
                if (ok) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    status.text = "系统维护中，请稍后再试"
                    retry.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private companion object {
        const val GATE_URL = "http://wall-e.cn/DNFDA/RenZheng/admin.txt"
    }
}
