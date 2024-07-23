package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

public class VayraCaliphJetsStats extends BaseShipSystemScript {

    private final Color color = new Color(33, 106, 109, 255);
    private final Color jittercolor = new Color(255,165,90,55);
    private final Color jittercolorunder = new Color(255,165,90,155);

    public static final Float RAM_JET_SPEED = 150f;
    public static final float FIGHTER_EFFECT_RANGE_MULT = 1.5f;
    public static final float FIGHTER_EFFECT_FORCE = 1312f;
    public static final float DMG_TAKEN = 0.5f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        ship.getEngineController().fadeToOtherColor(this, color, new Color(255, 213, 133, 255), effectLevel, 0.67f);
        ship.getEngineController().extendFlame(this, 1.5f * effectLevel, 1.5f * effectLevel, 2f * effectLevel);
        
        
        
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 300f);
        } else {
            if (ship.getParentStation() == null && ship.getChildModulesCopy() != null && !ship.getChildModulesCopy().isEmpty()) {
                for (ShipAPI childModulesCopy : ship.getChildModulesCopy()) {
                    childModulesCopy.setJitterShields(false);
                    childModulesCopy.setJitterUnder(childModulesCopy.getId(), jittercolorunder, effectLevel, 10, 0, 7);
                    childModulesCopy.setJitter(childModulesCopy.getId(), jittercolor, effectLevel, 2, 0, 5);
                    childModulesCopy.getMutableStats().getHullDamageTakenMult().modifyMult(id, DMG_TAKEN);
                    childModulesCopy.getMutableStats().getArmorDamageTakenMult().modifyMult(id, DMG_TAKEN);
                    childModulesCopy.getMutableStats().getEmpDamageTakenMult().modifyMult(id, 0f);
                }
            }
            
            List<ShipAPI> fighters = CombatUtils.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius() * FIGHTER_EFFECT_RANGE_MULT);
            float amount = Global.getCombatEngine().getElapsedInLastFrame();
            
            for (ShipAPI fighter : fighters) {
                if (!fighter.isFighter()) {
                    continue;
                }
                if (fighter.getOwner() == ship.getOwner()) {
                    continue;
                }
                if (ship.getMass() > fighter.getMass() && (!fighter.getEngineController().isFlamedOut() || fighter.getEngineController().isFlamingOut())) {
                    fighter.getEngineController().forceFlameout();
                }
                
                float force = /*FIGHTER_EFFECT_FORCE */ ship.getMass() * amount;
                CombatUtils.applyForce(fighter, VectorUtils.getAngle(fighter.getLocation(), ship.getLocation()), force);
            }
            
            stats.getMaxSpeed().modifyFlat(id, RAM_JET_SPEED * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 150f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 50f * effectLevel);
            //stats.getTurnAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 25f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getHullDamageTakenMult().modifyMult(id, DMG_TAKEN);
            stats.getArmorDamageTakenMult().modifyMult(id, DMG_TAKEN);
            stats.getEmpDamageTakenMult().modifyMult(id, 0f);
        }
    }

    /**
     *
     * @param stats
     * @param id
     */
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        if (ship.getParentStation() == null && ship.getChildModulesCopy() != null && !ship.getChildModulesCopy().isEmpty()) {
                for (ShipAPI childModulesCopy : ship.getChildModulesCopy()) {
                    childModulesCopy.getMutableStats().getHullDamageTakenMult().unmodifyMult(id);
                    childModulesCopy.getMutableStats().getArmorDamageTakenMult().unmodifyMult(id);
                    childModulesCopy.getMutableStats().getEmpDamageTakenMult().unmodifyMult(id);
                }
            }
    }

    /**
     *
     * @param index
     * @param state
     * @param effectLevel
     * @return
     */
    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 1) {
            return new ShipSystemStatsScript.StatusData("if i am banned from jangala for hollering at the luddites", false);
        }
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("i will face god and walk backwards into hell", false);
        }
        return null;
    }
}
