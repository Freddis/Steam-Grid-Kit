package kit.vdf;

public enum VdfKey {

    APP_ID("appid"),
    APP_NAME("AppName"),
    EXE_PATH("Exe"),
    START_DIR("StartDir"),
    ICON("icon"),
    SHORTCUT_PATH("ShortcutPath"),
    LAUNCH_OPTIONS("LaunchOptions"),
    IS_HIDDEN("IsHidden"),
    ALLOW_DESKTOP_CONFIG("AllowDesktopConfig"),
    ALLOW_OVERLAY("AllowOverlay"),
    OPEN_VR("OpenVR"),
    DEVKIT("Devkit"),
    DEVKIT_GAME_ID("DevkitGameID"),
    DEVKIT_OVERRIDE_GAME_ID("DevkitOverrideAppID"),
    LAST_PLAY_TIME("LastPlayTime"),
    FLATPAK_APP_ID("FlatpakAppID"),
    TAGS("tags");

    private final String key;

    VdfKey(String name) {
        this.key = name;
    }

    public String getKey() {
        return key;
    }

}
