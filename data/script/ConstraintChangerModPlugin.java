package data.script;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ConstraintChangerModPlugin extends BaseModPlugin {

    public static final boolean HAVE_LUNALIB = Global.getSettings().getModManager().isModEnabled("lunalib");
    public static final String MOD_ID = "Shark_ConstraintChanger";

    public static final String FIELD_MAX_OFFICER_COUNT = "constraintchanger_officerNumber";
    public static final String FIELD_OFFICER_MAX_LEVEL = "constraintchanger_officerMaxLevel";

    private static final HashMap<String, String> LunaToRealKeymap = new HashMap<>();

    static {
        LunaToRealKeymap.put(FIELD_MAX_OFFICER_COUNT, "baseNumOfficers");
        LunaToRealKeymap.put(FIELD_OFFICER_MAX_LEVEL, "officerMaxLevel");
    }

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        if (HAVE_LUNALIB) {
            LunaSettings.addSettingsListener(new MyLunaSettinsListener());
        }
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        // Add your code here, or delete this method (it does nothing unless you add code)
    }

    // You can add more methods from ModPlugin here. Press Control-O in IntelliJ to see options.
    private static class MyLunaSettinsListener implements LunaSettingsListener {

        @Override
        public void settingsChanged(@NotNull String modId) {
            if (modId.equalsIgnoreCase(MOD_ID)) {
                Global
                        .getSettings()
                        .setFloat(
                                convertLunaToRealKey(FIELD_OFFICER_MAX_LEVEL),
                                (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_OFFICER_MAX_LEVEL))
                        );

                Global
                        .getSettings()
                        .setFloat(
                                convertLunaToRealKey(FIELD_MAX_OFFICER_COUNT),
                                (float) safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_MAX_OFFICER_COUNT))
                        );
            }
        }

        private String convertLunaToRealKey(String lunaKey) {
            return LunaToRealKeymap.get(lunaKey);
        }

        private int safeUnboxing(Integer object) {
            int retVal = 0;
            if (object == null) {
                retVal = 0;
            } else {
                retVal = object;
            }

            return retVal;
        }
    }
}
