package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedGunnery extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedGunnery";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_AUTOFIRE_ACCURACY_PENALTY = 0.2f;
    public static float AUTOFIRE_ACCURACY_PENALTY = DEFAULT_AUTOFIRE_ACCURACY_PENALTY;

    public static final float DEFAULT_PROJECTILE_SPEED_PENALTY = 0.1f;
    public static float PROJECTILE_SPEED_PENALTY = DEFAULT_PROJECTILE_SPEED_PENALTY;

    public static final float DEFAULT_RECOIL_PENALTY = 0.5f;
    public static float RECOIL_PENALTY = DEFAULT_RECOIL_PENALTY;

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
        float autofireMult = calculateAutofireAccuracyMultiplier(effect);
        float projSpeedMult = calculateProjectileSpeedMultiplier(effect);
        float recoilMult = calculateRecoilMultiplier(effect);

        stats.getAutofireAimAccuracy().modifyMult(id, autofireMult);
        stats.getProjectileSpeedMult().modifyMult(id, projSpeedMult);
        stats.getRecoilPerShotMult().modifyMult(id, recoilMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float autofireMalus = calculateAutofireAccuracyMalus(effect);
        float projSpeedMalus = calculateProjectileSpeedMalus(effect);
        float recoilMalus = calculateRecoilMalus(effect);

        if (index == 0) {
            return Math.round(autofireMalus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(projSpeedMalus * 100f) + "%";
        }
        if (index == 2) {
            return Math.round(recoilMalus * 100f) + "%";
        }
        if (index >= 3) {
            return CompromisedStructure.getCostDescParam(index, 3);
        }
        return null;
    }

    private float calculateAutofireAccuracyMultiplier(float baseEffect) {
        // Basically, autofire multiplier is going to be 1 - AutofireMalus
        float retVal;
        float malus = calculateAutofireAccuracyMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateAutofireAccuracyMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = AUTOFIRE_ACCURACY_PENALTY - AUTOFIRE_ACCURACY_PENALTY * penaltyFactor;
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

    private float calculateProjectileSpeedMultiplier(float baseEffect) {
        // Basically, speed multiplier is going to be 1 - SpeedMalus
        float retVal;
        float malus = calculateProjectileSpeedMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateProjectileSpeedMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = PROJECTILE_SPEED_PENALTY - PROJECTILE_SPEED_PENALTY * penaltyFactor;
        /*
         * will produce 0.1 for 100% dmod effect mult
         * 0.1 - 0.1 * (0) = 0.1
         * will produce 0.05 for 50% dmod effect mult
         * 0.1 - 0.1*0.5 = 0.05
         * will produce 0.2 for 200%
         * 0.1 - 0.1*(-1) = 0.2
         * will produce 0.3 for 300%
         * 0.1 - 0.1*-2 = 0.3
         */

        return retVal;
    }

    private float calculateRecoilMultiplier(float baseEffect) {
        // Basically, recoil multiplier is going to be 1 + RecoilMalus
        float retVal;
        float malus = calculateRecoilMalus(baseEffect);
        retVal = 1f + malus;

        return retVal;
    }

    private float calculateRecoilMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = RECOIL_PENALTY - RECOIL_PENALTY * penaltyFactor;
        /*
         * will produce 0.5 for 100% dmod effect mult
         * 0.5 - 0.5 * (0) = 0.5
         * will produce 0.25 for 50% dmod effect mult
         * 0.5 - 0.5*0.5 = 0.25
         * will produce 1.0 for 200%
         * 0.5 - 0.5*(-1) = 1.0
         * will produce 1.5 for 300%
         * 0.5 - 0.5*-2 = 1.5
         */

        return retVal;
    }
}
