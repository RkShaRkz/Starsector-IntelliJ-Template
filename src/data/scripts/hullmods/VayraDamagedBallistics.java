package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedBallistics extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedBallistics";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_BALLISTIC_ROF_PENALTY = 0.2f;
    public static float BALLISTIC_ROF_PENALTY = DEFAULT_BALLISTIC_ROF_PENALTY;
    public static final float DEFAULT_BALLISTIC_FLUX_PENALTY = 0.25f;
    public static float BALLISTIC_FLUX_PENALTY = DEFAULT_BALLISTIC_FLUX_PENALTY;

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
        float fireRateMult = calculateFireRateMultiplier(effect);
        float fluxMult = calculateFluxMultiplier(effect);

        stats.getBallisticRoFMult().modifyMult(id, fireRateMult);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, fluxMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float fireRateMalus = calculateFireRateMalus(effect);
        float fluxMalus = calculateFluxMalus(effect);

        if (index == 0) {
            return Math.round(fireRateMalus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(fluxMalus * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }

    private float calculateFireRateMultiplier(float baseEffect) {
        // Basically, fire rate multiplier is going to be 1 - FireRateMalus
        float retVal;
        float malus = calculateFireRateMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateFireRateMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = BALLISTIC_ROF_PENALTY - BALLISTIC_ROF_PENALTY * penaltyFactor;
        /**
         * will produce 0.2 for 100% dmod effect mult
         * 0.2 - 0.2 * (0) = 0.2
         * will produce 0.3 for 50% dmod effect mult
         * 0.2 - 0.2*0.5 = 0.1
         * will produce 0.4 for 200%
         * 0.2 - 0.2*(-1) = 0.4
         * will produce 0.6 for 300%
         * 0.2 - 0.2*-2 = 0.6
         */

        return retVal;
    }

    private float calculateFluxMultiplier(float baseEffect) {
        // Basically, the flux multiplier is going to be 1 + FluxMalus
        float retVal;
        float malus = calculateFluxMalus(baseEffect);
        retVal = 1f + malus;

        return retVal;
    }

    private float calculateFluxMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = BALLISTIC_FLUX_PENALTY - BALLISTIC_FLUX_PENALTY * penaltyFactor;
        /**
         * will produce 0.25 for 100% dmod effect mult
         * 0.25 - 0.25 * 0 = 0.25
         * will produce 0.125 for 50% dmod effect mult
         * 0.25 - 0.25*0.5 = 0.125
         * will produce 0.5 for 200% dmod effect mult
         * 0.25 - 0.25*(-1) = 0.5
         * will produce 0.75 for 300% dmod effect mult
         * 0.25 - 0.25*(-2) = 0.75
         */

        return retVal;
    }
}