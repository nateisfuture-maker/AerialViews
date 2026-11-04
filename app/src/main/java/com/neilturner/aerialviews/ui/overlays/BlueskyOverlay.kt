package com.neilturner.aerialviews.ui.overlays

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import coil3.load
import com.neilturner.aerialviews.services.BlueskyPostEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A compact native Bluesky-style post card drawn over the Aerial Views scenery. */
class BlueskyOverlay(context: Context) : LinearLayout(context) {
    private val avatar: ImageView
    private val identity: TextView
    private val postText: TextView
    private val timestamp: TextView

    init {
        orientation = VERTICAL
        setPadding(dp(22), dp(18), dp(22), dp(18))
        background = GradientDrawable().apply {
            setColor(Color.argb(179, 24, 27, 34))
            cornerRadius = dp(22).toFloat()
            setStroke(dp(1), Color.argb(45, 255, 255, 255))
        }
        elevation = dp(8).toFloat()
        visibility = View.GONE

        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        avatar = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(75, 85, 105))
            }
            clipToOutline = true
            setImageResource(android.R.drawable.sym_def_app_icon)
        }
        header.addView(avatar, LayoutParams(dp(48), dp(48)))

        identity = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 16f
            maxLines = 2
            setPadding(dp(12), 0, 0, 0)
        }
        header.addView(identity, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(header)

        postText = TextView(context).apply {
            setTextColor(Color.rgb(245, 246, 248))
            textSize = 18f
            setLineSpacing(dp(3).toFloat(), 1f)
            maxLines = 8
            setPadding(0, dp(14), 0, dp(8))
        }
        addView(postText)

        timestamp = TextView(context).apply {
            setTextColor(Color.rgb(145, 153, 168))
            textSize = 12f
        }
        addView(timestamp)
    }

    fun render(post: BlueskyPostEvent?) {
        if (post == null || post.text.isBlank()) {
            isVisible = false
            return
        }

        val name = post.displayName.ifBlank { "Rice" }
        val handleAndTime = buildString {
            append(post.handle)
            formatTimestamp(post.createdAt)?.let {
                append(" · ")
                append(it)
            }
        }
        identity.text = SpannableStringBuilder(name).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, name.length, 0)
            append("  ")
            val detailStart = length
            append(handleAndTime)
            setSpan(ForegroundColorSpan(Color.rgb(166, 174, 188)), detailStart, length, 0)
        }

        postText.text = post.text
        timestamp.visibility = View.GONE

        avatar.setImageResource(android.R.drawable.sym_def_app_icon)
        if (!post.avatarUrl.isNullOrBlank()) {
            avatar.load(post.avatarUrl) {
                listener(
                    onError = { _, _ ->
                        avatar.setImageResource(android.R.drawable.sym_def_app_icon)
                    },
                )
            }
        }
        isVisible = true
    }

    private fun formatTimestamp(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val instant = Instant.parse(value)
            DateTimeFormatter.ofPattern("MMM d · h:mm a", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }.getOrNull()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
