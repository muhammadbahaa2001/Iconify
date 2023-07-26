package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_AFTER_BOOT;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.Onboarding;
import com.drdisagree.iconify.utils.helpers.BackupRestore;
import com.drdisagree.iconify.utils.helpers.BinaryInstaller;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Map;

public class ModuleUtil {

    private static final String TAG = "ModuleUtil";
    private static final List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();

    public static void handleModule() {
        if (moduleExists()) {
            // Backup necessary files
            BackupRestore.backupFiles();

            Shell.cmd("rm -rf " + Resources.MODULE_DIR).exec();
        }
        installModule();
    }

    static void installModule() {
        Log.d(TAG, "Magisk module does not exist, creating...");

        // Clean temporary directory
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR).exec();
        Shell.cmd("printf 'id=Iconify\nname=Iconify\nversion=" + BuildConfig.VERSION_NAME + "\nversionCode=" + BuildConfig.VERSION_CODE + "\nauthor=@DrDisagree\ndescription=Systemless module for Iconify.\n' > " + Resources.MODULE_DIR + "/module.prop").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/common").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n\n' > " + Resources.MODULE_DIR + "/post-fs-data.sh").exec();
        if (!Onboarding.skippedInstallation) {
            Shell.cmd("printf 'MODDIR=${0%%/*}\n\nwhile [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\ndo\n sleep 1\ndone\nsleep 5\n\nsh $MODDIR/post-exec.sh\n\nuntil [ -d /storage/emulated/0/Android ]; do\n  sleep 1\ndone\nsleep 3\n\n" + (Prefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false) ? "killall " + SYSTEMUI_PACKAGE + "\n" : "") + "sleep 6\n\nqspbd=$(cmd overlay list |  grep -E \"^.x..IconifyComponentQSPBD.overlay\" | sed -E \"s/^.x..//\")\ndm=$(cmd overlay list |  grep -E \"^.x..IconifyComponentDM.overlay\" | sed -E \"s/^.x..//\")\nif ([ ! -z \"$qspbd\" ] && [ -z \"$dm\" ])\nthen\n cmd overlay disable --user current IconifyComponentQSPBD.overlay\n cmd overlay enable --user current IconifyComponentQSPBD.overlay\n cmd overlay set-priority IconifyComponentQSPBD.overlay highest\nfi\n\nqspba=$(cmd overlay list |  grep -E \"^.x..IconifyComponentQSPBA.overlay\" | sed -E \"s/^.x..//\")\ndm=$(cmd overlay list |  grep -E \"^.x..IconifyComponentDM.overlay\" | sed -E \"s/^.x..//\")\nif ([ ! -z \"$qspba\" ] && [ -z \"$dm\" ])\nthen\n cmd overlay disable --user current IconifyComponentQSPBA.overlay\n cmd overlay enable --user current IconifyComponentQSPBA.overlay\n cmd overlay set-priority IconifyComponentQSPBA.overlay highest\nfi\n\n' > " + Resources.MODULE_DIR + "/service.sh").exec();
        } else {
            Shell.cmd("printf 'MODDIR=${0%%/*}\n\nwhile [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\ndo\n sleep 1\ndone\nsleep 5\n\nsh $MODDIR/post-exec.sh\n\n' > " + Resources.MODULE_DIR + "/service.sh").exec();
        }
        Shell.cmd("touch " + Resources.MODULE_DIR + "/common/system.prop").exec();
        Shell.cmd("touch " + Resources.MODULE_DIR + "/auto_mount").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + Resources.MODULE_DIR + "/system/product/overlay").exec();

        writePostExec();
        BinaryInstaller.symLinkBinaries();

        Log.i(TAG, "Magisk module successfully created.");
    }

    public static void flashModule() {
        if (RootUtil.isMagiskInstalled()) {
            Shell.cmd("magisk --install-module " + Resources.TEMP_DIR + "/Iconify.zip").exec();
        } else {
            Shell.cmd("/data/adb/ksud module install " + Resources.TEMP_DIR + "/Iconify.zip").exec();
        }
    }

    private static void writePostExec() {
        StringBuilder post_exec = new StringBuilder();
        boolean primaryColorEnabled = false;
        boolean secondaryColorEnabled = false;

        SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();
        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().startsWith("fabricated")) {
                String name = item.getKey().replace("fabricated", "");
                List<String> commands = FabricatedUtil.buildCommands(Prefs.getString("FOCMDtarget" + name), Prefs.getString("FOCMDname" + name), Prefs.getString("FOCMDtype" + name), Prefs.getString("FOCMDresourceName" + name), Prefs.getString("FOCMDval" + name));
                post_exec.append(commands.get(0)).append('\n').append(commands.get(1)).append('\n');

                if (name.contains(COLOR_ACCENT_PRIMARY))
                    primaryColorEnabled = true;
                else if (name.contains(COLOR_ACCENT_SECONDARY))
                    secondaryColorEnabled = true;
            }
        }

        if (!primaryColorEnabled && shouldUseDefaultColors() && !Onboarding.skippedInstallation) {
            post_exec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c " + ICONIFY_COLOR_ACCENT_PRIMARY + "\n");
            post_exec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary\n");
            post_exec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimaryLight android:color/holo_blue_dark 0x1c " + ICONIFY_COLOR_ACCENT_PRIMARY + "\n");
            post_exec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimaryLight\n");
        }

        if (!secondaryColorEnabled && shouldUseDefaultColors() && !Onboarding.skippedInstallation) {
            post_exec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_green_light 0x1c " + ICONIFY_COLOR_ACCENT_SECONDARY + "\n");
            post_exec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary\n");
            post_exec.append("cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondaryLight android:color/holo_green_dark 0x1c " + ICONIFY_COLOR_ACCENT_SECONDARY + "\n");
            post_exec.append("cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondaryLight\n");
        }

        Shell.cmd("printf '" + post_exec + "' > " + Resources.MODULE_DIR + "/post-exec.sh").exec();
        Shell.cmd("chmod 755 " + Resources.MODULE_DIR + "/post-exec.sh").exec();
    }

    private static boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay");
    }

    public static boolean moduleExists() {
        return RootUtil.folderExists(Resources.MODULE_DIR);
    }

    public static void extractPremadeOverlays() {
        Log.d(TAG, "Extracting pre-made overlays...");
        try {
            FileUtil.copyAssets("PremadeOverlays");
            Shell.cmd("rm " + Resources.DATA_DIR + "/PremadeOverlays/cheatsheet").exec();
            Shell.cmd("cp -a " + Resources.DATA_DIR + "/PremadeOverlays/. " + Resources.OVERLAY_DIR).exec();
            FileUtil.cleanDir("PremadeOverlays");
            RootUtil.setPermissionsRecursively(644, Resources.OVERLAY_DIR + '/');
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract pre-made overlays.\n" + e);
        }
    }
}
