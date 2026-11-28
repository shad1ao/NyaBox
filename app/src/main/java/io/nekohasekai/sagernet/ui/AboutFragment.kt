package io.nekohasekai.sagernet.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.util.Linkify
import android.view.View
import androidx.core.widget.NestedScrollView
import com.google.android.material.card.MaterialCardView
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import io.nekohasekai.sagernet.widget.Material3GroupedListDecoration
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.databinding.LayoutAboutBinding
import io.nekohasekai.sagernet.ktx.*
import io.nekohasekai.sagernet.plugin.PluginManager.loadString
import io.nekohasekai.sagernet.utils.PackageCache
import io.nekohasekai.sagernet.widget.ListListener
import libcore.Libcore
import moe.matsuri.nb4a.plugin.Plugins
import androidx.core.net.toUri
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.update.AppUpdate
import io.nekohasekai.sagernet.update.AppUpdatePrompt
import io.nekohasekai.sagernet.update.NyaBoxUpdateClient

// Set true to show the About "捐款" row again.
private const val showDonateRow = false

class AboutFragment : ToolbarFragment(R.layout.layout_about) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = LayoutAboutBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(view, ListListener)
        toolbar.setTitle(R.string.menu_about)

        parentFragmentManager.beginTransaction()
            .replace(R.id.about_fragment_holder, AboutContent())
            .commitAllowingStateLoss()

        runOnDefaultDispatcher {
            val license = view.context.assets.open("LICENSE").bufferedReader().readText()
            onMainDispatcher {
                binding.license.text = license
                Linkify.addLinks(binding.license, Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
            }
        }
    }

    class AboutContent : MaterialAboutFragment() {

        val requestIgnoreBatteryOptimizations = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { (resultCode, _) ->
            if (resultCode == Activity.RESULT_OK) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.about_fragment_holder, AboutContent())
                    .commitAllowingStateLoss()
            }
        }

        override fun getMaterialAboutList(activityContext: Context): MaterialAboutList {
            return MaterialAboutList.Builder()
                .addCard(
                    MaterialAboutCard.Builder()
                        .outline(false)
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_baseline_update_24)
                                .text(R.string.app_version)
                                .subText(SagerNet.appVersionNameForDisplay)
                                .setOnClickAction {
                                    requireContext().launchCustomTab(
                                        "https://github.com/shad1ao/NyaBox/releases"
                                    )
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .text(R.string.check_update_release)
                                .setOnClickAction {
                                    checkUpdate(false)
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .text(R.string.check_update_preview)
                                .setOnClickAction {
                                    checkUpdate(true)
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_baseline_layers_24)
                                .text(getString(R.string.version_x, "sing-box"))
                                .subText(Libcore.versionBox().replace(",", ",\u200B"))
                                .setOnClickAction { }
                                .build())
                        .apply {
                            if (showDonateRow) {
                                addItem(
                                    MaterialAboutActionItem.Builder()
                                        .icon(R.drawable.ic_baseline_card_giftcard_24)
                                        .text(R.string.donate)
                                        .subText(R.string.donate_info)
                                        .setOnClickAction {
                                            requireContext().launchCustomTab(
                                                "https://matsuridayo.github.io/index_docs/#donate"
                                            )
                                        }
                                        .build())
                            }
                        }
                        .apply {
                            PackageCache.awaitLoadSync()
                            for ((_, pkg) in PackageCache.installedPluginPackages) {
                                try {
                                    val pluginId =
                                        pkg.providers?.get(0)?.loadString(Plugins.METADATA_KEY_ID)
                                    if (pluginId.isNullOrBlank()) continue
                                    addItem(
                                        MaterialAboutActionItem.Builder()
                                            .icon(R.drawable.ic_baseline_nfc_24)
                                            .text(
                                                getString(
                                                    R.string.version_x,
                                                    pluginId
                                                ) + " (${Plugins.displayExeProvider(pkg.packageName)})"
                                            )
                                            .subText("v" + pkg.versionName)
                                            .setOnClickAction {
                                                startActivity(Intent().apply {
                                                    action =
                                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                                    data = Uri.fromParts(
                                                        "package", pkg.packageName, null
                                                    )
                                                })
                                            }
                                            .build())
                                } catch (e: Exception) {
                                    Logs.w(e)
                                }
                            }
                        }
                        .apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
                                if (!pm.isIgnoringBatteryOptimizations(app.packageName)) {
                                    addItem(
                                        MaterialAboutActionItem.Builder()
                                            .icon(R.drawable.ic_baseline_running_with_errors_24)
                                            .text(R.string.ignore_battery_optimizations)
                                            .subText(R.string.ignore_battery_optimizations_sum)
                                            .setOnClickAction {
                                                requestIgnoreBatteryOptimizations.launch(
                                                    Intent(
                                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                                        "package:${app.packageName}".toUri()
                                                    )
                                                )
                                            }
                                            .build())
                                }
                            }
                        }
                        .build())
                .addCard(
                    MaterialAboutCard.Builder()
                        .outline(false)
                        .title(R.string.project)
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_baseline_sanitizer_24)
                                .text(R.string.github)
                                .setOnClickAction {
                                    requireContext().launchCustomTab(
                                        "https://github.com/shad1ao/NyaBox"

                                    )
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_qu_shadowsocks_foreground)
                                .text(R.string.telegram)
                                .setOnClickAction {
                                    requireContext().launchCustomTab(
                                        "https://t.me/MatsuriDayo"
                                    )
                                }
                                .build())
                        .build())
                .build()

        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // The about library calls setCardElevation(mal_card_elevation) while binding,
            // which puts the 2dp shadow back after the flat card style is inflated.
            val cards = view.findViewById<RecyclerView>(R.id.mal_recyclerview)
            cards.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            cards.isFocusable = false
            cards.isFocusableInTouchMode = false
            cards.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                flattenAboutCards(cards)
            }
            cards.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(child: View) {
                    flattenAboutCard(child)
                    val rows = child.findViewById<RecyclerView>(R.id.mal_card_recyclerview) ?: return
                    rows.isFocusable = false
                    rows.isFocusableInTouchMode = false
                    if ((0 until rows.itemDecorationCount).none {
                            rows.getItemDecorationAt(it) is Material3GroupedListDecoration
                        }
                    ) {
                        rows.addItemDecoration(
                            Material3GroupedListDecoration(rows) { _, _ -> false }
                        )
                    }
                }

                override fun onChildViewDetachedFromWindow(child: View) {}
            })
            // A focused child inside this scroll view pulls the title card under the app bar.
            var ancestor = view.parent
            while (ancestor is View && ancestor !is NestedScrollView) ancestor = ancestor.parent
            (ancestor as? NestedScrollView)?.post { ancestor.scrollTo(0, 0) }
        }

        private fun flattenAboutCards(cards: RecyclerView) {
            for (index in 0 until cards.childCount) {
                flattenAboutCard(cards.getChildAt(index))
            }
        }

        private fun flattenAboutCard(child: View) {
            val card = child as? MaterialCardView ?: return
            card.stateListAnimator = null
            if (card.cardElevation != 0f || card.maxCardElevation != 0f || card.translationZ != 0f) {
                card.cardElevation = 0f
                card.maxCardElevation = 0f
                card.translationZ = 0f
            }
        }

        fun checkUpdate(checkPreview: Boolean) {
            runOnIoDispatcher {
                val abi = NyaBoxUpdateClient.primaryAbi()
                val outcome = if (checkPreview) {
                    NyaBoxUpdateClient.checkPreview(BuildConfig.PRE_VERSION_NAME, abi)
                } else {
                    NyaBoxUpdateClient.checkStable(BuildConfig.VERSION_NAME, abi)
                }
                if (!checkPreview && AppUpdate.rememberSuccessfulCheck(outcome)) {
                    DataStore.appUpdateCheckedAt = System.currentTimeMillis()
                }
                runOnMainDispatcher {
                    if (!isAdded) return@runOnMainDispatcher
                    AppUpdatePrompt.show(requireContext(), outcome, manual = true)
                }
            }
        }

    }

}