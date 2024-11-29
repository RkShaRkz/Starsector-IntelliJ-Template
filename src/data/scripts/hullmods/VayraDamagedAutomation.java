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
        float retVal;
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = (CR_PENALTY * baseEffect) / 2;
            } else {
                retVal = CR_PENALTY * baseEffect;
            }
        } else {
            // If variant is null, just return the basic thing
            retVal = CR_PENALTY * baseEffect;
        }

        return retVal;
    }

    private float calculateMinCrewMultiplier(ShipVariantAPI variant, float baseEffect) {
        float retVal;
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = (MIN_CREW_MULT + (1f - MIN_CREW_MULT) * (1f - baseEffect)) / 2;
            } else {
                retVal = MIN_CREW_MULT + (1f - MIN_CREW_MULT) * (1f - baseEffect);
            }
        } else {
            // If variant is null, just return the basic thing
            retVal = MIN_CREW_MULT + (1f - MIN_CREW_MULT) * (1f - baseEffect);
        }

        return retVal;
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
