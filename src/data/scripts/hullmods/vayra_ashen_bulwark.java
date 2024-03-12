package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class vayra_ashen_bulwark extends BaseHullMod {

    // regular hullmod stuff
    private static final float ARMOR_BONUS = 100f;
    private static final float SUPPLY_MULT = 1.75f;

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) ARMOR_BONUS;
        }
        if (index == 1) {
            return "" + (int) MSL_DMG_THRESHOLD;
        }
        if (index == 2) {
            return (int) ((1 - MSL_DMG_MULT) * 100) + "%";
        }
        if (index == 3) {
            return "" + (int) ((MSL_DMG_MAX - MSL_DMG_THRESHOLD) * (1 - MSL_DMG_MULT));
        }
        if (index == 4) {
            return 1 + " incoming missile";
        }
        if (index == 5) {
            return ARC_TIMER + " seconds";
        }
        if (index == 6) {
            return "+" + (int) ((SUPPLY_MULT - 1) * 100) + "%";
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyFlat(id, ARMOR_BONUS);
        stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_MULT);
        arcTimers.clear();
        missiles.clear();
    }

    // HAMMERSHIELD stuff
    private static final float MSL_RANGE = 100f; // range in units ahead of the missile to scan for collision

    // arc stuff
    private static final float ARC_TIMER = 4f; // timer in seconds

    // oh god
    private final Map<ShipAPI, Float> arcTimers = new HashMap<>();
    private final Map<MissileAPI, Float> missiles = new HashMap<>();

    private static final float ARC_DAMAGE = 400f;
    private static final float ARC_EMP = 500f;
    private static final float ARC_WIDTH = 25f;

    // missile weakening stuff
    private static final float MSL_DMG_THRESHOLD = 500f;
    private static final float MSL_DMG_MAX = 2000f;
    private static final float MSL_DMG_MULT = 0.25f;

    // status effect stuff (sprite loaded in settings.json)
    private static final String BULWARK_ICON = Global.getSettings().getSpriteName("vayra_ashen_bulwark_icon", "1");
    private static final String BULWARK_TITLE = "Ludd's Ward";
    private static final String BULWARK_TEXT = "no weapon formed against you shall prosper";

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        // blah blahhh setup, null checks
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || engine == null) {
            return;
        }

        // set a timer if we don't have one
        // decrement the timer
        // store the timer
        float arcTimer;
        if (arcTimers.get(ship) == null) {
            arcTimer = ARC_TIMER;
        } else {
            arcTimer = arcTimers.get(ship);
        }
        arcTimer -= amount;
        if (arcTimer < 0) {
            arcTimer = 0;
        }

        arcTimers.put(ship, arcTimer);

        if (ship.equals(engine.getPlayerShip())) {
            if (arcTimer <= 0) {
                engine.maintainStatusForPlayerShip(this, BULWARK_ICON, BULWARK_TITLE, BULWARK_TEXT, false);
            }
        }

        // more setup, for missile stuff this time
        Vector2f location = ship.getLocation();
        float radius = ship.getCollisionRadius() + MSL_RANGE;

        // check for missiles
        for (MissileAPI m : CombatUtils.getMissilesWithinRange(location, radius)) {
            if (m.getOwner() != ship.getOwner()
                    && !missiles.containsKey(m)
                    && closeEnough(m, ship, amount)) {
                missiles.put(m, m.getDamageAmount());
                missileEffect(m, engine, ship);
            }
        }
    }

    private boolean closeEnough(MissileAPI m, ShipAPI ship, float amount) {
        Vector2f loc = m.getLocation();
        Vector2f vel = m.getVelocity();
        float dist = m.getCollisionRadius() + (m.getMoveSpeed() * amount) + MSL_RANGE;
        float angle = VectorUtils.getAngle(loc, vel);
        Vector2f move = MathUtils.getPointOnCircumference(loc, dist, angle);
        return CollisionUtils.isPointWithinBounds(move, ship);
    }

    public void missileEffect(MissileAPI missile, CombatEngineAPI engine, ShipAPI ship) {

        // reduce missile damage
        float base = missiles.get(missile);
        float overflow = base - MSL_DMG_MAX;
        float reduce = Math.min(base - MSL_DMG_THRESHOLD, MSL_DMG_MAX - MSL_DMG_THRESHOLD);

        if (reduce > 0) {
            reduce *= MSL_DMG_MULT;
            if (overflow < 0) {
                overflow = 0;
            }
            missile.setDamageAmount(MSL_DMG_THRESHOLD + reduce + overflow);
        }

        // get the timer, should be safe from nulls since we just checked in advanceInCombat()
        float arcTimer = arcTimers.get(ship);

        // arc if the timer's out
        if (arcTimer <= 0) {
            Vector2f mloc = missile.getLocation();

            engine.spawnEmpArcPierceShields(
                    ship, // damage source
                    CollisionUtils.getNearestPointOnBounds(mloc, ship), // start point
                    ship, // entity for start point to move with
                    missile, // target
                    DamageType.FRAGMENTATION, // damage type
                    ARC_DAMAGE, // damage
                    ARC_EMP, // emp
                    69420f, // max range
                    "tachyon_lance_emp_impact", // sound id
                    ARC_WIDTH, // arc width
                    Color.orange, // fringe color
                    Color.green.brighter()); // core color

            // reset the timer if we arc'd
            arcTimer = ARC_TIMER;
            arcTimers.put(ship, arcTimer);
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

}
