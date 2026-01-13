package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import data.scripts.util.MiscUtils;
import data.util.LoggerLogLevel;

import static data.scripts.util.MiscUtils.getMaximumWeaponSpecAngleOffsetsSize;

public class VayraDamagedOptics extends BaseHullMod {
    private static final String LOGTAG = "VayraDamagedOptics";
    public static volatile boolean DISABLE_FOR_PLAYER = false;
    public static volatile boolean DISABLE_FOR_ENEMY = false;

    public static final float DEFAULT_BEAM_RANGE_PENALTY = 0.15f;
    public static float BEAM_RANGE_PENALTY = DEFAULT_BEAM_RANGE_PENALTY;

    public static final float DEFAULT_BEAM_WAVER = 5f;
    public static float BEAM_WAVER = DEFAULT_BEAM_WAVER;


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
        float rangeMult = calculateBeamRangeMultiplier(effect);

        stats.getBeamWeaponRangeBonus().modifyMult(id, rangeMult);

        CompromisedStructure.modifyCost(hullSize, stats, id);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        // Do nothing if it's been disabled
        int owner = ship.getOwner();
        if (DISABLE_FOR_ENEMY && MiscUtils.OWNER_ENEMY == owner) return;
        if (DISABLE_FOR_PLAYER && MiscUtils.OWNER_PLAYER == owner) return;

        // Carry on as usual
        MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats == null || engine == null || engine.isPaused()) {
            return;
        }

        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.isBeam()) {
                // Beam weapons get special treatment with this DMod
                if(w.isFiring()) {
                    // If the weapon is firing, generate a "move array", clone the spec and start messing with it's
                    // angle offsets
                    float[] moveArray = generateMoveArray(ship, w);
                    int maxOffsetSize = getMaximumWeaponSpecAngleOffsetsSize(w);
                    // Before we start messing with the WeaponSpecAPI (which is shared by all instances of this weapon)
                    // lets first ensure that we have a local clone we can modify, so that we don't end up messing up all
                    // instances of this weapon across all ships (even those that never had this DMod)
                    w.ensureClonedSpec();
                    for (int i = 0; i < maxOffsetSize; i++) {
                        if(i < w.getSpec().getTurretAngleOffsets().size()) {
                            w.getSpec().getTurretAngleOffsets().set(i, moveArray[i]);
                        }

                        if(i < w.getSpec().getHardpointAngleOffsets().size()) {
                            w.getSpec().getHardpointAngleOffsets().set(i, moveArray[i]);
                        }

                        if(i < w.getSpec().getHiddenAngleOffsets().size()) {
                            w.getSpec().getHiddenAngleOffsets().set(i, moveArray[i]);
                        }
                    }
                } else {
                    // If we're not firing, just "reset" the weapon to it's original angles
                    resetWeaponsAngles(w);
                }
            }
        }
    }

    private void resetWeaponsAngles(WeaponAPI weapon) {
        // First, grab the original spec
        WeaponSpecAPI originalSpec = getWeaponsOriginalSpec(weapon);
        // Now, go through each of the original spec's offsets, and set the weapon's offsets to those
        int maxOffsetSize = getMaximumWeaponSpecAngleOffsetsSize(weapon);
        for (int i = 0; i < maxOffsetSize; i++) {
            // turret angles
            if (i < weapon.getSpec().getTurretAngleOffsets().size()) {
                float originalValue = originalSpec.getTurretAngleOffsets().get(i);
                weapon.getSpec().getTurretAngleOffsets().set(i, originalValue);
            }

            // hardpoint angles
            if(i < weapon.getSpec().getHardpointAngleOffsets().size()) {
                float originalValue = originalSpec.getHardpointAngleOffsets().get(i);
                weapon.getSpec().getHardpointAngleOffsets().set(i, originalValue);
            }

            if(i < weapon.getSpec().getHiddenAngleOffsets().size()) {
                float originalValue = originalSpec.getHiddenAngleOffsets().get(i);
                weapon.getSpec().getHiddenAngleOffsets().set(i, originalValue);
            }
        }
    }

    private WeaponSpecAPI getWeaponsOriginalSpec(WeaponAPI weapon) {
        return Global.getSettings().getWeaponSpec(weapon.getId());
    }

    private float[] generateMoveArray(ShipAPI ship, WeaponAPI weapon) {
        float beamWaver = getBeamWaverValue(ship);
        // First, figure out how many items we have
        int size = getMaximumWeaponSpecAngleOffsetsSize(weapon);

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
        return calculateBeamWaverMalus(effect);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        }
        float beamWaver = calculateBeamWaverMalus(effect);
        float beamRangeMalus = calculateBeamRangeMalus(effect);

        if (index == 0) {
            return Math.round(beamRangeMalus * 100f) + "%";
        }
        if (index == 1) {
            return "" + Math.round(beamWaver * 2f);
        }
        if (index >= 2) {
            return CompromisedStructure.getCostDescParam(index, 2);
        }
        return null;
    }

    private float calculateBeamRangeMultiplier(float baseEffect) {
        // Basically, beam range multiplier is going to be 1 - BeamRangeMalus
        float retVal;
        float malus = calculateBeamRangeMalus(baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateBeamRangeMalus(float baseEffect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = BEAM_RANGE_PENALTY - BEAM_RANGE_PENALTY * penaltyFactor;
        /*
         * will produce 0.15 for 100% dmod effect mult
         * 0.15 - 0.15 * (0) = 0.15
         * will produce 0.075 for 50% dmod effect mult
         * 0.15 - 0.15*0.5 = 0.075
         * will produce 0.3 for 200%
         * 0.15 - 0.15*(-1) = 0.3
         * will produce 0.45 for 300%
         * 0.15 - 0.15*-2 = 0.45
         */

        return retVal;
    }

    public float calculateBeamWaverMalus(float effect) {
        // Since having "rugged" is already implicitly a part of baseEffect, meaning it will come in as 0.5
        // instead of 1.0, the penalty factor will also end up being 0.5 so we don't need to check for rugged
        float retVal = BEAM_WAVER * effect;

        return retVal;
    }
}
