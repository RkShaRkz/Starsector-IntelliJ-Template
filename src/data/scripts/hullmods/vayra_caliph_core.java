package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import java.util.ArrayList;
import java.util.Arrays;

public class vayra_caliph_core extends BaseHullMod {

    // sound that plays when you try to put on an excluded hullmod or weapon
    public static String ERROR_SOUND = "vayra_note1";

    // excluded hullmods
    public static ArrayList<String> EXCLUDED_HULLMODS = new ArrayList<>(Arrays.asList(
            HullMods.DEDICATED_TARGETING_CORE,
            HullMods.INTEGRATED_TARGETING_UNIT));

    public static final float VISION_BONUS = 2000f;
    public static final float AUTOFIRE_PENALTY = -0.5f;

    // all of these following are PERCENTAGE MODIFIERS
    public static final float ANTIFTR_BONUS = 100f; // +100%
    public static final float RANGE_BONUS = 175f; // +175%
    public static final float RECOIL_PENALTY = 10f; // +10% recoil (bad)

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + Math.round(ANTIFTR_BONUS) + "%";
        }
        if (index == 1) {
            return "non-PD";
        }
        if (index == 2) {
            return "" + Math.round(RANGE_BONUS) + "%";
        }
        if (index == 3) {
            return "significantly";
        }
        if (index == 4) {
            return "" + Math.round(Math.abs(AUTOFIRE_PENALTY) * 100f) + "%";
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);
        stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);

        stats.getSightRadiusMod().modifyFlat(id, VISION_BONUS);
        
        stats.getAutofireAimAccuracy().modifyFlat(id, AUTOFIRE_PENALTY);

        //stats.getMaxRecoilMult().modifyPercent(id, RECOIL_PENALTY);
        //stats.getRecoilPerShotMult().modifyPercent(id, RECOIL_PENALTY);
        
        stats.getDamageToFighters().modifyPercent(id, ANTIFTR_BONUS);
        if (stats.getVariant().hasHullMod("converted_hangar")) {
            if (stats.getVariant().hasHullMod("vayra_slow_autoforge")) {stats.getNumFighterBays().modifyFlat(id, 1f);}
            if ((stats.getVariant().hasHullMod("vayra_slow_autoforge") && stats.getVariant().hasHullMod("vayra_modular_engines")) || !stats.getVariant().hasHullMod("vayra_modular_engines")) {stats.addListener(new ConvertedHangarScript());}
        }
        
    }
    
    // handles removing excluded hullmods
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        // remove excluded mods, play a sound if we do
        ArrayList<String> delete = new ArrayList<>();
        for (String excluded : EXCLUDED_HULLMODS) {
            if (ship.getVariant().hasHullMod(excluded)) {
                delete.add(excluded);
            }
        }
        for (String toDelete : delete) {
            ship.getVariant().removeMod(toDelete);
            Global.getSoundPlayer().playUISound(ERROR_SOUND, 1f, 1f);
        }
    }
    public static class ConvertedHangarScript implements FighterOPCostModifier {
            
            @Override
                        public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
				if (fighter != null && fighter.hasTag("kadur")) {return (int) fighter.getOpCost(null)/2;}
                                return currCost;
			}
        }
    
    @Override
    public boolean affectsOPCosts() {
	return true;
    }
}
