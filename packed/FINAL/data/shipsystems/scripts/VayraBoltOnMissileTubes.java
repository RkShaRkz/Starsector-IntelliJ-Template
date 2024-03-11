package data.shipsystems.scripts;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lazywizard.lazylib.combat.CombatUtils;

public class VayraBoltOnMissileTubes extends BaseShipSystemScript {

    // boolean we switch on/off to make sure we don't spam swarmers
    private boolean fired = false;

    // setup, dunno how to get the weapon range oops so you gotta update it manually
    public static final String WEAPON_ID = "swarmer_fighter";
    public static final float RANGE = 1000f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {

        // setup, null checks
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        // if shipsystem not active, reset "fired" boolean
        if (effectLevel <= 0.5f) {
            fired = false;

            // if shipsystem IS active and we haven't fired yet...
        } else if (!fired) {

            // loop through our fighters
            for (ShipAPI fighter : getFighters(ship)) {

                // set up a boolean that will tell us to not bother firing a missile if nobody is in range
                boolean useful = false;

                // ignore our fighters that are dead, lol
                if (fighter.isHulk()) {
                    continue;
                }

                // set up a list of stuff within range of each fighter
                List<ShipAPI> withinRange = CombatUtils.getShipsWithinRange(fighter.getLocation(), RANGE);
                // loop through ships within range, looking for enemies
                for (ShipAPI pt : withinRange) {
                    // if we found an enemy, set "useful" to true
                    if (pt.getOwner() != fighter.getOwner()) {
                        useful = true;
                    }
                }

                // if we found at least one enemy within range, FIRE ZE MISSILES
                if (useful) {
                    engine.spawnProjectile(fighter, null, WEAPON_ID, fighter.getLocation(), fighter.getFacing(), fighter.getVelocity());
                    Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, fighter.getLocation(), fighter.getVelocity());
                    fired = true;
                }

            }
        }
    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("FIRE ZE MISSILES", false);
        }
        return null;
    }

}
