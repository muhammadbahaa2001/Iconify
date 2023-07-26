package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BLACK_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.DUALTONE_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.FLUID_NOTIF_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_POWERMENU_TRANSPARENCY;
import static com.drdisagree.iconify.common.Preferences.FLUID_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.HEADER_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LIGHT_QSPANEL;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_QQS_TOPMARGIN;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TOPMARGIN;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedQuickSettingsBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedQuickSettings extends BaseActivity {

    private ActivityXposedQuickSettingsBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedQuickSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_quick_settings);

        // Vertical QS Tile
        binding.enableVerticalTile.setChecked(RPrefs.getBoolean(VERTICAL_QSTILE_SWITCH, false));
        binding.enableVerticalTile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(VERTICAL_QSTILE_SWITCH, isChecked);
            binding.hideTileLabel.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });
        binding.hideTileLabel.setEnabled(binding.enableVerticalTile.isChecked());

        // Hide label for vertical tiles
        binding.hideTileLabel.setChecked(RPrefs.getBoolean(HIDE_QSLABEL_SWITCH, false));
        binding.hideTileLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_QSLABEL_SWITCH, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });

        // Light Theme
        binding.enableLightTheme.setChecked(RPrefs.getBoolean(LIGHT_QSPANEL, false));
        binding.enableLightTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LIGHT_QSPANEL, isChecked);
            binding.enableDualTone.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });
        binding.enableDualTone.setEnabled(binding.enableLightTheme.isChecked());

        // Dual Tone
        binding.enableDualTone.setChecked(RPrefs.getBoolean(DUALTONE_QSPANEL, false));
        binding.enableDualTone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(DUALTONE_QSPANEL, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::doubleToggleDarkMode, SWITCH_ANIMATION_DELAY);
        });

        // Pixel Black Theme
        binding.enableBlackTheme.setChecked(RPrefs.getBoolean(BLACK_QSPANEL, false));
        binding.enableBlackTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(BLACK_QSPANEL, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Fluid QS Theme
        binding.enableFluidTheme.setChecked(RPrefs.getBoolean(FLUID_QSPANEL, false));
        binding.enableFluidTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_QSPANEL, isChecked);
            binding.enableNotificationTransparency.setEnabled(isChecked);
            binding.enablePowermenuTransparency.setEnabled(isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });
        binding.enableNotificationTransparency.setEnabled(binding.enableFluidTheme.isChecked());
        binding.enablePowermenuTransparency.setEnabled(binding.enableFluidTheme.isChecked());

        // Fluid QS Notification Transparency
        binding.enableNotificationTransparency.setChecked(RPrefs.getBoolean(FLUID_NOTIF_TRANSPARENCY, false));
        binding.enableNotificationTransparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_NOTIF_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Fluid QS Power Menu Transparency
        binding.enablePowermenuTransparency.setChecked(RPrefs.getBoolean(FLUID_POWERMENU_TRANSPARENCY, false));
        binding.enablePowermenuTransparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(FLUID_POWERMENU_TRANSPARENCY, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // QQS panel top margin slider
        binding.qqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100) + "dp");
        binding.qqsTopMarginSeekbar.setProgress(Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100));
        final int[] qqsTopMargin = {Prefs.getInt(FABRICATED_QQS_TOPMARGIN, 100)};
        binding.qqsTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qqsTopMargin[0] = progress;
                binding.qqsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putInt(FABRICATED_QQS_TOPMARGIN, qqsTopMargin[0]);
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{FRAMEWORK_PACKAGE, "quick_qs_offset_height", "dimen", "quick_qs_offset_height", qqsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qqs_layout_margin_top", "dimen", "qqs_layout_margin_top", qqsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qs_header_row_min_height", "dimen", "qs_header_row_min_height", qqsTopMargin[0] + "dp"}
                );
                RPrefs.putInt(HEADER_QQS_TOPMARGIN, qqsTopMargin[0]);
            }
        });

        // QQS Reset button
        boolean reset_qqs_top_margin_visible = Prefs.getBoolean("fabricatedquick_qs_offset_height", false) || Prefs.getBoolean("fabricatedqqs_layout_margin_top", false) || Prefs.getBoolean("fabricatedqs_header_row_min_height", false);
        binding.resetQqsTopMargin.setVisibility(reset_qqs_top_margin_visible ? View.VISIBLE : View.INVISIBLE);

        binding.resetQqsTopMargin.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlays("quick_qs_offset_height", "qqs_layout_margin_top", "qs_header_row_min_height");
            RPrefs.putInt(HEADER_QQS_TOPMARGIN, -1);
            binding.resetQqsTopMargin.setVisibility(View.INVISIBLE);
            return true;
        });

        // QS panel top margin slider
        binding.qsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100) + "dp");
        binding.qsTopMarginSeekbar.setProgress(Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100));
        final int[] qsTopMargin = {Prefs.getInt(FABRICATED_QS_TOPMARGIN, 100)};
        binding.qsTopMarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qsTopMargin[0] = progress;
                binding.qsTopMarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putInt(FABRICATED_QS_TOPMARGIN, qsTopMargin[0]);
                FabricatedUtil.buildAndEnableOverlays(
                        new Object[]{FRAMEWORK_PACKAGE, "quick_qs_total_height", "dimen", "quick_qs_total_height", qsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qs_panel_padding_top", "dimen", "qs_panel_padding_top", qsTopMargin[0] + "dp"},
                        new Object[]{SYSTEMUI_PACKAGE, "qs_panel_padding_top_combined_headers", "dimen", "qs_panel_padding_top_combined_headers", qsTopMargin[0] + "dp"}
                );
            }
        });

        // QS Reset button
        boolean reset_qs_top_margin_visible = Prefs.getBoolean("fabricatedquick_qs_total_height", false) || Prefs.getBoolean("fabricatedqs_panel_padding_top", false) || Prefs.getBoolean("fabricatedqs_panel_padding_top_combined_headers", false);
        binding.resetQsTopMargin.setVisibility(reset_qs_top_margin_visible ? View.VISIBLE : View.INVISIBLE);

        binding.resetQsTopMargin.setOnLongClickListener(v -> {
            FabricatedUtil.disableOverlays("quick_qs_total_height", "qs_panel_padding_top", "qs_panel_padding_top_combined_headers");
            binding.resetQsTopMargin.setVisibility(View.INVISIBLE);
            return true;
        });
    }
}