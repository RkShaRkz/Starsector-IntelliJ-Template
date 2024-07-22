package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import java.util.List;

// DarkRevenant made this and i'm using it under the Ship and Weapon Pack license
// at the time of this writing, that license says:
// Code is free to copy, modify, and redistribute.  Attribution must be made to the original creator, DarkRevenant.  
// All assets are copyright their respective owners, all rights reserved.
public class vayra_modular_engines extends BaseHullMod {

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "thruster modules";
        }
        if (index == 1) {
            return "proportionate to the number of engines lost";
        }
        return null;
    }

    private static void advanceChild(ShipAPI child, ShipAPI parent) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            if (parent.isAlive()) {
                if (ec.isAccelerating()) {
                    child.giveCommand(ShipCommand.ACCELERATE, null, 0);
                }
                if (ec.isAcceleratingBackwards()) {
                    child.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                }
                if (ec.isDecelerating()) {
                    child.giveCommand(ShipCommand.DECELERATE, null, 0);
                }
                if (ec.isStrafingLeft()) {
                    child.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
                }
                if (ec.isStrafingRight()) {
                    child.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
                }
                if (ec.isTurningLeft()) {
                    child.giveCommand(ShipCommand.TURN_LEFT, null, 0);
                }
                if (ec.isTurningRight()) {
                    child.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
                }
            }

            ShipEngineControllerAPI cec = child.getEngineController();
            if (cec != null) {
                if ((ec.isFlamingOut() || ec.isFlamedOut()) && !cec.isFlamingOut() && !cec.isFlamedOut()) {
                    child.getEngineController().forceFlameout(true);
                }
            }
        }
    }

    private static void advanceParent(ShipAPI parent, List<ShipAPI> children) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            int originalEngines = 10;

            float workingEngines = ec.getShipEngines().size();
            for (ShipAPI child : children) {
                if (child.getParentStation() == parent && child.getStationSlot() != null && child.isAlive()) {
                    ShipEngineControllerAPI cec = child.getEngineController();
                    if (cec != null) {
                        workingEngines += cec.getShipEngines().size() * (1f - cec.computeDisabledFraction());
                    }
                }
            }
            
            float enginePerformance = Math.max(1f, workingEngines / originalEngines);
            parent.getMutableStats().getAcceleration().modifyMult("vayra_modular_engines", enginePerformance);
            parent.getMutableStats().getDeceleration().modifyMult("vayra_modular_engines", enginePerformance);
            parent.getMutableStats().getTurnAcceleration().modifyMult("vayra_modular_engines", enginePerformance);
            parent.getMutableStats().getMaxTurnRate().modifyMult("vayra_modular_engines", enginePerformance);
            parent.getMutableStats().getMaxSpeed().modifyMult("vayra_modular_engines", enginePerformance);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ShipAPI parent = ship.getParentStation();
        if (parent != null) {
            advanceChild(ship, parent);
        }

        List<ShipAPI> children = ship.getChildModulesCopy();
        if (children != null && !children.isEmpty()) {
            advanceParent(ship, children);
        }
    }
}
