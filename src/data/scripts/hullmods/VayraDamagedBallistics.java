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
    public static boolean DISABLE_FOR_PLAYER = false;
    public static boolean DISABLE_FOR_ENEMY = false;

    public static final float BALLISTIC_ROF_MULT = 0.8f;
    public static final float BALLISTIC_FLUX_MULT = 1.25f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Do nothing if it's been disabled
        if (stats.getFleetMember() != null) {
            int owner = stats.getFleetMember().getOwner();
            if (owner == MiscUtils.OWNER_ENEMY && DISABLE_FOR_ENEMY) return;
            if (owner == MiscUtils.OWNER_PLAYER && DISABLE_FOR_PLAYER) return;
        } else {
            MiscUtils.log(LoggerLogLevel.WARN, LOGTAG, "Encountered a ship with NULL fleetmember in 'applyEffectsBeforeShipCreation' so settings to disable it won't work");
        }

        // Carry on as usual
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float fireRateMult = BALLISTIC_ROF_MULT + (1f - BALLISTIC_ROF_MULT) * (1f - effect);
        float fluxMult = BALLISTIC_ROF_MULT + (1f - BALLISTIC_ROF_MULT) * (1f - effect);

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
        float fireRateMult = BALLISTIC_ROF_MULT + (1f - BALLISTIC_ROF_MULT) * (1f - effect);
        float fluxMult = BALLISTIC_FLUX_MULT + (1f - BALLISTIC_FLUX_MULT) * (1f - effect);

        if (index == 0) {
            return Math.round((1f - fireRateMult) * 100f) + "%";
        }
        if (index == 1) {
            return Math.round((1f - fluxMult) * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }
}