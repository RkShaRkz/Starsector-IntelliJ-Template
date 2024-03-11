package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import java.awt.Color;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lwjgl.util.vector.Vector2f;

public class VayraGloryBurnStats extends BaseShipSystemScript {

    public static final float SPEED_BONUS = 60f;            // flat bonus to top speed (x2 for accel/decel)
    public static final float TURN_BONUS = 80f;             // flat bonus to max turn rate (x2 for turn accel)
    public static final float ROF_BONUS = 1f;               // bonus rate of fire multiplier i.e. 1f = +100% rate of fire
    public static final float FLUX_MULT = 0.5f;             // multiplier for flux cost

    public static final Color COLOR = new Color(255, 245, 235, 255);    // engine flare color

    public static final float JUMP_VELOCITY = 666666f;      // gotta go fast
    public static final float JUMP_LENGTH = 0.5f;           // in seconds

    private boolean jumped = false;
    private Vector2f storedVel = null;
    private float timer;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || engine == null) {
            return;
        }

        ship.getEngineController().fadeToOtherColor(this, COLOR, COLOR.darker().darker().darker().darker(), effectLevel, 1f);
        ship.getEngineController().extendFlame(this, 1.25f * effectLevel, 1.25f * effectLevel, 1.5f * effectLevel);

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
            jumped = false;
        } else {

            stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
            stats.getAcceleration().modifyFlat(id, SPEED_BONUS * 2f * effectLevel);
            stats.getDeceleration().modifyFlat(id, SPEED_BONUS * 2f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * 2f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, TURN_BONUS * effectLevel);
            stats.getBallisticRoFMult().modifyMult(id, 1f + (ROF_BONUS * effectLevel));
            stats.getEnergyRoFMult().modifyMult(id, 1f + (ROF_BONUS * effectLevel));
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_MULT);

            if (!jumped) {
                storedVel = new Vector2f(ship.getVelocity());
                Vector2f vel = new Vector2f(0, 0);
                if (ship.getEngineController().isAccelerating()) {
                    vel.y += 1f;
                }
                if (ship.getEngineController().isDecelerating() || ship.getEngineController().isAcceleratingBackwards()) {
                    vel.y -= 1f;
                }
                if (ship.getEngineController().isStrafingLeft()) {
                    vel.x -= 1f;
                }
                if (ship.getEngineController().isStrafingRight()) {
                    vel.x += 1f;
                }
                vel.normalise();
                vel.scale(JUMP_VELOCITY);
                ship.getVelocity().set(vel);
                timer = JUMP_LENGTH;
            } else if (storedVel != null && timer > 0f) {
                timer -= engine.getElapsedInLastFrame();
            } else if (timer <= 0f) {
                ship.getVelocity().set(storedVel);
                storedVel = null;
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {

        if (index == 1) {
            return new StatusData("engines to maximum", false);
        }
        if (index == 1) {
            return new StatusData("weapons systems overcharged", false);
        }

        if (state != null) {
            switch (state) {
                case IN:
                    if (index == 0) {
                        return new StatusData("witness me", false);
                    }
                    break;
                case ACTIVE:
                    if (index == 0) {
                        return new StatusData("if i die, i historic on the fury road", false);
                    }
                    break;
                case OUT:
                    if (index == 0) {
                        return new StatusData("i live, i die, i live again", false);
                    }
                    break;
                case COOLDOWN:
                    break;
                case IDLE:
                    break;
                default:
                    break;
            }
        }
        return null;
    }
}
