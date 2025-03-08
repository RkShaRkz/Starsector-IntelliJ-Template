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

public class VayraDamagedLifeSupport extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedLifeSupport";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_CR_PENALTY = 15f;
    public static float CR_PENALTY = DEFAULT_CR_PENALTY;

    public static final float DEFAULT_CREW_CAPACITY_PENALTY = 0.75f;
    public static float CREW_CAPACITY_PENALTY = DEFAULT_CREW_CAPACITY_PENALTY;

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
        float crPenalty = calculateCRMalus(stats.getVariant(), effect);
        float crewMult = calculateCrewCapacityMultiplier(stats.getVariant(), effect);

        stats.getMaxCombatReadiness().modifyFlat(id, -(crPenalty * 0.01f), "Damaged life support");
        stats.getMaxCrewMod().modifyMult(id, crewMult);

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
        float crPenalty = calculateCRMalus(variant, effect);
        float crewMalus = calculateCrewCapacityMalus(variant, effect);

        if (index == 0) {
            return Math.round(crPenalty) + "%";
        }
        if (index == 1) {
            return Math.round(crewMalus * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }


    private float calculateCRMalus(ShipVariantAPI variant, float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = CR_PENALTY - CR_PENALTY * penaltyFactor;
        /*
         * will produce 15 for 100% dmod effect mult
         * 15 - 15 * (0) = 15
         * will produce 7.5 for 50% dmod effect mult
         * 15 - 15*0.5 = 7.5
         * will produce 30 for 200%
         * 15 - 15*(-1) = 30
         * will produce 45 for 300%
         * 15 - 15*-2 = 45
         */

        return retVal;
    }

    private float calculateCrewCapacityMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, crew capacity multiplier is going to be 1 - CrewCapacityMalus
        float retVal;
        float malus = calculateCrewCapacityMalus(variant, baseEffect);
        retVal = 1f - malus;

        // In this exceptional case, i think it would make sense to clamp it to 0
        return MiscUtils.clamp(retVal, 0.0f);
    }

    private float calculateCrewCapacityMalus(ShipVariantAPI variant, float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = CREW_CAPACITY_PENALTY - CREW_CAPACITY_PENALTY * penaltyFactor;
        /*
         * will produce 0.75 for 100% dmod effect mult
         * 0.75 - 0.75 * (0) = 0.75
         * will produce 0.375 for 50% dmod effect mult
         * 0.75 - 0.7*0.5 = 0.375
         * will produce 1.5 for 200%
         * 0.75 - 0.75*(-1) = 1.5
         * will produce 2.25 for 300%
         * 0.75 - 0.75*-2 = 2.25
         */

        return retVal;
    }
}
