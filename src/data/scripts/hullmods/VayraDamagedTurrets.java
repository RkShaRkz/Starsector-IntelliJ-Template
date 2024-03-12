package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class VayraDamagedTurrets extends BaseHullMod {

    public static final float WEAPON_SPEED_MULT = 0.75f;
    public static final float WEAPON_HP_MULT = 0.75f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float speedMult = WEAPON_SPEED_MULT + (1f - WEAPON_SPEED_MULT) * (1f - effect);
        float hpMult = WEAPON_HP_MULT + (1f - WEAPON_HP_MULT) * (1f - effect);

        stats.getWeaponTurnRateBonus().modifyMult(id, speedMult);
        stats.getWeaponHealthBonus().modifyMult(id, hpMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float speedMult = WEAPON_SPEED_MULT + (1f - WEAPON_SPEED_MULT) * (1f - effect);
        float hpMult = WEAPON_HP_MULT + (1f - WEAPON_HP_MULT) * (1f - effect);

        if (index == 0) {
            return Math.round((1f - speedMult) * 100f) + "%";
        }
        if (index == 1) {
            return Math.round((1f - hpMult) * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }

        return null;
    }

}
