package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

// you would think that this handles shield flickering but that's actually in VayraSecretMartyrBuff for... reasons
public class VayraGarbageShields extends BaseHullMod {

    // sound that plays when you try to put on an excluded hullmod or weapon
    public static String ERROR_SOUND = "vayra_note1";

    // excluded hullmods
    public static ArrayList<String> EXCLUDED_HULLMODS = new ArrayList<>(Arrays.asList(
            HullMods.EXTENDED_SHIELDS,
            HullMods.OMNI_SHIELD_CONVERSION));

    private static final float ARC_MULT = 0.666f;
    private static final float EFF_MULT = 1.25f;
    private static final float UPKEEP_MULT = 1.5f;

    public static final Color SHIELD_INNER_COLOR = new Color(33, 106, 109, 255);
    public static final Color SHIELD_RING_COLOR = new Color(255, 255, 255, 175);

    // handles applying stat bonuses
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldArcBonus().modifyMult(id, ARC_MULT);
        stats.getShieldArcBonus().modifyFlat(id + "_flat", 1f);
        stats.getShieldDamageTakenMult().modifyMult(id, EFF_MULT);
        stats.getShieldUpkeepMult().modifyMult(id, UPKEEP_MULT);
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

    // handles shield color
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || ship.getShield() == null || ship.getShield().getType() != ShieldType.FRONT) {
            return;
        }
        ship.getShield().setInnerColor(SHIELD_INNER_COLOR);
        ship.getShield().setRingColor(SHIELD_RING_COLOR);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) ((1f - ARC_MULT) * 100) + "%";
        }
        if (index == 1) {
            return "" + (int) ((EFF_MULT - 1f) * 100) + "%";
        }
        if (index == 2) {
            return "" + (int) ((UPKEEP_MULT - 1f) * 100) + "%";
        }
        return null;
    }

}
