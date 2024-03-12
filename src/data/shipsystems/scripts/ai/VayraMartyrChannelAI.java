package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import static data.shipsystems.scripts.vayra_MartyrChannelStats.CHECK_RADIUS;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import java.util.HashMap;
import java.util.Map;

public class VayraMartyrChannelAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    
    // use the system if at least one wing is locally outnumbered by at least 50%
    public static final float USE_THRESHOLD = 0.5f;

    // only check every little while (for optimization and staggering)
    private IntervalUtil timer = new IntervalUtil(0.2f, 0.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    private Map<String, List<ShipAPI>> getFightersWithinRange(ShipAPI source) {
        List<ShipAPI> enemies = new ArrayList<>();
        List<ShipAPI> allies = new ArrayList<>();
        Map<String, List<ShipAPI>> result = new HashMap<>();

        for (ShipAPI fighter : CombatUtils.getShipsWithinRange(source.getLocation(), CHECK_RADIUS)) {
            if (!fighter.isFighter()) {
                continue;
            }
            if (fighter.getWing() == null) {
                continue;
            }
            if (fighter.getWing().getSourceShip() == null) {
                continue;
            }
            if (fighter.getOwner() == source.getOwner()) {
                allies.add(fighter);
            } else {
                enemies.add(fighter);
            }
        }

        result.put("enemies", enemies);
        result.put("allies", allies);

        return result;
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

        float usefulness = 0f;

        for (ShipAPI fighter : getFighters(ship)) {

            if (fighter.isHulk()) {
                continue;
            }

            Map<String, List<ShipAPI>> withinRange = getFightersWithinRange(fighter);
            List<ShipAPI> enemies = withinRange.get("enemies");

            if (enemies.size() < 1) {
                continue;
            }

            List<ShipAPI> allies = withinRange.get("allies");

            float ratio = ((float) enemies.size() / (float) allies.size()) - 1f;
            float buffLevel = ratio > 1 ? 1 : ratio < 0 ? 0 : ratio; // clamp buffLevel between 1 and 0
            if (buffLevel > 0) {
                usefulness = Math.max(usefulness, (buffLevel / fighter.getWing().getWingMembers().size()));
            }
        }
        
        if (usefulness > USE_THRESHOLD) {
            ship.useSystem();
        }

    }
}
