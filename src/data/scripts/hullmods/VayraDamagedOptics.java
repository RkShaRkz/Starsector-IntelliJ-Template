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

        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.isBeam() && w.isFiring()) {
                float[] moveArray = generateMoveArray(ship, w);
                for (int i = 0; i < w.getSpec().getTurretAngleOffsets().size(); i++) {
                    w.getSpec().getTurretAngleOffsets().set(i, moveArray[i]);
                }
                for (int i = 0; i < w.getSpec().getHardpointAngleOffsets().size(); i++) {
                    w.getSpec().getHardpointAngleOffsets().set(i, moveArray[i]);
                }
                for (int i = 0; i < w.getSpec().getHiddenAngleOffsets().size(); i++) {
                    w.getSpec().getHiddenAngleOffsets().set(i, moveArray[i]);
                }
            }
        }
    }

    private float[] generateMoveArray(ShipAPI ship, WeaponAPI weapon) {
        float beamWaver = getBeamWaverValue(ship);
        // First, figure out how many items we have
        int size = 0;
        size = Math.max(size, weapon.getSpec().getTurretAngleOffsets().size());
        size = Math.max(size, weapon.getSpec().getHardpointAngleOffsets().size());
        size = Math.max(size, weapon.getSpec().getHiddenAngleOffsets().size());

        // now that we know how large the random array should be, lets create it
        float[] retVal = new float[size];
        for (int i = 0; i < size; i++) {
            retVal[i] =  (float) ((Math.random() * beamWaver) + (Math.random() * -beamWaver));
        }

        return retVal;
    }

    private float getBeamWaverValue(ShipAPI ship) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats == null || engine == null || engine.isPaused()) {
            return 0f;
        }

        float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);

        return BEAM_WAVER * effect;
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
