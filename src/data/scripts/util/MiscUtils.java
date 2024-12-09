package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.sun.javafx.beans.annotations.NonNull;
import data.scripts.VayraMergedModPlugin;
import data.util.LoggerLogLevel;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.console.Console;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Bunch of small copypastable methods belonging everywhere but nowhere in specific
 */
public class MiscUtils {

    public static final int OWNER_ENEMY = 1;
    public static final int OWNER_PLAYER = Misc.OWNER_PLAYER;
    public static final int OWNER_NEUTRAL = Misc.OWNER_NEUTRAL;
    public static final String HULLMOD_RUGGED_CONSTRUCTION = "rugged";

    public static final Random random = new Random();

    /**
     * The annonymous logger instance
     */
    public static Logger annonymousLogger = Global.getLogger(MiscUtils.class);

    /**
     * Returns the maximum size of {@link WeaponAPI} angle offsets by looking at it's:
     * {@link WeaponSpecAPI#getTurretAngleOffsets()},
     * {@link WeaponSpecAPI#getHardpointAngleOffsets()}
     * {@link WeaponSpecAPI#getHiddenAngleOffsets()}
     *
     * @param weapon the weapon to lookup
     * @return the max size of these three things
     */
    public static int getMaximumWeaponSpecAngleOffsetsSize(WeaponAPI weapon) {
        int size = 0;
        size = Math.max(size, weapon.getSpec().getTurretAngleOffsets().size());
        size = Math.max(size, weapon.getSpec().getHardpointAngleOffsets().size());
        size = Math.max(size, weapon.getSpec().getHiddenAngleOffsets().size());

        return size;
    }

    public static String getClockTime(boolean showMilliseconds) {
        long currentMillis = System.currentTimeMillis();
        Date currentTime = new Date(currentMillis);

        String timeFormat = showMilliseconds ? "HH:mm:ss.SSS" : "HH:mm:ss";
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
        return timeFormatter.format(currentTime);
    }

    public static String stringifyFleetParams(FleetParamsV3 params) {
        return "FleetParamsV3{" +
                "source=" + params.source +
                ", locInHyper=" + params.locInHyper +
                ", quality=" + params.quality +
                ", factionId='" + params.factionId + '\'' +
                ", fleetType='" + params.fleetType + '\'' +
                ", combatPts=" + params.combatPts +
                ", freighterPts=" + params.freighterPts +
                ", tankerPts=" + params.tankerPts +
                ", transportPts=" + params.transportPts +
                ", linerPts=" + params.linerPts +
                ", utilityPts=" + params.utilityPts +
                ", maxShipSize=" + params.maxShipSize +
                ", minShipSize=" + params.minShipSize +
                ", qualityMod=" + params.qualityMod +
                ", qualityOverride=" + params.qualityOverride +
                ", averageSMods=" + params.averageSMods +
                ", withOfficers=" + params.withOfficers +
                ", ignoreMarketFleetSizeMult=" + params.ignoreMarketFleetSizeMult +
                ", onlyApplyFleetSizeToCombatShips=" + params.onlyApplyFleetSizeToCombatShips +
                ", doNotPrune=" + params.doNotPrune +
                ", doNotAddShipsBeforePruning=" + params.doNotAddShipsBeforePruning +
                ", modeOverride=" + params.modeOverride +
                ", officerLevelBonus=" + params.officerLevelBonus +
                ", officerNumberBonus=" + params.officerNumberBonus +
                ", maxOfficersToAdd=" + params.maxOfficersToAdd +
                ", officerNumberMult=" + params.officerNumberMult +
                ", officerLevelLimit=" + params.officerLevelLimit +
                ", commanderLevelLimit=" + params.commanderLevelLimit +
                ", random=" + params.random +
                ", commander=" + params.commander +
                ", noCommanderSkills=" + params.noCommanderSkills +
                ", forceAllowPhaseShipsEtc=" + params.forceAllowPhaseShipsEtc +
                ", treatCombatFreighterSettingAsFraction=" + params.treatCombatFreighterSettingAsFraction +
                ", doctrineOverride=" + params.doctrineOverride +
                ", timestamp=" + params.timestamp +
                ", maxNumShips=" + params.maxNumShips +
                ", onlyRetainFlagship=" + params.onlyRetainFlagship +
                ", flagshipVariantId='" + params.flagshipVariantId + '\'' +
                ", flagshipVariant=" + params.flagshipVariant +
                ", aiCores=" + params.aiCores +
                ", doNotIntegrateAICores=" + params.doNotIntegrateAICores +
                ", mode=" + params.mode +
                ", banPhaseShipsEtc=" + params.banPhaseShipsEtc +
                ", blockFallback=" + params.blockFallback +
                ", allWeapons=" + params.allWeapons +
                ", addShips=" + params.addShips +
                '}';

    }

