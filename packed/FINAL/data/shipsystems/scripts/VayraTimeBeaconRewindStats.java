package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import java.awt.Color;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class VayraTimeBeaconRewindStats extends BaseShipSystemScript {

    public static final float MAX_TIME = 15f;
    public static final String SOUND_ID = "tachyon_lance_emp_impact";

    private boolean active = false;
    private float activeTime = 0f;
    private final IntervalUtil arcTimer = new IntervalUtil(0.25f, 0.5f);

    private Vector2f storedLoc = Misc.ZERO;
    private Vector2f storedVel = Misc.ZERO;
    private float storedAngVel = 0f;
    private float storedFacing = 0f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || engine == null) {
            return;
        }

        Vector2f loc = ship.getLocation();

        if (!active) {
            if (state.equals(State.ACTIVE)) {
                storedLoc = new Vector2f(ship.getCopyLocation());
                storedVel = new Vector2f(ship.getVelocity());
                storedAngVel = ship.getAngularVelocity();
                storedFacing = ship.getFacing();

                /**
                 * @param color
                 * @param locX
                 * @param locY
                 * @param velX
                 * @param velY
                 * @param maxJitter
                 * @param in
                 * @param dur
                 * @param out
                 * @param additive
                 * @param combineWithSpriteColor
                 * @param aboveShip
                 */
                ship.addAfterimage(
                        Color.CYAN.brighter(),
                        storedLoc.getX(),
                        storedLoc.getY(),
                        0f,
                        0f,
                        MAX_TIME,
                        0f,
                        1f,
                        MAX_TIME - 1f,
                        true,
                        false,
                        true
                );
            }

        } else if (active && !engine.isPaused()) {
            float amount = engine.getElapsedInLastFrame();
            activeTime += amount;
            if (activeTime >= MAX_TIME) {
                active = false;
            }

            arcTimer.advance(amount);
            if (arcTimer.intervalElapsed()) {
                engine.spawnEmpArc(
                        ship,
                        loc,
                        ship,
                        new SimpleEntity(storedLoc),
                        DamageType.OTHER,
                        0f,
                        0f,
                        69420f,
                        null,
                        (MAX_TIME - activeTime) * 2f,
                        Color.CYAN.brighter().brighter(),
                        Color.CYAN.brighter()
                );
            }

            if (state.equals(State.ACTIVE)) {
                active = false;
                ship.getLocation().set(storedLoc);
                ship.getVelocity().set(storedVel);
                ship.setAngularVelocity(storedAngVel);
                ship.setFacing(storedFacing);
                if (ship.getShield() != null) {
                    ship.getShield().toggleOff();
                }
                float max = ship.getFluxTracker().getMaxFlux() * 0.9f;
                if (ship.getFluxTracker().getHardFlux() <= max) {
                    ship.getFluxTracker().setHardFlux(max);
                }
            }
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {

        if (active && index == 0) {
            return new StatusData("engines to maximum", false);
        }

        return null;
    }
}
