package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedShields extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedShields";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_SHIELD_UPKEEP_PENALTY = 0.5f;
    public static float SHIELD_UPKEEP_PENALTY = DEFAULT_SHIELD_UPKEEP_PENALTY;

    public static final float DEFAULT_SHIELD_DAMAGE_PENALTY = 0.1f;
    public static float SHIELD_DAMAGE_PENALTY = DEFAULT_SHIELD_DAMAGE_PENALTY;

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
        float upkeepMult = calculateShieldUpkeepMultiplier(effect);
        float damageMult = calculateShieldDamageMultiplier(effect);

        stats.getShieldUpkeepMult().modifyMult(id, upkeepMult);
        stats.getShieldDamageTakenMult().modifyMult(id, damageMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float upkeepMalus = calculateShieldUpkeepMalus(effect);
        float damageMalus = calculateShieldDamageMalus(effect);

        if (index == 0) {
            return Math.round(upkeepMalus * 100f) + "%";
        }
        if (index == 1) {
            return Math.round(damageMalus * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }

    private float calculateShieldUpkeepMultiplier(float baseEffect) {
        // Basically, shield upkeep multiplier is going to be 1 + ShieldUpkeepMalus
        float retVal;
        float malus = calculateShieldUpkeepMalus(baseEffect);
        retVal = 1f + malus;

        return retVal;
    }

    private float calculateShieldUpkeepMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = SHIELD_UPKEEP_PENALTY - SHIELD_UPKEEP_PENALTY * penaltyFactor;
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

    private float calculateShieldDamageMultiplier(float baseEffect) {
        // Basically, shield damage multiplier is going to be 1 + ShieldDamageMalus
        float retVal;
        float malus = calculateShieldDamageMalus(baseEffect);
        retVal = 1f + malus;

        return retVal;
    }

    private float calculateShieldDamageMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = SHIELD_DAMAGE_PENALTY - SHIELD_DAMAGE_PENALTY * penaltyFactor;
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
}
