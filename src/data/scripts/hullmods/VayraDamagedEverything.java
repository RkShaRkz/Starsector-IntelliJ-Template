package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import com.sun.javafx.beans.annotations.NonNull;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedEverything extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedEverything";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_CR_PENALTY = 10f;
    public static float CR_PENALTY = DEFAULT_CR_PENALTY;

    public static final float DEFAULT_WEAPON_MALFUNCTION_CHANCE = 0.05f;
    public static float WEAPON_MALFUNCTION_CHANCE = DEFAULT_WEAPON_MALFUNCTION_CHANCE;

    public static final float DEFAULT_ENGINE_MALFUNCTION_CHANCE = 0.05f;
    public static float ENGINE_MALFUNCTION_CHANCE = DEFAULT_ENGINE_MALFUNCTION_CHANCE;

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
        float crPenalty = calculateCRMalus(effect);
        float weaponMalfunctionPenalty = calculateMalfunctionMalus(effect, MalfunctionType.WEAPON);
        float engineMalfunctionPenalty = calculateMalfunctionMalus(effect, MalfunctionType.ENGINE);

        stats.getMaxCombatReadiness().modifyFlat(id, -(crPenalty * 0.01f), "Performance irregularities");
        stats.getWeaponMalfunctionChance().modifyFlat(id, weaponMalfunctionPenalty);
        stats.getEngineMalfunctionChance().modifyFlat(id, engineMalfunctionPenalty);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float crPenalty = calculateCRMalus(effect);
        float weaponMalfunctionPenalty = calculateMalfunctionMalus(effect, MalfunctionType.WEAPON);
        float engineMalfunctionPenalty = calculateMalfunctionMalus(effect, MalfunctionType.ENGINE);

        if (index == 0) {
            return Math.round(crPenalty) + "%";
        }
        if (index == 1) {
            return Math.round(weaponMalfunctionPenalty * 100f) + "%";
        }
        if (index == 2) {
            return Math.round(engineMalfunctionPenalty * 100f) + "%";
        }
        if (index >= 3) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }

    private float calculateCRMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = CR_PENALTY - CR_PENALTY * penaltyFactor;
        /**
         * will produce 10 for 100% dmod effect mult
         * 10 - 10 * (0) = 10
         * will produce 5 for 50% dmod effect mult
         * 10 - 10*0.5 = 5
         * will produce 20 for 200%
         * 10 - 10*(-1) = 20
         * will produce 30 for 300%
         * 10 - 10*-2 = 30
         */

        return retVal;
    }

    private float calculateMalfunctionMalus(float baseEffect, @NonNull MalfunctionType type) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal;
        switch(type) {
            case ENGINE:
                retVal = ENGINE_MALFUNCTION_CHANCE - ENGINE_MALFUNCTION_CHANCE * penaltyFactor;
                break;
            case WEAPON:
                retVal = WEAPON_MALFUNCTION_CHANCE - WEAPON_MALFUNCTION_CHANCE * penaltyFactor;
                break;
            default: throw new IllegalArgumentException("add support for "+type+" malfunction type in VayraDamagedEverything !!!");
        }

        /*
         * I'm not gonna bother doing the math for 5%
         */

        return retVal;
    }

    private enum MalfunctionType{ WEAPON, ENGINE }
}