    /**
     * Logs a message, using the passed-in {@link Logger} <i>log</i>
     *
     * @param logLevel the {@link LoggerLogLevel} to use
     * @param log the {@link Logger} to log with
     * @param logMessage the message to log
     * @param alsoLogToConsole whether the message should also be printed in the console or not
     */
    public static void log(@NonNull LoggerLogLevel logLevel, @NonNull Logger log, String logMessage, boolean alsoLogToConsole) {
        switch (logLevel) {
            case FATAL: {
                log.fatal(logMessage);
                if (alsoLogToConsole) Console.showMessage("[FATAL] " + logMessage);
            }
            break;

            case ERROR: {
                log.error(logMessage);
                if (alsoLogToConsole) Console.showMessage("[ERROR] " + logMessage);
            }
            break;

            case WARN: {
                log.warn(logMessage);
                if (alsoLogToConsole) Console.showMessage("[WARN] " + logMessage);
            }
            break;

            case DEBUG: {
                log.debug(logMessage);
                if (alsoLogToConsole) Console.showMessage("[DEBUG] " + logMessage);
            }
            break;

            case INFO: {
                log.info(logMessage);
                if (alsoLogToConsole) Console.showMessage("[INFO] " + logMessage);
            }
            break;

            case NONE: {
                // it does nothing but occupying ordinal 0
            }
            break;
        }
    }

    /**
     * Logs a message, using the annonymous logger {@link #annonymousLogger}
     *
     * @param logLevel the {@link LoggerLogLevel} to use
     * @param logTag the logtag to use to separate the log from others
     * @param logMessage the message to log
     * @param alsoLogToConsole whether the message should also be printed in the console or not
     */
    public static void log(@NonNull LoggerLogLevel logLevel, String logTag, String logMessage, boolean alsoLogToConsole) {
        String logtaggedMessage = String.format("%s %s", logTag, logMessage);
        switch (logLevel) {
            case FATAL: {
                annonymousLogger.fatal(logtaggedMessage);
                if (alsoLogToConsole) Console.showMessage("[FATAL] " + logtaggedMessage);
            }
            break;

            case ERROR: {
                annonymousLogger.error(logtaggedMessage);
                if (alsoLogToConsole) Console.showMessage("[ERROR] " + logtaggedMessage);
            }
            break;

            case WARN: {
                annonymousLogger.warn(logtaggedMessage);
                if (alsoLogToConsole) Console.showMessage("[WARN] " + logtaggedMessage);
            }
            break;

            case DEBUG: {
                annonymousLogger.debug(logtaggedMessage);
                if (alsoLogToConsole) Console.showMessage("[DEBUG] " + logtaggedMessage);
            }
            break;

            case INFO: {
                annonymousLogger.info(logtaggedMessage);
                if (alsoLogToConsole) Console.showMessage("[INFO] " + logtaggedMessage);
            }
            break;

            case NONE: {
                // it does nothing but occupying ordinal 0
            }
            break;
        }
    }

    /**
     * Just a shortcut for {@link #log(LoggerLogLevel, String, String, boolean)} with <i>alsoLogToConsole</i> set to false
     *
     * @see #log(LoggerLogLevel, String, String, boolean)
     */
    public static void log(LoggerLogLevel logLevel, String logTag, String logMessage) {
        log(logLevel, logTag, logMessage, false);
    }

