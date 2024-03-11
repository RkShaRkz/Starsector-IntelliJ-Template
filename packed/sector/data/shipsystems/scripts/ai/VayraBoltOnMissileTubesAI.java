package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import static data.shipsystems.scripts.VayraBoltOnMissileTubes.RANGE;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraBoltOnMissileTubesAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // only check every little while (for optimization and staggering)
    private final IntervalUtil timer = new IntervalUtil(0.2f, 0.5f);
    
    // only FIRE ZE MISSILES if at least this fraction of our fighters are within range of something
    private final float USEFUL_FRACTION = 0.5f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused()) {
            return;
        }

        timer.advance(amount);

        if (!timer.intervalElapsed()) {
            return;
        }
        if (!AIUtils.canUseSystemThisFrame(ship)) {
            return;
        }

        // get all our fighters, we're gonna use this later
        List<ShipAPI> fighters = getFighters(ship);
        // also set up a count of fighters within range of enemies
        float fightersInRange = 0;

        // loop through our fighters
        for (ShipAPI fighter : fighters) {

            // ignore our fighters that are dead, lol
            if (fighter.isHulk()) {
                continue;
            }

            // set up a list of stuff within range of each fighter
            List<ShipAPI> withinRange = CombatUtils.getShipsWithinRange(fighter.getLocation(), RANGE);
            // loop through ships within range, looking for enemies
            for (ShipAPI pt : withinRange) {
                // if we found an enemy, increment the counter of fighters within range
                if (pt.getOwner() != fighter.getOwner()) {
                    fightersInRange++;
                }
            }
        }

        // if at least USEFUL_FRACTION of our fighters are within range of an enemy, FIRE ZE MISSILES
        if ((fightersInRange / (float) fighters.size()) > USEFUL_FRACTION) {
            ship.useSystem();
        }

    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (!fighter.isFighter()) {
                continue;
            }
            if (fighter.getWing() == null) {
                continue;
            }
            if (fighter.getWing().getSourceShip() == carrier) {
                result.add(fighter);
            }
        }

        return result;
    }
}
