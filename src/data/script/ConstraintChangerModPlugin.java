package data.script;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ConstraintChangerModPlugin extends BaseModPlugin {

    public static Logger logger = Global.getLogger(ConstraintChangerModPlugin.class);

    public static final boolean HAVE_LUNALIB = Global.getSettings().getModManager().isModEnabled("lunalib");
    public static final String MOD_ID = "Shark_ConstraintChanger";

    /**************************
     * OFFICER LUNAKEYS BELOW *
     **************************/

    public static final String FIELD_MAX_OFFICER_COUNT = "constraintchanger_officerNumber";
    public static final String FIELD_OFFICER_MAX_LEVEL = "constraintchanger_officerMaxLevel";
    public static final String FIELD_MAX_ELITE_SKILLS = "constraintchanger_officerMaxEliteSkills";
    public static final String FIELD_MAX_AI_OFFICER_COUNT = "constraintchanger_officerAIMax";
    public static final String FIELD_MAX_OFFICERS_IN_AI_FLEET = "constraintchanger_maxOfficersInAIFleet";
    public static final String FIELD_MERC_OFFICER_MIN_LEVEL = "constraintchanger_officerMercMinLevel";
    public static final String FIELD_MERC_OFFICER_MAX_LEVEL = "constraintchanger_officerMercMaxLevel";
    public static final String FIELD_MERC_OFFICER_PAY_MULT = "constraintchanger_officerMercPayMult";
    public static final String FIELD_MERC_OFFICER_CONTRACT_DURATION = "constraintchanger_officerMercContractDur";

    /*************************
     * COMBAT LUNAKEYS BELOW *
     *************************/

    public static final String FIELD_MAX_SHIPS_IN_FLEET = "constraintchanger_maxShipsInFleet";
    public static final String FIELD_MAX_SHIPS_IN_AI_FLEET = "constraintchanger_maxShipsInAIFleet";
    public static final String FIELD_MIN_BATTLE_SIZE = "constraintchanger_minBattleSize";
    public static final String FIELD_DEFAULT_BATTLE_SIZE = "constraintchanger_defaultBattleSize";
    public static final String FIELD_MAX_BATTLE_SIZE = "constraintchanger_maxBattleSize";

    /***********************
     * MISC LUNAKEYS BELOW *
     ***********************/

    public static final String FIELD_CAMPAIGN_SPEEDUP_MULT = "constraintchanger_campaignSpeedupMult";
    public static final String FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD = "constraintchanger_useDynamicAutomatedShipsOpThreshold";
    public static final String FIELD_AUTOMATED_SHIPS_OP_THRESHOLD = "constraintchanger_automatedShipsOpThreshold";

    public static final String FIELD_MIN_COMBAT_ZOOM = "constraintchanger_minCombatZoom";
    public static final String FIELD_MAX_COMBAT_ZOOM = "constraintchanger_maxCombatZoom";
    public static final String FIELD_MIN_CAMPAIGN_ZOOM = "constraintchanger_minCampaignZoom";
    public static final String FIELD_MAX_CAMPAIGN_ZOOM = "constraintchanger_maxCampaignZoom";


    /**
     * Map used to map LunaSettings keys to their actual starsector-core/data/config/settings.json keys
     */
    private static final HashMap<String, String> LunaToRealKeymap = new HashMap<>();

    static {
        // Officers
        LunaToRealKeymap.put(FIELD_MAX_OFFICER_COUNT, "baseNumOfficers");
        LunaToRealKeymap.put(FIELD_OFFICER_MAX_LEVEL, "officerMaxLevel");
        LunaToRealKeymap.put(FIELD_MAX_ELITE_SKILLS, "officerMaxEliteSkills");
        LunaToRealKeymap.put(FIELD_MAX_AI_OFFICER_COUNT, "officerAIMax");
        LunaToRealKeymap.put(FIELD_MAX_OFFICERS_IN_AI_FLEET, "maxOfficersInAIFleet");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_MIN_LEVEL, "officerMercMinLevel");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_MAX_LEVEL, "officerMercMaxLevel");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_PAY_MULT, "officerMercPayMult");
        LunaToRealKeymap.put(FIELD_MERC_OFFICER_CONTRACT_DURATION, "officerMercContractDur");
        // Combat
        LunaToRealKeymap.put(FIELD_MAX_SHIPS_IN_FLEET, "maxShipsInFleet");
        LunaToRealKeymap.put(FIELD_MAX_SHIPS_IN_AI_FLEET, "maxShipsInAIFleet");
        LunaToRealKeymap.put(FIELD_MIN_BATTLE_SIZE, "minBattleSize");
        LunaToRealKeymap.put(FIELD_DEFAULT_BATTLE_SIZE, "defaultBattleSize");
        LunaToRealKeymap.put(FIELD_MAX_BATTLE_SIZE, "maxBattleSize");
        // Misc
        LunaToRealKeymap.put(FIELD_CAMPAIGN_SPEEDUP_MULT, "campaignSpeedupMult");
        LunaToRealKeymap.put(FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD, "RKZ_useDynamicAutomatedShipsOpThreshold");
        LunaToRealKeymap.put(FIELD_AUTOMATED_SHIPS_OP_THRESHOLD, "RKZ_automatedShipsOPThreshold");
        LunaToRealKeymap.put(FIELD_MIN_COMBAT_ZOOM, "minCombatZoom");
        LunaToRealKeymap.put(FIELD_MAX_COMBAT_ZOOM, "maxCombatZoom");
        LunaToRealKeymap.put(FIELD_MIN_CAMPAIGN_ZOOM, "minCampaignZoom");
        LunaToRealKeymap.put(FIELD_MAX_CAMPAIGN_ZOOM, "maxCampaignZoom");

    }

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

