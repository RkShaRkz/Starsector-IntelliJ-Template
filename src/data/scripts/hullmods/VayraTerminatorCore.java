package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class VayraTerminatorCore extends BaseHullMod {

    public static float DAMAGE_MISSILES_PERCENT = 100f;
    public static float DAMAGE_FIGHTERS_PERCENT = 100f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        stats.getDamageToMissiles().modifyPercent(id, DAMAGE_MISSILES_PERCENT);
        stats.getDamageToFighters().modifyPercent(id, DAMAGE_FIGHTERS_PERCENT);
        
        stats.getBeamWeaponTurnRateBonus().modifyMult(id, 2f);
        stats.getBeamWeaponRangeBonus().modifyFlat(id, 300f);
        stats.getAutofireAimAccuracy().modifyFlat(id, 1f);

        stats.getEngineDamageTakenMult().modifyMult(id, 0f);

        stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "100%";
        }
        if (index == 1) {
            return "300 su";
        }
        return null;
    }

}
