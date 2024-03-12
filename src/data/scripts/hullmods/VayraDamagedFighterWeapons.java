package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class VayraDamagedFighterWeapons extends BaseHullMod {

    public static final float FIGHTER_DAMAGE_MULT = 0.8f;
    public static final float FIGHTER_ACCURACY_MULT = 0.5f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        CompromisedStructure.modifyCost(hullSize, stats, id);

    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {

        MutableShipStatsAPI stats = ship.getMutableStats();
        if (stats == null) return;
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float damageMult = FIGHTER_DAMAGE_MULT + (1f - FIGHTER_DAMAGE_MULT) * (1f - effect);
        float accuracyMult = FIGHTER_ACCURACY_MULT + (1f - FIGHTER_ACCURACY_MULT) * (1f - effect);

        stats = fighter.getMutableStats();
        if (stats == null) return;
        stats.getBallisticWeaponDamageMult().modifyMult(id, damageMult);
        stats.getEnergyWeaponDamageMult().modifyMult(id, damageMult);
        stats.getMissileWeaponDamageMult().modifyMult(id, damageMult);
        stats.getAutofireAimAccuracy().modifyMult(id, accuracyMult);
        stats.getRecoilPerShotMult().modifyMult(id, 1f + accuracyMult);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float damageMult = FIGHTER_DAMAGE_MULT + (1f - FIGHTER_DAMAGE_MULT) * (1f - effect);
        float accuracyMult = FIGHTER_ACCURACY_MULT + (1f - FIGHTER_ACCURACY_MULT) * (1f - effect);

        if (index == 0) {
            return Math.round((1f - damageMult) * 100f) + "%";
        }
        if (index == 1) {
            return Math.round((1f - accuracyMult) * 100f) + "%";
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }
}
