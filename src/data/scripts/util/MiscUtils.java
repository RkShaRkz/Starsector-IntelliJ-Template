package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.sun.javafx.beans.annotations.NonNull;
import data.util.LoggerLogLevel;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Bunch of small copypastable methods belonging everywhere but nowhere in specific
 */
public class MiscUtils {

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
}
