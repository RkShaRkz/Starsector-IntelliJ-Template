// bye vayra
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import java.util.ArrayList;
import java.util.Arrays;

public class vayra_targeting_core extends BaseHullMod {

    // sound that plays when you try to put on an excluded hullmod or weapon
    public static String ERROR_SOUND = "vayra_note1";

    // excluded hullmods
    public static ArrayList<String> EXCLUDED_HULLMODS = new ArrayList<>(Arrays.asList(
            HullMods.DEDICATED_TARGETING_CORE,
            HullMods.INTEGRATED_TARGETING_UNIT));

    // range bonus
    public static float RANGE_BONUS = 50f; // multiplier for weapon range

    // handles applying stat bonuses
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
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

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) (RANGE_BONUS) + "%";
        }
        return null;
    }

}
