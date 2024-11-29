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

public class VayraDamagedAutomation extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedAutomation";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float CR_PENALTY = 10f;
    public static final float MIN_CREW_MULT = 1.5f;

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
        float crPenalty = calculateCRPenalty(stats.getVariant(), effect);
        float minCrewMult = calculateMinCrewMultiplier(stats.getVariant(), effect);

        stats.getMaxCombatReadiness().modifyFlat(id, -(crPenalty * 0.01f), "Damaged Automated Systems");
        stats.getMinCrewMod().modifyMult(id, minCrewMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    private float calculateCRPenalty(ShipVariantAPI variant, float baseEffect) {
        // Initialize to base value
        float retVal = CR_PENALTY * baseEffect;

        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }

    private float calculateMinCrewMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Initialize to base value
        float retVal = MIN_CREW_MULT + (1f - MIN_CREW_MULT) * (1f - baseEffect);;
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        // And finally, since there is a case that this D-Mod might actually end up *decreasing* the min crew
        // when rugged is present (1.5 / 2 = 0.75) lets clamp it between 1.0 and whatever it came up with
        // or rather min(retVal, max(retVal, 1.0))
        return clamp(retVal, 1.0f);
    }

    /**
     * Ensures that the {@code actualValue} value can't go below {@code minValue} value
     * @param actualValue the actual value to clamp
     * @param minValue the clamping cut-off value
     * @return the clamped value if it was below the cutoff or itself if it wasn't
     */
    private float clamp(float actualValue, float minValue) {
        return (float) Math.min(actualValue, Math.max(actualValue, 1.0));
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        ShipVariantAPI variant = null;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            variant = ship.getVariant();
        }
        float crPenalty = calculateCRPenalty(variant, effect);
        float minCrewMult = calculateMinCrewMultiplier(variant, effect);

        if (index == 0) {
            return Math.round(crPenalty) + "%";
        }
        if (index == 1) {
            return Math.round((minCrewMult - 1f) * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }
}
