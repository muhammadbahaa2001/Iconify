package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_AUTOHIDE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_BOTTOMMARGIN;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_CODE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_LINEHEIGHT;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_FONT_TEXT_SCALING;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TEXT_WHITE;
import static com.drdisagree.iconify.common.Preferences.LSCLOCK_TOPMARGIN;
import static com.drdisagree.iconify.common.Resources.LSCLOCK_FONT_DIR;
import static com.drdisagree.iconify.ui.utils.ViewBindingHelpers.disableNestedScrolling;
import static com.drdisagree.iconify.utils.FileUtil.copyToIconifyHiddenDir;
import static com.drdisagree.iconify.utils.FileUtil.getRealPath;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.databinding.ActivityXposedLockscreenClockBinding;
import com.drdisagree.iconify.ui.adapters.ClockPreviewAdapter;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.ui.views.LockscreenClockStyles;
import com.drdisagree.iconify.utils.SystemUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class XposedLockscreenClock extends BaseActivity implements ColorPickerDialogListener {

    private static int colorLockscreenClock;
    private ActivityXposedLockscreenClockBinding binding;
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && copyToIconifyHiddenDir(path, LSCLOCK_FONT_DIR)) {
                        binding.enableLsclockFont.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    private ColorPickerDialog.Builder colorPickerDialogLockscreenClock;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedLockscreenClockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_lockscreen_clock);

        // Enable lockscreen clock
        binding.enableLockscreenClock.setChecked(RPrefs.getBoolean(LSCLOCK_SWITCH, false));
        binding.enableLockscreenClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_SWITCH, isChecked);
            if (!isChecked) RPrefs.putInt(LSCLOCK_STYLE, 0);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Auto hide clock
        binding.enableAutoHideClock.setChecked(RPrefs.getBoolean(LSCLOCK_AUTOHIDE, false));
        binding.enableAutoHideClock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_AUTOHIDE, isChecked);
            new Handler(Looper.getMainLooper()).postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        });

        // Lockscreen clock style
        binding.lockscreenClockPreview.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        ClockPreviewAdapter adapter = initLockscreenClockStyles();
        binding.lockscreenClockPreview.setAdapter(adapter);
        disableNestedScrolling(binding.lockscreenClockPreview);

        binding.lockscreenClockPreview.setCurrentItem(RPrefs.getInt(LSCLOCK_STYLE, 0));
        binding.lockscreenClockPreviewIndicator.setViewPager(binding.lockscreenClockPreview);
        binding.lockscreenClockPreviewIndicator.tintIndicator(getResources().getColor(R.color.textColorSecondary, getTheme()));

        // Lockscreen clock font picker
        binding.pickLsclockFont.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(this);
            } else {
                browseLSClockFont();
            }
        });

        binding.disableLsclockFont.setVisibility(RPrefs.getBoolean(LSCLOCK_FONT_SWITCH, false) ? View.VISIBLE : View.GONE);

        binding.enableLsclockFont.setOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, false);
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, true);
            binding.enableLsclockFont.setVisibility(View.GONE);
            binding.disableLsclockFont.setVisibility(View.VISIBLE);
        });

        binding.disableLsclockFont.setOnClickListener(v -> {
            RPrefs.putBoolean(LSCLOCK_FONT_SWITCH, false);
            binding.disableLsclockFont.setVisibility(View.GONE);
        });

        // Custom clock color
        binding.enableLsClockCustomColor.setChecked(RPrefs.getBoolean(LSCLOCK_COLOR_SWITCH, false));
        binding.enableLsClockCustomColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_COLOR_SWITCH, isChecked);
            binding.lsClockColorPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        binding.lsClockColorPicker.setVisibility(RPrefs.getBoolean(LSCLOCK_COLOR_SWITCH, false) ? View.VISIBLE : View.GONE);

        // Clock color picker
        colorPickerDialogLockscreenClock = ColorPickerDialog.newBuilder();
        colorLockscreenClock = RPrefs.getInt(LSCLOCK_COLOR_CODE, Color.WHITE);
        colorPickerDialogLockscreenClock.setDialogStyle(R.style.ColorPicker).setColor(colorLockscreenClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        binding.lsClockColorPicker.setOnClickListener(v -> colorPickerDialogLockscreenClock.show(this));
        updateColorPreview();

        // Line height
        final int[] lineHeight = {RPrefs.getInt(LSCLOCK_FONT_LINEHEIGHT, 0)};
        binding.lsclockLineheightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + lineHeight[0] + "dp");
        binding.lsclockLineheightSeekbar.setProgress(lineHeight[0]);
        binding.lsclockLineheightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineHeight[0] = progress;
                binding.lsclockLineheightOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_FONT_LINEHEIGHT, lineHeight[0]);
            }
        });

        // Text Scaling
        final int[] textScaling = {RPrefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10)};
        binding.lsclockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(textScaling[0] / 10.0) + "x");
        binding.lsclockTextscalingSeekbar.setProgress(RPrefs.getInt(LSCLOCK_FONT_TEXT_SCALING, 10));
        binding.lsclockTextscalingSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textScaling[0] = progress;
                binding.lsclockTextscalingOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + new DecimalFormat("#.#").format(progress / 10.0) + "x");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_FONT_TEXT_SCALING, textScaling[0]);
            }
        });

        // Top margin
        final int[] topMargin = {RPrefs.getInt(LSCLOCK_TOPMARGIN, 100)};
        binding.lsclockTopmarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + topMargin[0] + "dp");
        binding.lsclockTopmarginSeekbar.setProgress(topMargin[0]);
        binding.lsclockTopmarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topMargin[0] = progress;
                binding.lsclockTopmarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_TOPMARGIN, topMargin[0]);
            }
        });

        // Bottom margin
        final int[] bottomMargin = {RPrefs.getInt(LSCLOCK_BOTTOMMARGIN, 40)};
        binding.lsclockBottommarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + bottomMargin[0] + "dp");
        binding.lsclockBottommarginSeekbar.setProgress(bottomMargin[0]);
        binding.lsclockBottommarginSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bottomMargin[0] = progress;
                binding.lsclockBottommarginOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(LSCLOCK_BOTTOMMARGIN, bottomMargin[0]);
            }
        });

        // Force white text
        binding.enableForceWhiteText.setChecked(RPrefs.getBoolean(LSCLOCK_TEXT_WHITE, false));
        binding.enableForceWhiteText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(LSCLOCK_TEXT_WHITE, isChecked);
        });
    }

    private ClockPreviewAdapter initLockscreenClockStyles() {
        ArrayList<ClockModel> ls_clock = new ArrayList<>();

        for (int i = 0; i <= 8; i++)
            ls_clock.add(new ClockModel(LockscreenClockStyles.initLockscreenClockStyle(this, i)));

        return new ClockPreviewAdapter(this, ls_clock, LSCLOCK_SWITCH, LSCLOCK_STYLE);
    }

    public void browseLSClockFont() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("font/*");
        startActivityIntent.launch(chooseFile);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 1) {
            colorLockscreenClock = color;
            updateColorPreview();
            RPrefs.putInt(LSCLOCK_COLOR_CODE, colorLockscreenClock);
            colorPickerDialogLockscreenClock.setDialogStyle(R.style.ColorPicker).setColor(colorLockscreenClock).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(true).setShowColorShades(true);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void updateColorPreview() {
        GradientDrawable gd;

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorLockscreenClock, colorLockscreenClock});
        gd.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        binding.previewColorPickerClocktext.setBackground(gd);
    }
}