package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedTurrets extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedTurrets";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_WEAPON_SPEED_PENALTY = 0.15f;
    public static float WEAPON_SPEED_PENALTY = DEFAULT_WEAPON_SPEED_PENALTY;

    public static final float DEFAULT_WEAPON_HP_PENALTY = 0.15f;
    public static float WEAPON_HP_PENALTY = DEFAULT_WEAPON_HP_PENALTY;

    public static final float DEFAULT_WEAPON_MALFUNCTION_PENALTY = 0.05f;
    public static float WEAPON_MALFUNCTION_PENALTY = DEFAULT_WEAPON_MALFUNCTION_PENALTY;

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
        float speedMult = calculateWeaponSpeedMultiplier(effect);
        float hpMult = calculateWeaponHPMultiplier(effect);
        float malfunctionMult = calculateWeaponMalfunctionMalus(effect);

        stats.getWeaponTurnRateBonus().modifyMult(id, speedMult);
        stats.getWeaponHealthBonus().modifyMult(id, hpMult);
        stats.getWeaponMalfunctionChance().modifyFlat(id, malfunctionMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float speedMalus = calculateWeaponSpeedMalus(effect);
        float hpMalus = calculateWeaponHPMalus(effect);
        float malfunctionMalus = calculateWeaponMalfunctionMalus(effect);

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

    private float calculateWeaponSpeedMultiplier(float baseEffect) {
        // Basically, speed multiplier is going to be 1 - SpeedMalus
        float retVal;
        float malus = calculateWeaponSpeedMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateWeaponSpeedMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_SPEED_PENALTY - WEAPON_SPEED_PENALTY * penaltyFactor;
        /*
         * will produce 0.15 for 100% dmod effect mult
         * 0.15 - 0.15 * (0) = 0.15
         * will produce 0.075 for 50% dmod effect mult
         * 0.15 - 0.15*0.5 = 0.075
         * will produce 0.3 for 200%
         * 0.15 - 0.15*(-1) = 0.3
         * will produce 0.45 for 300%
         * 0.15 - 0.15*-2 = 0.45
         */

        return retVal;
    }

    private float calculateWeaponHPMultiplier(float baseEffect) {
        // Basically, HP multiplier is going to be 1 - WeaponHPMalus
        float retVal;
        float malus = calculateWeaponHPMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateWeaponHPMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_HP_PENALTY - WEAPON_HP_PENALTY * penaltyFactor;
        /*
         * will produce 0.15 for 100% dmod effect mult
         * 0.1 - 0.15 * (0) = 0.15
         * will produce 0.075 for 50% dmod effect mult
         * 0.15 - 0.15*0.5 = 0.075
         * will produce 0.3 for 200%
         * 0.15 - 0.15*(-1) = 0.3
         * will produce 0.45 for 300%
         * 0.15 - 0.15*-2 = 0.45
         */

        return retVal;
    }

    private float calculateWeaponMalfunctionMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = WEAPON_MALFUNCTION_PENALTY - WEAPON_MALFUNCTION_PENALTY * penaltyFactor;
        /*
         * will produce 0.05 for 100% dmod effect mult
         * 0.05 - 0.05 * (0) = 0.05
         * will produce 0.025 for 50% dmod effect mult
         * 0.05 - 0.05*0.5 = 0.025
         * will produce 0.1 for 200%
         * 0.05 - 0.05*(-1) = 0.1
         * will produce 0.15 for 300%
         * 0.05 - 0.05*-2 = 0.15
         */

        return retVal;
    }
}
