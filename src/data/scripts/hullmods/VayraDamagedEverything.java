package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class VayraDamagedEverything extends BaseHullMod {

    public static final float CR_PENALTY = 10f;
    public static final float MALFUNCTION_CHANCE = 0.01f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float crPenalty = CR_PENALTY * effect;

        stats.getMaxCombatReadiness().modifyFlat(id, -(crPenalty * 0.01f), "Performance irregularities");
        stats.getWeaponMalfunctionChance().modifyFlat(id, MALFUNCTION_CHANCE * effect);
        stats.getEngineMalfunctionChance().modifyFlat(id, MALFUNCTION_CHANCE * 0.5f * effect);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float crPenalty = CR_PENALTY * effect;

        if (index == 0) {
            return Math.round(crPenalty) + "%";
        }
        if (index == 1) {
            return "minor";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }
}
