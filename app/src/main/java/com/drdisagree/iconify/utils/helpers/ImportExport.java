package com.drdisagree.iconify.utils.helpers;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY_LIGHT;
import static com.drdisagree.iconify.common.Preferences.FIRST_INSTALL;
import static com.drdisagree.iconify.common.Preferences.LAND_QQS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.LAND_QSTILE_NONEXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.LAND_QS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE;
import static com.drdisagree.iconify.common.Preferences.PORT_QQS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_EXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QSTILE_NONEXPANDED_HEIGHT;
import static com.drdisagree.iconify.common.Preferences.PORT_QS_TOP_MARGIN;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.SELECTED_ICON_SHAPE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_PROGRESSBAR;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_BG;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_COLOR;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SET;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SHAPE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SIZE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SWITCH;
import static com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME;
import static com.drdisagree.iconify.common.Preferences.UI_CORNER_RADIUS;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Resources.MODULE_DIR;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.overlaymanager.MonetEngineManager;
import com.drdisagree.iconify.overlaymanager.QsMarginManager;
import com.drdisagree.iconify.overlaymanager.QsTileHeightManager;
import com.drdisagree.iconify.overlaymanager.RoundnessManager;
import com.drdisagree.iconify.overlaymanager.SettingsIconResourceManager;
import com.drdisagree.iconify.utils.ColorUtil;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.OnDemandCompiler;
import com.drdisagree.iconify.utils.compiler.SwitchCompiler;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ImportExport {

    public static void exportSettings(SharedPreferences preferences, final @NonNull OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(preferences.getAll());
            objectOutputStream.close();
        } catch (IOException ioException) {
            Log.e("ExportSettings", "Error serializing preferences", ioException);
        } finally {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            outputStream.close();
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean importSettings(SharedPreferences sharedPreferences, final @NonNull InputStream inputStream, boolean restoreOverlays) throws IOException {
        ObjectInputStream objectInputStream = null;
        Map<String, Object> map;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
            map = (Map<String, Object>) objectInputStream.readObject();
        } catch (Exception exception) {
            Log.e("ImportSettings", "Error deserializing preferences", exception);
            return false;
        } finally {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            inputStream.close();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() instanceof Boolean) {
                editor.putBoolean(e.getKey(), (Boolean) e.getValue());
            } else if (e.getValue() instanceof String) {
                editor.putString(e.getKey(), (String) e.getValue());
            } else if (e.getValue() instanceof Integer) {
                editor.putInt(e.getKey(), (int) e.getValue());
            } else if (e.getValue() instanceof Float) {
                editor.putFloat(e.getKey(), (float) e.getValue());
            } else if (e.getValue() instanceof Long) {
                editor.putLong(e.getKey(), (Long) e.getValue());
            } else if (e.getValue() instanceof Set) {
                editor.putStringSet(e.getKey(), (Set<String>) e.getValue());
            } else {
                throw new IllegalArgumentException("Type " + e.getValue().getClass().getName() + " is unknown.");
            }
        }

        boolean status = editor.commit();

        if (restoreOverlays) {
            List<String> commands = new ArrayList<>();
            commands.add("> " + MODULE_DIR + "/common/system.prop; > " + MODULE_DIR + "/post-exec.sh; for ol in $(cmd overlay list | grep -E '^.x.*IconifyComponent' | sed -E 's/^.x..//'); do cmd overlay disable $ol; done");

            SystemUtil.getBootId();
            SystemUtil.getVersionCode();
            SystemUtil.disableBlur();
            editor.putBoolean(ON_HOME_PAGE, true);
            editor.putBoolean(FIRST_INSTALL, false);
            editor.putBoolean(QSPANEL_BLUR_SWITCH, false);

            boolean sip = false, pgb = false, sw = false, tstfrm = false, sis = false, cr = false, me = false, qsth = false, hsize = false;

            for (Map.Entry<String, Object> item : map.entrySet()) {
                if (item.getValue() instanceof Boolean) {
                    if ((Boolean) item.getValue()) {
                        if (item.getKey().startsWith("IconifyComponent") && item.getKey().endsWith(".overlay")) {
                            commands.add(addOverlay(item.getKey()));

                            if (item.getKey().contains("IconifyComponentSIP") && !sip) { // Settings Icon Pack
                                sip = true;
                                try {
                                    int selectedIcon = (int) Objects.requireNonNull(map.get(SELECTED_SETTINGS_ICONS_SET));
                                    int selectedBackground = (int) Objects.requireNonNull(map.get(SELECTED_SETTINGS_ICONS_BG));
                                    int selectedShape = (int) Objects.requireNonNull(map.get(SELECTED_SETTINGS_ICONS_SHAPE));
                                    int selectedSize = (int) Objects.requireNonNull(map.get(SELECTED_SETTINGS_ICONS_SIZE));
                                    int selectedIconColor = (int) Objects.requireNonNull(map.get(SELECTED_SETTINGS_ICONS_COLOR));

                                    SettingsIconResourceManager.enableOverlay(selectedIcon, selectedBackground, selectedShape, selectedSize, selectedIconColor, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building settings icon pack", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentPGB") && !pgb) { // Progressbar Style
                                pgb = true;
                                try {
                                    int selectedStyle = (int) Objects.requireNonNull(map.get(SELECTED_PROGRESSBAR));

                                    OnDemandCompiler.buildOverlay("PGB", selectedStyle + 1, FRAMEWORK_PACKAGE, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building progressbar style", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentSWITCH") && !sw) { // Switch Style
                                sw = true;
                                try {
                                    int selectedStyle = (int) Objects.requireNonNull(map.get(SELECTED_SWITCH));

                                    SwitchCompiler.buildOverlay(selectedStyle + 1, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building switch style", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentTSTFRM") && !tstfrm) { // Toast Frame Style
                                tstfrm = true;
                                try {
                                    int selectedStyle = (int) Objects.requireNonNull(map.get(SELECTED_TOAST_FRAME));

                                    OnDemandCompiler.buildOverlay("TSTFRM", selectedStyle + 1, FRAMEWORK_PACKAGE, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building toast frame style", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentSIS") && !sis) { // Icon Shape Style
                                sis = true;
                                try {
                                    int selectedStyle = (int) Objects.requireNonNull(map.get(SELECTED_ICON_SHAPE));

                                    OnDemandCompiler.buildOverlay("SIS", selectedStyle, FRAMEWORK_PACKAGE, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building icon shape style", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentCR") && !cr) { // UI Roundness
                                cr = true;
                                try {
                                    int radius = (int) Objects.requireNonNull(map.get(UI_CORNER_RADIUS));

                                    RoundnessManager.enableOverlay(radius, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building UI roundness", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentME") && !me) { // Monet Engine
                                me = true;
                                try {
                                    String[][] colors = ColorUtil.getColorNames();
                                    List<List<List<Object>>> palette = new ArrayList<>();
                                    String[] statNames = new String[]{"_day", "_night"};

                                    for (String stat : statNames) {
                                        List<List<Object>> temp = new ArrayList<>();
                                        for (String[] types : colors) {
                                            List<Object> tmp = new ArrayList<>();
                                            for (String color : types) {
                                                tmp.add(Integer.parseInt(Objects.requireNonNull(map.get(color + stat)).toString()));
                                            }
                                            temp.add(tmp);
                                        }
                                        palette.add(temp);
                                    }

                                    MonetEngineManager.enableOverlay(palette, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building Monet Engine", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentQSTH") && !qsth) { // QS Tile Size
                                qsth = true;
                                try {
                                    int pneh = (int) Objects.requireNonNull(map.get(PORT_QSTILE_NONEXPANDED_HEIGHT));
                                    int peh = (int) Objects.requireNonNull(map.get(PORT_QSTILE_EXPANDED_HEIGHT));
                                    int lneh = (int) Objects.requireNonNull(map.get(LAND_QSTILE_NONEXPANDED_HEIGHT));
                                    int leh = (int) Objects.requireNonNull(map.get(LAND_QSTILE_EXPANDED_HEIGHT));

                                    QsTileHeightManager.enableOverlay(pneh, peh, lneh, leh, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building QS Tile Size", exception);
                                }
                            } else if (item.getKey().contains("IconifyComponentHSIZE") && !hsize) { // QS Header Size
                                hsize = true;
                                try {
                                    int pqqs = (int) Objects.requireNonNull(map.get(PORT_QQS_TOP_MARGIN));
                                    int pqs = (int) Objects.requireNonNull(map.get(PORT_QS_TOP_MARGIN));
                                    int lqqs = (int) Objects.requireNonNull(map.get(LAND_QQS_TOP_MARGIN));
                                    int lqs = (int) Objects.requireNonNull(map.get(LAND_QS_TOP_MARGIN));

                                    QsMarginManager.enableOverlay(pqqs, pqs, lqqs, lqs, false);
                                } catch (Exception exception) {
                                    Log.e("ImportSettings", "Error building QS Header Size", exception);
                                }
                            }
                        } else if (item.getKey().startsWith("fabricated")) {
                            String overlayName = item.getKey().replace("fabricated", "");

                            try {
                                if (map.get("FOCMDtarget" + overlayName) == null) {
                                    if (overlayName.contains(COLOR_ACCENT_PRIMARY)) {
                                        String build = "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c " + ICONIFY_COLOR_ACCENT_PRIMARY;
                                        String enable = "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary";
                                        commands.add("echo -e \"" + build + "\n" + enable + "\" >> " + MODULE_DIR + "/post-exec.sh");
                                        commands.add(build);
                                        commands.add(enable);
                                    }
                                    if (overlayName.contains(COLOR_ACCENT_PRIMARY_LIGHT)) {
                                        String build = "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimaryLight android:color/holo_blue_dark 0x1c " + ICONIFY_COLOR_ACCENT_PRIMARY;
                                        String enable = "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimaryLight";
                                        commands.add("echo -e \"" + build + "\n" + enable + "\" >> " + MODULE_DIR + "/post-exec.sh");
                                        commands.add(build);
                                        commands.add(enable);
                                    }
                                    if (overlayName.contains(COLOR_ACCENT_SECONDARY)) {
                                        String build = "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c " + ICONIFY_COLOR_ACCENT_SECONDARY;
                                        String enable = "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary";
                                        commands.add("echo -e \"" + build + "\n" + enable + "\" >> " + MODULE_DIR + "/post-exec.sh");
                                        commands.add(build);
                                        commands.add(enable);
                                    }
                                    if (overlayName.contains(COLOR_ACCENT_SECONDARY_LIGHT)) {
                                        String build = "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondaryLight android:color/holo_green_dark 0x1c " + ICONIFY_COLOR_ACCENT_SECONDARY;
                                        String enable = "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondaryLight";
                                        commands.add("echo -e \"" + build + "\n" + enable + "\" >> " + MODULE_DIR + "/post-exec.sh");
                                        commands.add(build);
                                        commands.add(enable);
                                    }
                                } else {
                                    List<String> tempCommands = FabricatedUtil.buildCommands(
                                            (String) Objects.requireNonNull(map.get("FOCMDtarget" + overlayName)),
                                            (String) Objects.requireNonNull(map.get("FOCMDname" + overlayName)),
                                            (String) Objects.requireNonNull(map.get("FOCMDtype" + overlayName)),
                                            (String) Objects.requireNonNull(map.get("FOCMDresourceName" + overlayName)),
                                            (String) Objects.requireNonNull(map.get("FOCMDval" + overlayName)));

                                    commands.add("echo -e \"" + tempCommands.get(0) + "\n" + tempCommands.get(1) + "\" >> " + MODULE_DIR + "/post-exec.sh");
                                    commands.add(tempCommands.get(0));
                                    commands.add(tempCommands.get(1));
                                }
                            } catch (Exception exception) {
                                Log.e("ImportSettings", "Error building fabricated commands", exception);
                            }
                        }
                    }
                }
            }
            commands.add("sleep 3");
            commands.add("killall com.android.systemui");

            Shell.cmd(String.join("; ", commands)).submit();
        }

        return status;
    }

    private static String addOverlay(String pkgName) {
        return "cmd overlay enable --user current " + pkgName + "; cmd overlay set-priority " + pkgName + " highest";
    }
}
