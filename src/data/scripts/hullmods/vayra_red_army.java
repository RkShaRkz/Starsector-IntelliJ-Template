package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class vayra_red_army extends BaseHullMod {

    public static final float CHEAP_BOOTLEG_PARTS_FALL_OFF_SPACE_TRUCK = 50f; // percentage bonus to repair and CR recovery rates
    public static final float ETHANOL_IN_THE_ANTIFREEZE_TANK = 50f; // percentage resistance to EMP damage and terrain effects
    public static final float BUILT_TOUGH_LIKE_BABUSHKA = 100f; // flat bonus to weapon and engine HP

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return (int) CHEAP_BOOTLEG_PARTS_FALL_OFF_SPACE_TRUCK + "%";
        }
        if (index == 1) {
            return (int) ETHANOL_IN_THE_ANTIFREEZE_TANK + "%";
        }
        if (index == 2) {
            return "" + (int) BUILT_TOUGH_LIKE_BABUSHKA;
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - CHEAP_BOOTLEG_PARTS_FALL_OFF_SPACE_TRUCK * 0.01f);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - CHEAP_BOOTLEG_PARTS_FALL_OFF_SPACE_TRUCK * 0.01f);
        stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, CHEAP_BOOTLEG_PARTS_FALL_OFF_SPACE_TRUCK);
        stats.getRepairRatePercentPerDay().modifyPercent(id, CHEAP_BOOTLEG_PARTS_FALL_OFF_SPACE_TRUCK);

        stats.getEmpDamageTakenMult().modifyMult(id, 1f - ETHANOL_IN_THE_ANTIFREEZE_TANK * 0.01f);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 1f - ETHANOL_IN_THE_ANTIFREEZE_TANK * 0.01f);

        stats.getWeaponHealthBonus().modifyPercent(id, BUILT_TOUGH_LIKE_BABUSHKA);
    }
}
