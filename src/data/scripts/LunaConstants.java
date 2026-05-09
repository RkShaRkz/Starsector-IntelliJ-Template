package data.scripts;

/**
 * Class holding all {@link lunalib.lunaSettings.LunaSettings} keys that the {@link lunalib.lunaSettings.LunaSettingsListener}
 * will listen to, that are used in the src/data/config/LunaSettings.csv file
 */
public class LunaConstants {

    // general settings
    public final static String ENABLE_RPG_MINIGAME = "vayramerged_enableRpgMinigame";
    public final static String ENABLE_VAYRA_DEBUG = "vayramerged_enableVayraDebug";
    public final static String ENABLE_COLONIAL_MANAGER_LOGGING = "vayramerged_enableColonialManagerLogging";
    public final static String VAYRA_BOUNTY_DURATION = "vayramerged_defaultBountyDuration";
    public final static String VAYRA_PLAYER_BOUNTY_DURATION = "vayramerged_defaultPlayerBountyDuration";

    // popular front settings
    public final static String DISABLE_INTERSTELLAIRE_UPGRADES = "vayramerged_disableInterstellaireUpgrades";
    public final static String COMMUNIST_CLOUDS_FP_MULTIPLIER = "vayramerged_popularFrontExpeditionMultiplier";
    public final static String POPULAR_FRONT_ENABLED = "vayramerged_popularFrontEnabled";
    public final static String POPULAR_FRONT_START_CYCLE = "vayramerged_popularFrontStartCycle";
    public final static String POPULAR_FRONT_TIMER_INTERVAL_MIN = "vayramerged_popularFrontTimerIntervalMin";
    public final static String POPULAR_FRONT_TIMER_INTERVAL_MAX = "vayramerged_popularFrontTimerIntervalMax";

    // colonial factions
    public final static String COLONIAL_COMPETITORS_ENABLED = "vayramerged_enableColonialCompetitors";
    public final static String COLONIAL_COMPETITORS_START_CYCLE = "vayramerged_colonialCompetitorsStartCycle";
    public final static String COLONIAL_COMPETITORS_COLONY_MULT = "vayramerged_colonialCompetitorsColonyMult";
    public final static String COLONIAL_COMPETITOR_FACTION_COLONY_MAX = "vayramerged_colonialCompetitorFactionColonyMax";
    public final static String COLONIAL_COMPETITOR_TIMER_INTERVAL_MIN = "vayramerged_colonialCompetitorTimerIntervalMin";
    public final static String COLONIAL_COMPETITOR_TIMER_INTERVAL_MAX = "vayramerged_colonialCompetitorTimerIntervalMax";
    public final static String COLONIAL_COMPETITOR_CHANCE = "vayramerged_colonialCompetitorChance";
    public final static String COLONIAL_COMPETITOR_UPGRADE_INTERVAL_MIN = "vayramerged_colonialCompetitorUpgradeIntervalMin";
    public final static String COLONIAL_COMPETITOR_UPGRADE_INTERVAL_MAX = "vayramerged_colonialCompetitorUpgradeIntervalMax";
    public final static String COLONIAL_COMPETITOR_BASE_FLEET_POINTS = "vayramerged_colonialCompetitorBaseFleetPoints";
    public final static String COLONIAL_COMPETITOR_AI_REBELLION_THRESHOLD = "vayramerged_colonialCompetitorAIRebellionThreshold";

    // dmod settings
    public final static String DISABLE_DAMAGED_AMMO_FOR_PLAYER = "vayramerged_disableDamagedAmmo_player";
    public final static String DISABLE_DAMAGED_AMMO_FOR_ENEMY = "vayramerged_disableDamagedAmmo_enemy";
    public final static String DAMAGED_AMMO_EFFECT = "vayramerged_damagedAmmo_effect1";

    public final static String DISABLE_DAMAGED_AUTOMATION_FOR_PLAYER = "vayramerged_disableDamagedAutomation_player";
    public final static String DISABLE_DAMAGED_AUTOMATION_FOR_ENEMY = "vayramerged_disableDamagedAutomation_enemy";
    public final static String DAMAGED_AUTOMATION_EFFECT1 = "vayramerged_damagedAutomation_effect1";
    public final static String DAMAGED_AUTOMATION_EFFECT2 = "vayramerged_damagedAutomation_effect2";

    public final static String DISABLE_DAMAGED_BALLISTICS_FOR_PLAYER = "vayramerged_disableDamagedBallistics_player";
    public final static String DISABLE_DAMAGED_BALLISTICS_FOR_ENEMY = "vayramerged_disableDamagedBallistics_enemy";
    public final static String DAMAGED_BALLISTICS_EFFECT1 = "vayramerged_damagedBallistics_effect1";
    public final static String DAMAGED_BALLISTICS_EFFECT2 = "vayramerged_damagedBallistics_effect2";

    public final static String DISABLE_DAMAGED_ENVIRONMENT_FOR_PLAYER = "vayramerged_disableDamagedEnvironment_player";
    public final static String DISABLE_DAMAGED_ENVIRONMENT_FOR_ENEMY = "vayramerged_disableDamagedEnvironment_enemy";
    public final static String DAMAGED_ENVIRONMENT_EFFECT1 = "vayramerged_damagedEnvironment_effect1";
    public final static String DAMAGED_ENVIRONMENT_EFFECT2 = "vayramerged_damagedEnvironment_effect2";

    public final static String DISABLE_DAMAGED_EVERYTHING_FOR_PLAYER = "vayramerged_disableDamagedEverything_player";
    public final static String DISABLE_DAMAGED_EVERYTHING_FOR_ENEMY = "vayramerged_disableDamagedEverything_enemy";
    public final static String DAMAGED_EVERYTHING_EFFECT1 = "vayramerged_damagedEverything_effect1";
    public final static String DAMAGED_EVERYTHING_EFFECT2 = "vayramerged_damagedEverything_effect2";
    public final static String DAMAGED_EVERYTHING_EFFECT3 = "vayramerged_damagedEverything_effect3";

