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

public class VayraDamagedEnvironment extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedEnvironment";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DAMAGE_PENALTY = 0.5f;

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
        float damageMult = calculateDamageMultiplier(stats.getVariant(), effect);

        stats.getEmpDamageTakenMult().modifyMult(id, damageMult);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, damageMult);

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
        float damageMalus = calculateDamageMalus(variant, effect);

        if (index == 0) {
            return Math.round(damageMalus * 100f) + "%";
        }
        if (index >= 1) {
            return CompromisedStructure.getCostDescParam(index, 1);
        }
        return null;
    }

    private float calculateDamageMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, damage multiplier is going to be 1 + DamageMalus
        float retVal;
        float malus = calculateDamageMalus(variant, baseEffect);
        retVal = 1f + malus;

        return retVal;
    }

    private float calculateDamageMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = DAMAGE_PENALTY - DAMAGE_PENALTY * penaltyFactor;
        /**
         * will produce 0.5 for 100% dmod effect mult
         * 0.5 - 0.5 * (0) = 0.5
         * will produce 0.25 for 50% dmod effect mult
         * 0.5 - 0.5*0.5 = 0.25
         * will produce 1.0 for 200%
         * 0.5 - 0.5*(-1) = 1.0
         * will produce 1.5 for 300%
         * 0.5 - 0.5*-2 = 1.5
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
