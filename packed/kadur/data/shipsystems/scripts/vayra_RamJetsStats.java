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

public class vayra_RamJetsStats extends BaseShipSystemScript {

    private final Color color = new Color(33, 106, 109, 255);

    public static final Float RAM_JET_SPEED = 150f;
    public static final float FIGHTER_EFFECT_RANGE_MULT = 1.5f;
    public static final float FIGHTER_EFFECT_FORCE = 1312f;
    public static final float MASS_MULT = 2f;
    public static final float DMG_TAKEN = 0.5f;
    private Float mass = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (mass == null) {
            mass = ship.getMass();
        }

        ship.getEngineController().fadeToOtherColor(this, color, new Color(255, 213, 133, 255), effectLevel, 0.67f);
        ship.getEngineController().extendFlame(this, 1.5f * effectLevel, 1.5f * effectLevel, 2f * effectLevel);

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 200f);

        } else {

            List<ShipAPI> fighters = CombatUtils.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius() * FIGHTER_EFFECT_RANGE_MULT);
            float amount = Global.getCombatEngine().getElapsedInLastFrame();
            
            for (ShipAPI fighter : fighters) {
                if (!fighter.isFighter()) {
                    continue;
                }
                if (fighter.getOwner() == ship.getOwner()) {
                    continue;
                }
                if (fighter.getEngineController().isFlamedOut() || fighter.getEngineController().isFlamingOut()) {
                    fighter.getEngineController().forceFlameout();
                }
                
                float force = FIGHTER_EFFECT_FORCE * amount;
                CombatUtils.applyForce(fighter, VectorUtils.getAngle(fighter.getLocation(), ship.getLocation()), force);
                
            }

            stats.getMaxSpeed().modifyFlat(id, RAM_JET_SPEED * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 150f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 50f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 25f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getHullDamageTakenMult().modifyMult(id, DMG_TAKEN);
            stats.getArmorDamageTakenMult().modifyMult(id, DMG_TAKEN);
            stats.getEmpDamageTakenMult().modifyMult(id, 0f);
            if (ship.getMass() == mass) {
                ship.setMass(mass * MASS_MULT);
            }
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

        if (mass == null) {
            mass = ship.getMass();
        }
        if (ship.getMass() != mass) {
            ship.setMass(mass);
        }

        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
    }

    /**
     *
     * @param index
     * @param state
     * @param effectLevel
     * @return
     */
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 1) {
            return new StatusData("if i am banned from jangala for hollering at the luddites", false);
        }
        if (index == 0) {
            return new StatusData("i will face god and walk backwards into hell", false);
        }
        return null;
    }
}
