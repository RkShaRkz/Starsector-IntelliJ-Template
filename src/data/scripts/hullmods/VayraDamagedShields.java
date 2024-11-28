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
    public static boolean DISABLE_FOR_PLAYER = false;
    public static boolean DISABLE_FOR_ENEMY = false;

    public static final float SHIELD_UPKEEP_MULT = 1.5f;
    public static final float SHIELD_DAMAGE_MULT = 1.1f;

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
        float upkeepMult = SHIELD_UPKEEP_MULT + (1f - SHIELD_UPKEEP_MULT) * (1f - effect);
        float damageMult = SHIELD_DAMAGE_MULT + (1f - SHIELD_DAMAGE_MULT) * (1f - effect);

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
        float upkeepMult = SHIELD_UPKEEP_MULT + (1f - SHIELD_UPKEEP_MULT) * (1f - effect);
        float damageMult = SHIELD_DAMAGE_MULT + (1f - SHIELD_DAMAGE_MULT) * (1f - effect);

        if (index == 0) {
            return Math.round((upkeepMult - 1f) * 100f) + "%";
        }
        if (index == 1) {
            return Math.round((damageMult - 1f) * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }
}
