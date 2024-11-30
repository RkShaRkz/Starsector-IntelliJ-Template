package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedTurrets extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedTurrets";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float WEAPON_SPEED_PENALTY = 0.15f;
    public static final float WEAPON_HP_PENALTY = 0.15f;
    public static final float WEAPON_MALFUNCTION_PENALTY = 0.05f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Do nothing if it's been disabled
        if (stats.getFleetMember() != null) {
            int owner = stats.getFleetMember().getOwner();
            if (DISABLE_FOR_ENEMY && MiscUtils.OWNER_ENEMY == owner) return;
            if (DISABLE_FOR_PLAYER && MiscUtils.OWNER_PLAYER == owner) return;
        } else {
            MiscUtils.log(LoggerLogLevel.WARN, LOGTAG, "Encountered a ship with NULL fleetmember in 'applyEffectsBeforeShipCreation' so settings to disable it won't work");
        }

        // Carry on as usual
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float speedMult = calculateWeaponSpeedMultiplier(stats.getVariant(), effect);
        float hpMult = calculateWeaponHPMultiplier(stats.getVariant(), effect);
        float malfunctionMult = calculateWeaponMalfunctionMalus(stats.getVariant(), effect);

        stats.getWeaponTurnRateBonus().modifyMult(id, speedMult);
        stats.getWeaponHealthBonus().modifyMult(id, hpMult);
        stats.getWeaponMalfunctionChance().modifyFlat(id, malfunctionMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        ShipVariantAPI variant = null;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            variant = ship.getVariant();
        }
        float speedMalus = calculateWeaponSpeedMalus(variant, effect);
        float hpMalus = calculateWeaponHPMalus(variant, effect);
        float malfunctionMalus = calculateWeaponMalfunctionMalus(variant, effect);

        if (index == 0) {
            return Math.round(speedMalus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(hpMalus * 100f) + "%";
        }
        if (index == 2) {
            return Math.round(malfunctionMalus * 100f) + "%";
        }
        if (index >= 3) {
            return CompromisedStructure.getCostDescParam(index, 3);
        }

        return null;
    }

    private float calculateWeaponSpeedMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, speed multiplier is going to be 1 - SpeedMalus
        float retVal;
        float malus = calculateWeaponSpeedMalus(variant, baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateWeaponSpeedMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_SPEED_PENALTY - WEAPON_SPEED_PENALTY * penaltyFactor;
        /**
         * will produce 0.15 for 100% dmod effect mult
         * 0.15 - 0.15 * (0) = 0.15
         * will produce 0.075 for 50% dmod effect mult
         * 0.15 - 0.15*0.5 = 0.075
         * will produce 0.3 for 200%
         * 0.15 - 0.15*(-1) = 0.3
         * will produce 0.45 for 300%
         * 0.15 - 0.15*-2 = 0.45
         *
         * and then half of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }

    private float calculateWeaponHPMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, HP multiplier is going to be 1 - HMMalus
        float retVal;
        float malus = calculateWeaponHPMalus(variant, baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateWeaponHPMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_HP_PENALTY - WEAPON_HP_PENALTY * penaltyFactor;
        /**
         * will produce 0.15 for 100% dmod effect mult
         * 0.1 - 0.15 * (0) = 0.15
         * will produce 0.075 for 50% dmod effect mult
         * 0.15 - 0.15*0.5 = 0.075
         * will produce 0.3 for 200%
         * 0.15 - 0.15*(-1) = 0.3
         * will produce 0.45 for 300%
         * 0.15 - 0.15*-2 = 0.45
         *
         * and then half of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }

    private float calculateWeaponMalfunctionMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_MALFUNCTION_PENALTY - WEAPON_MALFUNCTION_PENALTY * penaltyFactor;
        /**
         * will produce 0.05 for 100% dmod effect mult
         * 0.05 - 0.05 * (0) = 0.05
         * will produce 0.025 for 50% dmod effect mult
         * 0.05 - 0.05*0.5 = 0.025
         * will produce 0.1 for 200%
         * 0.05 - 0.05*(-1) = 0.1
         * will produce 0.15 for 300%
         * 0.05 - 0.05*-2 = 0.15
         *
         * and then half of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }
}
