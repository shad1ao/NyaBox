package io.nekohasekai.sagernet.ui

import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.view.View
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.utils.Theme
import io.nekohasekai.sagernet.widget.installTonalNavigationButton

abstract class ThemedActivity : AppCompatActivity {
    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    var themeResId = 0
    var uiMode = 0
    open val isDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isDialog) {
            Theme.apply(this)
        } else {
            Theme.applyDialog(this)
        }
        Theme.applyNightTheme()

        super.onCreate(savedInstanceState)
        if (!isDialog) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        Theme.applySystemBars(this)

        uiMode = resources.configuration.uiMode

        if (!isDialog) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                findViewById<AppBarLayout>(R.id.appbar)?.updatePadding(top = bars.top)
                findViewById<android.view.View>(R.id.nav_view)?.updatePadding(top = bars.top)
                findViewById<android.view.View>(R.id.nav_view_black)?.updatePadding(top = bars.top)
                insets
            }
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.installTonalNavigationButton()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.installTonalNavigationButton()
    }

    override fun setTheme(resId: Int) {
        super.setTheme(resId)

        themeResId = resId
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.uiMode != uiMode) {
            uiMode = newConfig.uiMode
            ActivityCompat.recreate(this)
        }
    }

    fun snackbar(@StringRes resId: Int): Snackbar = snackbar("").setText(resId)
    fun snackbar(text: CharSequence): Snackbar = snackbarInternal(text).apply {
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            maxLines = 10
        }
    }

    internal open fun snackbarInternal(text: CharSequence): Snackbar = throw NotImplementedError()

}