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

public class VayraLessIllAdvised extends BaseHullMod {
    private static final String LOGTAG = "VayraLessIllAdvised";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    private static final float WEAPON_MALFUNCTION_PENALTY = 0.05f;
    private static final float WEAPON_TURN_RATE_BONUS = 0.1f;
    private static final float WEAPON_RATE_OF_FIRE_BONUS = 0.1f;

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
        float malfunctionChance = calculateWeaponMalfunctionMalus(stats.getVariant(), effect);
        float weaponTurnRateBonusMult = calculateTurnRateMultiplier(stats.getVariant(), effect);
        float weaponRateOfFireMult = calculateRoFMultiplier(stats.getVariant(), effect);

        stats.getWeaponMalfunctionChance().modifyFlat(id, malfunctionChance);
        stats.getWeaponTurnRateBonus().modifyMult(id, weaponTurnRateBonusMult);
        stats.getBallisticRoFMult().modifyMult(id, weaponRateOfFireMult);
        stats.getEnergyRoFMult().modifyMult(id, weaponRateOfFireMult);
        stats.getMissileRoFMult().modifyMult(id, weaponRateOfFireMult);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        ShipVariantAPI variant = null;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            variant = ship.getVariant();
        }
        float malfunctionChance = calculateWeaponMalfunctionMalus(variant, effect);
        float weaponTurnRateBonus = calculateTurnRateBonus(variant, effect);
        float weaponRateOfFireBonus = calculateRoFBonus(variant, effect);

        if (index == 0) {
            return Math.round(weaponTurnRateBonus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(weaponRateOfFireBonus * 100f) + "%";
        }
        if (index >= 2) {
            return Math.round(malfunctionChance * 100f) + "%";
        }
        return null;
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

    private float calculateTurnRateMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, turn rate multiplier is going to be 1 + TurnRateBonus
        float retVal;
        float bonus = calculateTurnRateBonus(variant, baseEffect);
        retVal = 1f + bonus;

        return retVal;
    }

    private float calculateTurnRateBonus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_TURN_RATE_BONUS - WEAPON_TURN_RATE_BONUS * penaltyFactor;
        /**
         * will produce 0.1 for 100% dmod effect mult
         * 0.1 - 0.1 * (0) = 0.1
         * will produce 0.05 for 50% dmod effect mult
         * 0.1 - 0.1*0.5 = 0.05
         * will produce 0.2 for 200%
         * 0.1 - 0.1*(-1) = 0.2
         * will produce 0.3 for 300%
         * 0.11 - 0.1*-2 = 0.3
         *
         * and then DOUBLE of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal * 2;
            }
        }

        return retVal;
    }

    private float calculateRoFMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, RoF multiplier is going to be 1 + RoFBonus
        float retVal;
        float bonus = calculateRoFBonus(variant, baseEffect);
        retVal = 1f + bonus;

        return retVal;
    }

    private float calculateRoFBonus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_RATE_OF_FIRE_BONUS - WEAPON_RATE_OF_FIRE_BONUS * penaltyFactor;
        /**
         * will produce 0.1 for 100% dmod effect mult
         * 0.1 - 0.1 * (0) = 0.1
         * will produce 0.05 for 50% dmod effect mult
         * 0.1 - 0.1*0.5 = 0.05
         * will produce 0.2 for 200%
         * 0.1 - 0.1*(-1) = 0.2
         * will produce 0.3 for 300%
         * 0.11 - 0.1*-2 = 0.3
         *
         * and then DOUBLE of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal * 2;
            }
        }

        return retVal;
    }
}
