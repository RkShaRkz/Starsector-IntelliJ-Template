package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class vayra_RamDriveStats extends BaseShipSystemScript {

    private final Color color = new Color(33, 106, 109, 255);

    public static final float MASS_MULT = 5f;
    public static final float DMG_TAKEN = 0.2f;
    public static final float STOP_RANGE = 100f;

    private Float mass = null;

    private Vector2f start = null;
    private Vector2f point = null;
    private float startAngle;
    private float targetAngle;

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
        ship.getEngineController().extendFlame(this, 4f * effectLevel, 4f * effectLevel, 4f * effectLevel);

        if (state != null) {
            switch (state) {
                case IN:

                    // set the target locations and angles
                    if (start == null) {
                        start = new Vector2f(ship.getLocation());
                        point = new Vector2f(ship.getMouseTarget());
                        startAngle = ship.getFacing();
                        targetAngle = VectorUtils.getAngle(start, point);
                    }

                    // blah blah stats
                    stats.getHullDamageTakenMult().modifyMult(id, DMG_TAKEN);
                    stats.getArmorDamageTakenMult().modifyMult(id, DMG_TAKEN);
                    stats.getEmpDamageTakenMult().modifyMult(id, 0f);
                    if (ship.getMass() == mass) {
                        ship.setMass(mass * MASS_MULT);
                    }

                    // spin to target
                    if (!Global.getCombatEngine().isPaused()) {
                        ship.setAngularVelocity(0f);
                        float angle = Misc.interpolate(startAngle, targetAngle, effectLevel);
                        ship.setFacing(angle);
                    }

                    break;

                case ACTIVE:

                    if (!Global.getCombatEngine().isPaused()) {
                        // stop if we're almost at the target
                        if (Misc.getDistance(ship.getLocation(), point) < STOP_RANGE) {
                            ship.getSystem().deactivate();
                        }

                        // move to point at $maxSpeed
                        ship.getVelocity().set(VectorUtils.getDirectionalVector(start, point));
                        ship.getVelocity().normalise();
                        ship.getVelocity().scale(1312f);

                        // stay on target
                        ship.setAngularVelocity(0f);
                        ship.setFacing(targetAngle);
                    }

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        start = null;
        point = null;

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

        final float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();
        final Vector2f velocity = ship.getVelocity();
        if (Global.getCombatEngine().isPaused()) {
        } else {
            if (velocity.lengthSquared() > (maxSpeed * maxSpeed)) {
                velocity.normalise();
                velocity.scale(maxSpeed);
            }
        }

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
        if (null != state) {
            switch (state) {
                case IN:
                    if (index == 1) {
                        return new StatusData("dehumanize yourself and face to bloodshed", false);
                    }
                    if (index == 0) {
                        return new StatusData("choo choo, here comes the night train", false);
                    }
                    break;
                case ACTIVE:
                    if (index == 1) {
                        return new StatusData("damn the torpedoes, full speed ahead!", false);
                    }
                    if (index == 0) {
                        return new StatusData("you are huge! that means you have huge guts!", false);
                    }
                    break;
                case OUT:
                    if (index == 1) {
                        return new StatusData("feelings of invincibility: over", false);
                    }
                    if (index == 0) {
                        return new StatusData("change of plans", false);
                    }
                    break;
                default:
                    break;
            }
        }
        return null;
    }
}