    public final static String DISABLE_DAMAGED_FIGHTER_WEAPONS_FOR_PLAYER = "vayramerged_disableDamagedFighterWeapons_player";
    public final static String DISABLE_DAMAGED_FIGHTER_WEAPONS_FOR_ENEMY = "vayramerged_disableDamagedFighterWeapons_enemy";
    public final static String DAMAGED_FIGHTER_WEAPONS_EFFECT1 = "vayramerged_damagedFighterWeapons_effect1";
    public final static String DAMAGED_FIGHTER_WEAPONS_EFFECT2 = "vayramerged_damagedFighterWeapons_effect2";
    public final static String DAMAGED_FIGHTER_WEAPONS_EFFECT3 = "vayramerged_damagedFighterWeapons_effect3";
    public final static String DAMAGED_FIGHTER_WEAPONS_EFFECT4 = "vayramerged_damagedFighterWeapons_effect4";
    public final static String DAMAGED_FIGHTER_WEAPONS_EFFECT5 = "vayramerged_damagedFighterWeapons_effect5";


    public final static String DISABLE_DAMAGED_GUNNERY_FOR_PLAYER = "vayramerged_disableDamagedGunnery_player";
    public final static String DISABLE_DAMAGED_GUNNERY_FOR_ENEMY = "vayramerged_disableDamagedGunnery_enemy";
    public final static String DAMAGED_GUNNERY_EFFECT1 = "vayramerged_damagedGunnery_effect1";
    public final static String DAMAGED_GUNNERY_EFFECT2 = "vayramerged_damagedGunnery_effect2";
    public final static String DAMAGED_GUNNERY_EFFECT3 = "vayramerged_damagedGunnery_effect3";

    public final static String DISABLE_DAMAGED_LIFE_SUPPORT_FOR_PLAYER = "vayramerged_disableDamagedLifeSupport_player";
    public final static String DISABLE_DAMAGED_LIFE_SUPPORT_FOR_ENEMY = "vayramerged_disableDamagedLifeSupport_enemy";
    public final static String DAMAGED_LIFE_SUPPORT_EFFECT1 = "vayramerged_damagedLifeSupport_effect1";
    public final static String DAMAGED_LIFE_SUPPORT_EFFECT2 = "vayramerged_damagedLifeSupport_effect2";

    public final static String DISABLE_DAMAGED_MISSILES_FOR_PLAYER = "vayramerged_disableDamagedMissiles_player";
    public final static String DISABLE_DAMAGED_MISSILES_FOR_ENEMY = "vayramerged_disableDamagedMissiles_enemy";
    public final static String DAMAGED_MISSILES_EFFECT1 = "vayramerged_damagedMissiles_effect1";
    public final static String DAMAGED_MISSILES_EFFECT2 = "vayramerged_damagedMissiles_effect2";
    public final static String DAMAGED_MISSILES_EFFECT3 = "vayramerged_damagedMissiles_effect3";

    public final static String DISABLE_DAMAGED_OPTICS_FOR_PLAYER = "vayramerged_disableDamagedOptics_player";
    public final static String DISABLE_DAMAGED_OPTICS_FOR_ENEMY = "vayramerged_disableDamagedOptics_enemy";
    public final static String DAMAGED_OPTICS_EFFECT1 = "vayramerged_damagedOptics_effect1";
    public final static String DAMAGED_OPTICS_EFFECT2 = "vayramerged_damagedOptics_effect2";

    public final static String DISABLE_DAMAGED_SHIELDS_FOR_PLAYER = "vayramerged_disableDamagedShields_player";
    public final static String DISABLE_DAMAGED_SHIELDS_FOR_ENEMY = "vayramerged_disableDamagedShields_enemy";
    public final static String DAMAGED_SHIELDS_EFFECT1 = "vayramerged_damagedShields_effect1";
    public final static String DAMAGED_SHIELDS_EFFECT2 = "vayramerged_damagedShields_effect2";

    public final static String DISABLE_DAMAGED_TURRETS_FOR_PLAYER = "vayramerged_disableDamagedTurrets_player";
    public final static String DISABLE_DAMAGED_TURRETS_FOR_ENEMY = "vayramerged_disableDamagedTurrets_enemy";
    public final static String DAMAGED_TURRETS_EFFECT1 = "vayramerged_damagedTurrets_effect1";
    public final static String DAMAGED_TURRETS_EFFECT2 = "vayramerged_damagedTurrets_effect2";
    public final static String DAMAGED_TURRETS_EFFECT3 = "vayramerged_damagedTurrets_effect3";

    public final static String DISABLE_LESS_ILL_ADVISED_FOR_PLAYER = "vayramerged_disableLessIllAdvised_player";
    public final static String DISABLE_LESS_ILL_ADVISED_FOR_ENEMY = "vayramerged_disableLessIllAdvised_enemy";
    public final static String LESS_ILL_ADVISED_EFFECT1 = "vayramerged_lessIllAdvised_effect1";
    public final static String LESS_ILL_ADVISED_EFFECT2 = "vayramerged_lessIllAdvised_effect2";
    public final static String LESS_ILL_ADVISED_EFFECT3 = "vayramerged_lessIllAdvised_effect3";
    public final static String LESS_ILL_ADVISED_EFFECT4 = "vayramerged_lessIllAdvised_effect4";
    public final static String LESS_ILL_ADVISED_EFFECT5 = "vayramerged_lessIllAdvised_effect5";
}