    /**
     * Just a shortcut for {@link #log(LoggerLogLevel, Logger, String, boolean)} with <i>alsoLogToConsole</i> set to false
     *
     * @see #log(LoggerLogLevel, Logger, String, boolean)
     */
    public static void log(@NonNull LoggerLogLevel logLevel, @NonNull Logger log, String logMessage) {
        log(logLevel, log, logMessage, false);
    }

    /**
     * Method that loads a {@link Boolean} property from {@link LunaSettings}, using the {@link VayraMergedModPlugin#MOD_ID}
     * and returning the {@code defaultValue} if no object was found, otherwise it returns whatever was loaded from LunaSettings
     *
     * @param settingName the setting name to load
     * @param defaultValue the default value to use
     * @return read value, or default value if we read {@code null}
     */
    public static boolean loadBooleanLunaSetting(String settingName, boolean defaultValue) {
        boolean retVal;
        Boolean object = LunaSettings.getBoolean(VayraMergedModPlugin.MOD_ID, settingName);
        if (object == null) {
            retVal = defaultValue;
        } else {
            retVal = object;
        }
        return retVal;
    }

    /**
     * Checks whether the ship variant has a hullmod installed on it (built-in or not)
     *
     * @param variant the variant to query for the hullmod
     * @param hullModId the hull mod's ID to check for
     * @return whether the hullmod is installed
     */
    public static boolean hasHullmod(ShipVariantAPI variant, String hullModId) {
        return variant.hasHullMod(hullModId);
    }

    /**
     * Checks whether the ship variant has a hullmod built into it (or rather, whether the hullmod is installed as "built-in")
     *
     * NOTE: Should be used for checking hullmods that typically come with the ship from the start, for hullmods that the
     * player has built-in use [hasSModdedBuiltInHullmod]
     *
     * @param variant the variant to query for the hullmod
     * @param hullModId the hull mod's ID to check for
     * @return whether the hullmod is installed
     */
    public static boolean hasBuiltInHullmod(ShipVariantAPI variant, String hullModId) {
        return variant.getHullSpec().getBuiltInMods().contains(hullModId);
    }

    /**
     * Checks whether the ship variant has a hullmod S-modded / built into it (or rather, whether the hullmod is installed as "built-in" in refit screen)
     *
     * @param variant the variant to query for the hullmod
     * @param hullModId the hull mod's ID to check for
     * @return whether the hullmod is installed
     * @see Misc#getCurrSpecialModsList(ShipVariantAPI)
     */
    public static boolean hasSModdedBuiltInHullmod(ShipVariantAPI variant, String hullModId) {
//        return Misc.getCurrSpecialModsList(this.variant).map { hullmods -> hullmods.id }.containsIgnoreCase(hullModId)
        boolean retVal = false;
        List<HullModSpecAPI> hullModSpecAPIList = Misc.getCurrSpecialModsList(variant);
        // remap to hullmod IDs
        for (HullModSpecAPI hullModSpec : hullModSpecAPIList) {
            if(hullModSpec.getId().contains(hullModId)) {
                retVal = true;
                break;
            }
        }

        return retVal;
    }

    /**
     * Checks whether the ship variant has *any* version of the hullmod in it
     *
     * @param variant the variant to query
     * @param hullModId  the hull mod's ID to look for
     * @return whether the hullmod with the given ID is installed, built-in or S-modded
     * @see #hasHullmod(ShipVariantAPI, String)
     * @see #hasBuiltInHullmod(ShipVariantAPI, String)
     * @see #hasSModdedBuiltInHullmod(ShipVariantAPI, String)
     */
    public static boolean hasHullmodAny(ShipVariantAPI variant, String hullModId) {
        return hasHullmod(variant, hullModId) || hasBuiltInHullmod(variant, hullModId) || hasSModdedBuiltInHullmod(variant, hullModId);
    }

    /**
     * Convenience method to check whether a variant has the "Rugged Construction" {@link #HULLMOD_RUGGED_CONSTRUCTION} hullmod
     * @param variant the variant to query
     * @return whether the variant contains the "rugged" hullmod or not
     */
    public static boolean hasRuggedConstructionHullmod(ShipVariantAPI variant) {
        return hasHullmodAny(variant, HULLMOD_RUGGED_CONSTRUCTION);
    }

