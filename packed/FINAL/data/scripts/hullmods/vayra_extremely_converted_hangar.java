package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.DefectiveManufactory;

public class vayra_extremely_converted_hangar extends BaseHullMod {

    public static final int ALL_FIGHTER_COST_PERCENT = 50;
    public static final int BOMBER_COST_PERCENT = 100;
    public static final float SPEED_REDUCTION = 0.33333333f;
    public static final float DAMAGE_INCREASE = 0.5f;
    public static final float CARGO_PENALTY = 200f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getNumFighterBays().modifyFlat(id, 3f);

        stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(id, BOMBER_COST_PERCENT);
        stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
        stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
        stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
        stats.getCargoMod().modifyFlat(id, -CARGO_PENALTY);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "why";
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        new DefectiveManufactory().applyEffectsToFighterSpawnedByShip(fighter, ship, id + "1");
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        float effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
        float realSpeedReduction = 1f - ((1f - SPEED_REDUCTION * effect));
        if (index == 0) {
            return "" + (int) CARGO_PENALTY;
        }
        if (index == 1) {
            return "" + (int) Math.round(100f * realSpeedReduction) + "%";
        }
        if (index == 2) {
            return "" + (int) Math.round((DAMAGE_INCREASE * 100f * effect)) + "%";
        }
        if (index == 3) {
            return "" + BOMBER_COST_PERCENT + "%";
        }
        if (index == 4) {
            return "" + ALL_FIGHTER_COST_PERCENT + "%";
        }
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}
