package kit.vdf;

public enum VdfKey {

    APP_NAME("appname"),
    EXE_PATH("exe"),
    START_DIR("StartDir"),
    ICON("icon"),
    SHORTCUT_PATH("ShortcutPath"),
    LAUNCH_OPTIONS("LaunchOptions"),
    IS_HIDDEN("IsHidden"),
    ALLOW_DESKTOP_CONFIG("AllowDesktopConfig"),
    ALLOW_OVERLAY("AllowOverlay"),
    OPEN_VR("openvr"),
    DEVKIT("Devkit"),
    DEVKIT_GAME_ID("DevkitGameID"),
    LAST_PLAY_TIME("LastPlayTime"),
    TAGS("tags");

    private final String key;

    VdfKey(String name) {
        this.key = name;
    }

    public String getKey() {
        return key;
    }

}
