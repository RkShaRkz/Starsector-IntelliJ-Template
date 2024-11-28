package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

public class VayraDamagedEnvironment extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedEnvironment";
    public static boolean DISABLE_FOR_PLAYER = false;
    public static boolean DISABLE_FOR_ENEMY = false;

    public static final float DAMAGE_MULT = 1.5f;

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
        float damageMult = DAMAGE_MULT + (1f - DAMAGE_MULT) * (1f - effect);

        stats.getEmpDamageTakenMult().modifyMult(id, damageMult);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, damageMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float damageMult = DAMAGE_MULT + (1f - DAMAGE_MULT) * (1f - effect);

        if (index == 0) {
            return Math.round((damageMult - 1f) * 100f) + "%";
        }
        if (index >= 1) {
            return CompromisedStructure.getCostDescParam(index, 1);
        }
        return null;
    }
}
