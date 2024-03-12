package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class VayraDamagedMissiles extends BaseHullMod {

    public static final float MISSILE_AMMO_MULT = 0.5f;
    public static final int MISSILE_MIN_AMMO = 1;
    public static final float MISSILE_ROF_MULT = 0.75f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float ammoMult = MISSILE_AMMO_MULT + (1f - MISSILE_AMMO_MULT) * (1f - effect);
        float fireRateMult = MISSILE_ROF_MULT + (1f - MISSILE_ROF_MULT) * (1f - effect);

        stats.getMissileAmmoBonus().modifyMult(id, ammoMult);
        stats.getMissileRoFMult().modifyMult(id, fireRateMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float ammoMult = MISSILE_AMMO_MULT + (1f - MISSILE_AMMO_MULT) * (1f - effect);
        float fireRateMult = MISSILE_ROF_MULT + (1f - MISSILE_ROF_MULT) * (1f - effect);

        if (index == 0) {
            return Math.round((1f - ammoMult) * 100f) + "%";
        }
        if (index == 1) {
            return Math.round((1f - fireRateMult) * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }

}
