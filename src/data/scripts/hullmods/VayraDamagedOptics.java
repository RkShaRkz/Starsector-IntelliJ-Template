package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
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
        float rangeMult = calculateBeamRangeMultiplier(stats.getVariant(), effect);

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
            if (w.isBeam() && w.isFiring()) {
                float[] moveArray = generateMoveArray(ship, w);
                int maxOffsetSize = getMaximumWeaponSpecAngleOffsetsSize(w);
                for (int i = 0; i < maxOffsetSize; i++) {
                    if (i < w.getSpec().getTurretAngleOffsets().size()) {
                        w.getSpec().getTurretAngleOffsets().set(i, moveArray[i]);
                    }
                    if (i < w.getSpec().getHardpointAngleOffsets().size()) {
                        w.getSpec().getHardpointAngleOffsets().set(i, moveArray[i]);
                    }
                    if (i < w.getSpec().getHiddenAngleOffsets().size()) {
                        w.getSpec().getHiddenAngleOffsets().set(i, moveArray[i]);
                    }
                }
            }
        }
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
        return calculateBeamWaverMalus(ship.getVariant(), effect);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = 1f;
        ShipVariantAPI variant = null;
        if (ship != null) {
            effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            variant = ship.getVariant();
        }
        float beamWaver = calculateBeamWaverMalus(variant, effect);
        float beamRangeMalus = calculateBeamRangeMalus(variant, effect);

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

    private float calculateBeamRangeMultiplier(ShipVariantAPI variant, float baseEffect) {
        // Basically, beam range multiplier is going to be 1 - BeamRangeMalus
        float retVal;
        float malus = calculateBeamRangeMalus(variant, baseEffect);
        retVal = 1f - malus;

        return retVal;
    }

    private float calculateBeamRangeMalus(ShipVariantAPI variant, float baseEffect) {
        float penaltyFactor = (1f - baseEffect);    // will be 0 for nominal DMOD_EFFECT_MULT
        float retVal = BEAM_RANGE_PENALTY - BEAM_RANGE_PENALTY * penaltyFactor;
        /**
         * will produce 0.15 for 100% dmod effect mult
         * 0.15 - 0.15 * (0) = 0.15
         * will produce 0.075 for 50% dmod effect mult
         * 0.15 - 0.15*0.5 = 0.075
         * will produce 0.3 for 200%
         * 0.15 - 0.15*(-1) = 0.3
         * will produce 0.45 for 300%
         * 0.15 - 0.15*-2 = 0.45
         *
         * and then half of that if we have "rugged"
         */
        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }

    public float calculateBeamWaverMalus(ShipVariantAPI variant, float effect) {
        float retVal = BEAM_WAVER * effect;

        if (variant != null) {
            boolean hasRugged = MiscUtils.hasRuggedConstructionHullmod(variant);

            if (hasRugged) {
                retVal = retVal / 2;
            }
        }

        return retVal;
    }
}
