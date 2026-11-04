package com.neilturner.aerialviews.ui.overlays

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import coil3.load
import com.neilturner.aerialviews.services.BlueskyPostEvent

/** A compact native Bluesky-style card drawn over the Aerial Views scenery. */
class BlueskyOverlay(context: Context) : LinearLayout(context) {
    private val avatar: ImageView
    private val displayName: TextView
    private val handle: TextView
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
        alpha = 1f
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
        header.addView(avatar, LayoutParams(dp(52), dp(52)))

        val identity = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(13), 0, 0, 0)
        }
        displayName = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 19f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            maxLines = 1
        }
        handle = TextView(context).apply {
            setTextColor(Color.rgb(166, 174, 188))
            textSize = 14f
            maxLines = 1
        }
        identity.addView(displayName)
        identity.addView(handle)
        header.addView(identity, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(header)

        postText = TextView(context).apply {
            setTextColor(Color.rgb(245, 246, 248))
            textSize = 18f
            setLineSpacing(dp(3).toFloat(), 1f)
            maxLines = 7
            setPadding(0, dp(16), 0, dp(8))
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
        displayName.text = post.displayName
        handle.text = post.handle
        postText.text = post.text
        timestamp.text = post.createdAt.orEmpty()
        timestamp.visibility = if (post.createdAt.isNullOrBlank()) View.GONE else View.VISIBLE
        if (post.avatarUrl.isNullOrBlank()) {
            avatar.setImageResource(android.R.drawable.sym_def_app_icon)
        } else {
            avatar.load(post.avatarUrl)
        }
        isVisible = true
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