    /**
     * Convenience method to check whether a variant has the "Rugged Construction" {@link #HULLMOD_RUGGED_CONSTRUCTION} hullmod
     * @param stats the stats whose variant we should query
     * @return whether the variant contains the "rugged" hullmod or not
     */
    public static boolean hasRuggedConstructionHullmod(MutableShipStatsAPI stats) {
        return hasHullmodAny(stats.getVariant(), HULLMOD_RUGGED_CONSTRUCTION);
    }

    /**
     * Generates a random integer between 0 and {@code range}
     * @param range number that has to be greater than zero
     * @return a random integer in the range
     * @see Random#nextInt(int)
     */
    public static int generateRandomInt(int range) {
        return random.nextInt(range);
    }

    /**
     * Ensures that the {@code actualValue} value can't go below {@code minValue} value
     * @param actualValue the actual value to clamp
     * @param minValue the clamping cut-off value
     * @return the clamped value if it was below the cutoff or itself if it wasn't
     */
    public static float clamp(float actualValue, float minValue) {
        return Math.min(actualValue, Math.max(actualValue, minValue));
    }

    /**
     * Method that tries getting various Stats from the ship, and finally falls back to PlayerStats in case all of these fail
     *
     * It will try getting stats in this order:
     * - ship.getFleetCommander().getFleetCommanderStats()
     * - ship.getFleetCommander().getStats();
     * - ship.getCaptain().getFleetCommanderStats();
     * - ship.getCaptain().getStats();
     * - ship.getFleetMember().getFleetCommanderForStats().getFleetCommanderStats()
     * - ship.getFleetMember().getFleetCommanderForStats().getStats()
     * - Global.getSector().getPlayerStats();
     *
     * @param ship the ship from which to get stats
     * @return a non-null instance of stats
     */
    public static @NonNull MutableCharacterStatsAPI getNonNullStats(ShipAPI ship) {
        MutableCharacterStatsAPI retVal = null;

        MutableCharacterStatsAPI fallback1 = null;
        MutableCharacterStatsAPI fallback2 = null;
        MutableCharacterStatsAPI fallback3 = null;
        MutableCharacterStatsAPI fallback4 = null;

        if (ship.getFleetCommander() != null) {
            fallback1 = ship.getFleetCommander().getFleetCommanderStats();
            fallback2 = ship.getFleetCommander().getStats();
        }

        if (fallback1 != null) retVal = fallback1;
        if (retVal != null) return retVal;

        if (fallback2 != null) retVal = fallback2;
        if (retVal != null) return retVal;

        if (ship.getCaptain() != null) {
            fallback3 = ship.getCaptain().getFleetCommanderStats();
            fallback4 = ship.getCaptain().getStats();
        }

        if (fallback3 != null) retVal = fallback3;
        if (retVal != null) return retVal;

        if (fallback4 != null) retVal = fallback4;
        if (retVal != null) return retVal;

        // Finally, if all of these failed, then fuck it and revert to using PlayerStats
        // if ship was being owned by player - return player stats, otherwise create new stats
        if (ship.getOwner() == OWNER_PLAYER) {
            return Global.getSector().getPlayerStats();
        } else {
            FleetMemberAPI member = ship.getFleetMember();
            if (member != null) {
                MutableCharacterStatsAPI fallback = getStats(member);
                if (fallback != null) return fallback;
            }

            // Fuck it again, return player stats
            return Global.getSector().getPlayerStats();
        }
    }

    public static @Nullable MutableCharacterStatsAPI getStats(FleetMemberAPI member) {
        PersonAPI fleetMemberForStats = member.getFleetCommanderForStats();
        // We don't have to check this 'fleetMemberForStats' for being null, because it will either return an
        // existing non-null instance of the fleetmember's getFleetCommanderForStats or create a new one and set it
        MutableCharacterStatsAPI fallback5 = fleetMemberForStats.getFleetCommanderStats();
        MutableCharacterStatsAPI fallback6 = fleetMemberForStats.getStats();
        if (fallback5 != null) return fallback5;
        if (fallback6 != null) return fallback6;

        // If the previous two didn't return, we can't really extract anything more from this member so return null
        return null;
    }
}