//        logger.info(">>>> Settings JSON: \n" + Global.getSettings().getSettingsJSON());

        if (HAVE_LUNALIB) {
            LunaSettings.addSettingsListener(new MyLunaSettinsListener());
        }
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        // Add your code here, or delete this method (it does nothing unless you add code)
    }

    /**
     * The {@link LunaSettingsListener} used for updating the backing settings whenever something is changed
     * in Luna that has to do with our mod.
     *
     * Then, the corresponding starsector-core's settings.json keys are also updated (overwritten) with the new values.
     */
    private static class MyLunaSettinsListener implements LunaSettingsListener {

        @Override
        public void settingsChanged(@NotNull String modId) {
            if (modId.equalsIgnoreCase(MOD_ID)) {
                // Officers
                writeLunaSettingToRealSetting(FIELD_OFFICER_MAX_LEVEL);
                writeLunaSettingToRealSetting(FIELD_MAX_OFFICER_COUNT);
                writeLunaSettingToRealSetting(FIELD_MAX_ELITE_SKILLS);
                writeLunaSettingToRealSetting(FIELD_MAX_AI_OFFICER_COUNT);
                writeLunaSettingToRealSetting(FIELD_MAX_OFFICERS_IN_AI_FLEET);
                writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_MIN_LEVEL);
                writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_MAX_LEVEL);
                writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_PAY_MULT);
                writeLunaSettingToRealSetting(FIELD_MERC_OFFICER_CONTRACT_DURATION);
                // Combat
                writeLunaSettingToRealSetting(FIELD_MAX_SHIPS_IN_FLEET);
                writeLunaSettingToRealSetting(FIELD_MAX_SHIPS_IN_AI_FLEET);
                writeLunaSettingToRealSetting(FIELD_MIN_BATTLE_SIZE);
                writeLunaSettingToRealSetting(FIELD_DEFAULT_BATTLE_SIZE);
                writeLunaSettingToRealSetting(FIELD_MAX_BATTLE_SIZE);
                // Misc
                writeLunaSettingToRealSetting(FIELD_CAMPAIGN_SPEEDUP_MULT);
                handleAutomaticShips();
                writeLunaSettingToRealSetting(FIELD_MIN_COMBAT_ZOOM);
                writeLunaSettingToRealSetting(FIELD_MAX_COMBAT_ZOOM);
                writeLunaSettingToRealSetting(FIELD_MIN_CAMPAIGN_ZOOM);
                writeLunaSettingToRealSetting(FIELD_MAX_CAMPAIGN_ZOOM);
            }
        }

        private void handleAutomaticShips() {
            // Automated ships need a bit more love
            // Since these two "real" settings are actually made up, we don't even need to save both of them
            // figure out whether we're using dynamic, save both of the made up settings, but only save what really matters
            // to the real thing controlling the autoships threshold - into BaseSkillEffectDescription
            boolean useDynamicAutoshipOP = safeUnboxing(LunaSettings.getBoolean(MOD_ID, FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD));
            writeLunaSettingToRealSetting(FIELD_USE_DYNAMIC_AUTOMATED_SHIPS_OP_THRESHOLD);
            writeLunaSettingToRealSetting(FIELD_AUTOMATED_SHIPS_OP_THRESHOLD);
            if (useDynamicAutoshipOP) {
                // get default battle size, use 40% of that for automated points OP
                int defaultBattleSize = safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_DEFAULT_BATTLE_SIZE));
                BaseSkillEffectDescription.AUTOMATED_POINTS_THRESHOLD = Math.round((defaultBattleSize * 4) / 10f);
            } else {
                BaseSkillEffectDescription.AUTOMATED_POINTS_THRESHOLD = safeUnboxing(LunaSettings.getInt(MOD_ID, FIELD_AUTOMATED_SHIPS_OP_THRESHOLD));
            }
        }

        /**
         * Method that fetches the <i>lunaKey</i> {@link LunaSettings} value, safely unboxes it via {@link #safeUnboxing(Integer)}
         * then finally overwrites the default starsector-data/settings.json key with the new value after getting the actual
         * key with {@link #convertLunaToRealKey(String)}
         *
         * @param lunaKey Luna key to fetch, convert to real key, then overwrite the real key's value with the fetched one
         */
        private void writeLunaSettingToRealSetting(String lunaKey) {

            Global
                    .getSettings()
                    .setFloat(
                            convertLunaToRealKey(lunaKey),
                            (float) safeUnboxing(LunaSettings.getInt(MOD_ID, lunaKey))
                    );
        }

        /**
         * Method for fetching the actual starsector-core settings.json key name that this <i>lunaKey</i> shadows
         *
         * @param lunaKey the Luna key to fetch the settings.json key for
         * @return the backing settings.json key
         */
        private String convertLunaToRealKey(String lunaKey) {
            return LunaToRealKeymap.get(lunaKey);
        }

        /**
         * Method for safe unboxing. Since we're mainly dealing with Integers here, and can only write
         * to global settings using Floats, the problem of unsafe unboxing shows up in a similar piece of code
         * {@snippet :
         *             Global
         *                     .getSettings()
         *                     .setFloat(
         *                             convertLunaToRealKey(lunaKey),
         *                             new Float(LunaSettings.getInt(MOD_ID, lunaKey))
         *                     );
         *}
         * Namely, if the {@link LunaSettings#getInt(String, String)} method returns null due to having no value
         * for the passed <i>lunaKey</i>, trying to instantiate a new Float out of that would, of course, also fail
         * and crash the program.
         *
         * So this utility method simply checks whether the <i><b>object</b></i> is null, and returns 0 if it is.
         * @param object {@link Integer} object to check and safely unbox and convert to <b>int</b>
         * @return 0 if the <b>object</b> was null, <b>object</b>'s value if it was non-null
         */
        private int safeUnboxing(Integer object) {
            int retVal;
            if (object == null) {
                retVal = 0;
            } else {
                retVal = object;
            }

            return retVal;
        }

        private boolean safeUnboxing(Boolean object) {
            boolean retVal;
            if (object == null) {
                retVal = false;
            } else {
                retVal = object;
            }

            return retVal;
        }
    }
}
