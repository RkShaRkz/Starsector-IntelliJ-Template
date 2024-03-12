package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class VayraDamagedGunnery extends BaseHullMod {

    public static final float AUTOFIRE_ACCURACY_MULT = 0.8f;
    public static final float PROJECTILE_SPEED_MULT = 0.9f;
    public static final float RECOIL_MULT = 1.5f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float autofireMult = AUTOFIRE_ACCURACY_MULT + (1f - AUTOFIRE_ACCURACY_MULT) * (1f - effect);
        float projSpeedMult = PROJECTILE_SPEED_MULT + (1f - PROJECTILE_SPEED_MULT) * (1f - effect);
        float recoilMult = RECOIL_MULT + (1f - RECOIL_MULT) * (1f - effect);

        stats.getAutofireAimAccuracy().modifyMult(id, autofireMult);
        stats.getProjectileSpeedMult().modifyMult(id, projSpeedMult);
        stats.getRecoilPerShotMult().modifyMult(id, recoilMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float autofireMult = AUTOFIRE_ACCURACY_MULT + (1f - AUTOFIRE_ACCURACY_MULT) * (1f - effect);
        float projSpeedMult = PROJECTILE_SPEED_MULT + (1f - PROJECTILE_SPEED_MULT) * (1f - effect);
        float recoilMult = RECOIL_MULT + (1f - RECOIL_MULT) * (1f - effect);

        if (index == 0) {
            return Math.round((1f - autofireMult) * 100f) + "%";
        }
        if (index == 1) {
            return Math.round((1f - projSpeedMult) * 100f) + "%";
        }
        if (index == 2) {
            return Math.round((recoilMult - 1f) * 100f) + "%";
        }
        if (index >= 3) {
            return CompromisedStructure.getCostDescParam(index, 3);
        }
        return null;
    }
}
