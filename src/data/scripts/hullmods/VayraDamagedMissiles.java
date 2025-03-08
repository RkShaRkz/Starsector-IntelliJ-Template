package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedMissiles extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedMissiles";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_MISSILE_AMMO_PENALTY = 0.3f;
    public static float MISSILE_AMMO_PENALTY = DEFAULT_MISSILE_AMMO_PENALTY;

    public static final float DEFAULT_MISSILE_ROF_PENALTY = 0.25f;
    public static float MISSILE_ROF_PENALTY = DEFAULT_MISSILE_ROF_PENALTY;

    public static final float DEFAULT_MISSILE_GUIDANCE_PENALTY = 0.2f;
    public static float MISSILE_GUIDANCE_PENALTY = DEFAULT_MISSILE_GUIDANCE_PENALTY;

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
        float ammoMult = calculateAmmoPenaltyMultiplier(effect);
        float fireRateMult = calculateMissileRoFMultiplier(effect);
        float guidanceMult = calculateGuidanceMultiplier(effect);

        // Lets make this one a bit more "fun".
        // missiles get less ammo
        // missiles shoot faster
        // missiles track like shit
        stats.getMissileAmmoBonus().modifyMult(id, ammoMult);
        stats.getMissileRoFMult().modifyMult(id, fireRateMult);
        stats.getMissileGuidance().modifyMult(id, guidanceMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float ammoMalus = calculateAmmoPenaltyMalus(effect);
        float rofMalus = calculateMissileRoFMalus(effect);
        float guidanceMalus = calculateGuidanceMalus(effect);

        if (index == 0) {
            return Math.round(ammoMalus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(rofMalus * 100f) + "%";
        }
        if (index == 2) {
            return Math.round(guidanceMalus * 100f) + "%";
        }
        if (index >= 3) {
            return CompromisedStructure.getCostDescParam(index, 3);
        }
        return null;
    }

    private float calculateAmmoPenaltyMultiplier(float baseEffect) {
        // Basically, ammo multiplier is going to be 1 - AmmoMalus
        float retVal;
        float malus = calculateAmmoPenaltyMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateAmmoPenaltyMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = MISSILE_AMMO_PENALTY - MISSILE_AMMO_PENALTY * penaltyFactor;
        /**
         * will produce 0.3 for 100% dmod effect mult
         * 0.3 - 0.3 * (0) = 0.3
         * will produce 0.15 for 50% dmod effect mult
         * 0.3 - 0.3*0.5 = 0.15
         * will produce 0.4 for 200%
         * 0.3 - 0.3*(-1) = 0.6
         * will produce 0.9 for 300%
         * 0.3 - 0.3*-2 = 0.9
         */

        return retVal;
    }

    private float calculateMissileRoFMultiplier(float baseEffect) {
        // Basically, RoF multiplier is going to be 1 + RoFMalus
        float retVal;
        float malus = calculateMissileRoFMalus(baseEffect);
        retVal = 1f + malus;

        return retVal;
    }

    private float calculateMissileRoFMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = MISSILE_ROF_PENALTY - MISSILE_ROF_PENALTY * penaltyFactor;
        /*
         * will produce 0.25 for 100% dmod effect mult
         * 0.25 - 0.25 * (0) = 0.25
         * will produce 0.125 for 50% dmod effect mult
         * 0.25 - 0.25*0.5 = 0.125
         * will produce 0.5 for 200%
         * 0.25 - 0.25*(-1) = 0.5
         * will produce 0.75 for 300%
         * 0.25 - 0.25*-2 = 0.75
         */

        return retVal;
    }

    private float calculateGuidanceMultiplier(float baseEffect) {
        // Basically, guidance multiplier is going to be 1 - GuidanceMalus
        float retVal;
        float malus = calculateGuidanceMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateGuidanceMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = MISSILE_GUIDANCE_PENALTY - MISSILE_GUIDANCE_PENALTY * penaltyFactor;
        /*
         * will produce 0.2 for 100% dmod effect mult
         * 0.2 - 0.2 * (0) = 0.2
         * will produce 0.1 for 50% dmod effect mult
         * 0.2 - 0.2*0.5 = 0.1
         * will produce 0.4 for 200%
         * 0.2 - 0.2*(-1) = 0.4
         * will produce 0.6 for 300%
         * 0.2 - 0.2*-2 = 0.6
         */

        return retVal;
    }
}
