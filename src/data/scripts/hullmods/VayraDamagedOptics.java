package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class VayraDamagedOptics extends BaseHullMod {

    public static final float BEAM_RANGE_MULT = 0.75f;
    public static final float BEAM_WAVER = 5f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float rangeMult = BEAM_RANGE_MULT + (1f - BEAM_RANGE_MULT) * (1f - effect);

        stats.getBeamWeaponRangeBonus().modifyMult(id, rangeMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats == null || engine == null || engine.isPaused()) {
            return;
        }

        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float beamWaver = BEAM_WAVER * effect;

        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.isBeam() && w.isFiring()) {
                for (int i = 0; i < w.getSpec().getTurretAngleOffsets().size(); i++) {
                    float move = (float) ((Math.random() * beamWaver) + (Math.random() * -beamWaver));
                    if (i < w.getSpec().getHardpointAngleOffsets().size()) {
                        w.getSpec().getHardpointAngleOffsets().set(i, move);
                    }
                    if (i < w.getSpec().getTurretAngleOffsets().size()) {
                        // Unnecessary since we're already using this as our loop condition but why not
                        w.getSpec().getTurretAngleOffsets().set(i, move);
                    }
                    if (i< w.getSpec().getHiddenAngleOffsets().size()) {
                        w.getSpec().getHiddenAngleOffsets().set(i, move);
                    }
                }
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float rangeMult = BEAM_RANGE_MULT + (1f - BEAM_RANGE_MULT) * (1f - effect);
        float beamWaver = BEAM_WAVER * effect;

        if (index == 0) {
            return Math.round((1f - rangeMult) * 100f) + "%";
        }
        if (index == 1) {
            return "" + Math.round(beamWaver * 2f);
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }

}
