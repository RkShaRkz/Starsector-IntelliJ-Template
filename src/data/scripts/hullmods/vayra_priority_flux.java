package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import java.util.ArrayList;
import java.util.Arrays;

public class vayra_priority_flux extends BaseHullMod {

    // sound that plays when you try to put on an excluded hullmod or weapon
    private static final String ERROR_SOUND = "vayra_note1";
    private static final String ID = "vayra_priority_flux_redirects";
    public static String ENGINE_SPRITE = "graphics/icons/hullsys/infernium_injector.png";
    public static String SHIELD_SPRITE = "graphics/icons/hullsys/fortress_shield.png";
    public static String WEAPON_SPRITE = "graphics/icons/hullsys/ammo_feeder.png";

    // excluded hullmods
    private static final ArrayList<String> EXCLUDED_HULLMODS = new ArrayList<>(Arrays.asList(
            HullMods.SAFETYOVERRIDES));

    // conditional stat modifiers
    private static final float ZERO_FLUX_SPEED_BOOST = 50f; // flat modifier to zero flux speed boost
    private static final float ZERO_FLUX_MANEUVERABILITY_MULT = 3f; // multiplier to maneuverability stats at zero flux

    private static final float SHIELD_DOWN_ROF_MULT = 1.5f; // multiplier to rate of fire when shields are down
    private static final float SHIELD_UP_ROF_MULT = 0.75f; // multiplier to rate of fire when shields are up

    private static final float SHIELD_UPKEEP_MULT = 2f; // multiplier to shield upkeep cost
    private static final float SHIELD_DAMAGE_MULT = 0.5f; // multiplier to shield damage taken

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_SPEED_BOOST);
        stats.getShieldUpkeepMult().modifyMult(id, SHIELD_UPKEEP_MULT);
        stats.getShieldDamageTakenMult().modifyMult(id, SHIELD_DAMAGE_MULT);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        
        // remove excluded mods, play a sound if we do
        ArrayList<String> deleteHullmods = new ArrayList<>();
        for (String excluded : EXCLUDED_HULLMODS) {
            if (ship.getVariant().hasHullMod(excluded)) {
                deleteHullmods.add(excluded);
            }
        }
        for (String toDelete : deleteHullmods) {
            ship.getVariant().removeMod(toDelete);
            Global.getSoundPlayer().playUISound(ERROR_SOUND, 1f, 1f);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || engine == null) {
            return;
        }

        MutableShipStatsAPI stats = ship.getMutableStats();
        String id = ID;

        if (ship.getMaxSpeed() > ship.getMaxSpeedWithoutBoost()) {
            stats.getAcceleration().modifyMult(id, ZERO_FLUX_MANEUVERABILITY_MULT);
            stats.getDeceleration().modifyMult(id, ZERO_FLUX_MANEUVERABILITY_MULT);
            stats.getMaxTurnRate().modifyMult(id, ZERO_FLUX_MANEUVERABILITY_MULT);
            stats.getTurnAcceleration().modifyMult(id, ZERO_FLUX_MANEUVERABILITY_MULT);
            ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
            engine.maintainStatusForPlayerShip(id, ENGINE_SPRITE, "Full Power to Engines", "increased maneuverability", false);
        } else {
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
            stats.getTurnAcceleration().unmodify(id);
            ship.getEngineController().extendFlame(this, 0f, 0f, 0f);
        }

        // shield stuff
        if (ship.getShield() == null || ship.getShield().getType().equals(ShieldType.NONE) || ship.getShield().isOff()) {
            stats.getBallisticRoFMult().modifyMult(id, SHIELD_DOWN_ROF_MULT);
            engine.maintainStatusForPlayerShip(id, WEAPON_SPRITE, "Full Power to Weapons", "increased fire rate", false);
        } else {
            stats.getBallisticRoFMult().modifyMult(id, SHIELD_UP_ROF_MULT);
            engine.maintainStatusForPlayerShip(id, WEAPON_SPRITE, "Full Power to Shields", "reduced fire rate", true);
            engine.maintainStatusForPlayerShip(id, SHIELD_SPRITE, "Full Power to Shields", "increased shield efficiency", false);
            engine.maintainStatusForPlayerShip(id, SHIELD_SPRITE, "Full Power to Shields", "increased shield upkeep", true);
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        for (String hullmod : ship.getVariant().getHullMods()) {
            if (EXCLUDED_HULLMODS.contains(hullmod)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        for (String hullmod : ship.getVariant().getHullMods()) {
            if (EXCLUDED_HULLMODS.contains(hullmod)) {
                return "Incompatible with Safety Overrides";
            }
        }
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "100";
        }
        if (index == 1) {
            return "greatly increased";
        }
        if (index == 2) {
            return "100%";
        }
        if (index == 3) {
            return "50%";
        }
        if (index == 4) {
            return "25%";
        }
        return null;
    }
}
