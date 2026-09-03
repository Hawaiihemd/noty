package com.noty.app.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.noty.app.R
import com.noty.app.ui.expressive.AppIdentityRow
import com.noty.app.ui.expressive.CollapsingLargeTopBar
import com.noty.app.ui.expressive.ControlSettingsRow
import com.noty.app.ui.expressive.ExternalLinkRow
import com.noty.app.ui.expressive.SectionGap
import com.noty.app.ui.expressive.SectionHeader
import com.noty.app.ui.expressive.SegmentPosition
import com.noty.app.ui.expressive.SegmentedGroup
import com.noty.app.ui.expressive.SwitchSettingsRow
import com.noty.app.ui.expressive.rememberExpressiveHaptics
import com.noty.app.ui.expressive.rememberExpressiveScrollBehavior
import com.noty.app.utils.ThemeManager

private const val GITHUB_REPO_URL = "https://github.com/usamaiqb/noty"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NotyViewModel,
    onNavigateBack: () -> Unit
) {
    val currentTheme by viewModel.themeFlow.collectAsState(initial = ThemeManager.ThemeMode.SYSTEM)
    val dynamicColors by viewModel.dynamicColorsFlow.collectAsState(initial = true)
    val defaultPin by viewModel.defaultPinFlow.collectAsState(initial = true)
    val scrollBehavior = rememberExpressiveScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            CollapsingLargeTopBar(
                title = stringResource(R.string.settings_title),
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
                navigateBackDescription = stringResource(R.string.cd_back)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(SectionGap)
        ) {
            // ── Appearance ─────────────────────────────────────────────
            Column {
                SectionHeader(stringResource(R.string.settings_section_appearance))
                val supportsDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                SegmentedGroup {
                    ControlSettingsRow(
                        position = if (supportsDynamicColors) {
                            SegmentPosition.First
                        } else {
                            SegmentPosition.Single
                        },
                        title = stringResource(R.string.settings_theme_title),
                        icon = Icons.Rounded.Palette,
                        subtitle = stringResource(R.string.settings_theme_subtitle)
                    ) {
                        ThemeModeControl(
                            selected = currentTheme,
                            onSelect = viewModel::setTheme
                        )
                    }
                    if (supportsDynamicColors) {
                        SwitchSettingsRow(
                            position = SegmentPosition.Last,
                            title = stringResource(R.string.settings_dynamic_color_title),
                            checked = dynamicColors,
                            onCheckedChange = viewModel::setDynamicColors,
                            icon = Icons.Rounded.Wallpaper,
                            subtitle = stringResource(R.string.settings_dynamic_color_subtitle)
                        )
                    }
                }
            }

            // ── Notes ──────────────────────────────────────────────────
            Column {
                SectionHeader(stringResource(R.string.settings_section_notes))
                SegmentedGroup {
                    SwitchSettingsRow(
                        position = SegmentPosition.Single,
                        title = stringResource(R.string.settings_pin_default_title),
                        checked = defaultPin,
                        onCheckedChange = viewModel::setDefaultPin,
                        icon = Icons.Rounded.PushPin,
                        subtitle = stringResource(R.string.settings_pin_default_subtitle)
                    )
                }
            }

            // ── About ──────────────────────────────────────────────────
            Column {
                SectionHeader(stringResource(R.string.settings_section_about))
                SegmentedGroup {
                    ExternalLinkRow(
                        position = SegmentPosition.First,
                        label = stringResource(R.string.settings_github),
                        url = GITHUB_REPO_URL,
                        icon = Icons.Rounded.Code
                    )
                    AppIdentityRow(
                        position = SegmentPosition.Last,
                        appName = stringResource(R.string.app_name),
                        versionLabel = rememberVersionLabel(),
                        icon = Icons.AutoMirrored.Rounded.Notes,
                        tagline = stringResource(R.string.app_tagline)
                    )
                }
            }
        }
    }
}

/**
 * Deliberately not the shared `rememberAppVersionLabel()`. That one formats in English and
 * hides the build number in release builds, whereas Noty reads its format from
 * `R.string.settings_version` and has always shown the code to everyone. Changing what a
 * shipped screen displays is a product decision, so it stays out of this refactor.
 */
@Composable
private fun rememberVersionLabel(): String {
    val context = LocalContext.current
    // remember cannot call stringResource, so read the package info there and
    // format outside it rather than reaching for context.getString in composition.
    val packageVersion = remember(context) {
        runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            packageInfo.versionName to versionCode
        }.getOrNull()
    }
    val versionNameFallback = stringResource(R.string.settings_version_name_fallback)
    return packageVersion?.let { (name, code) ->
        stringResource(R.string.settings_version, name ?: versionNameFallback, code)
    } ?: stringResource(R.string.settings_version_unknown)
}

// ─── Preference controls ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeControl(
    selected: ThemeManager.ThemeMode,
    onSelect: (ThemeManager.ThemeMode) -> Unit
) {
    val haptics = rememberExpressiveHaptics()
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            onClick = {
                haptics.confirm()
                onSelect(ThemeManager.ThemeMode.SYSTEM)
            },
            selected = selected == ThemeManager.ThemeMode.SYSTEM,
            icon = { Icon(Icons.Rounded.BrightnessAuto, contentDescription = null) }
        ) {
            Text(stringResource(R.string.theme_system))
        }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            onClick = {
                haptics.confirm()
                onSelect(ThemeManager.ThemeMode.LIGHT)
            },
            selected = selected == ThemeManager.ThemeMode.LIGHT,
            icon = { Icon(Icons.Rounded.LightMode, contentDescription = null) }
        ) {
            Text(stringResource(R.string.theme_light))
        }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            onClick = {
                haptics.confirm()
                onSelect(ThemeManager.ThemeMode.DARK)
            },
            selected = selected == ThemeManager.ThemeMode.DARK,
            icon = { Icon(Icons.Rounded.DarkMode, contentDescription = null) }
        ) {
            Text(stringResource(R.string.theme_dark))
        }
    }
}
