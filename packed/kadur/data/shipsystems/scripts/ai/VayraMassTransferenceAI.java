package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import static data.shipsystems.scripts.vayra_MartyrChannelStats.MARTYR_CHANNEL_BUFF_ID;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import static data.shipsystems.scripts.vayra_MassTransferenceFieldStats.EFFECT_RADIUS;
import java.util.HashMap;
import java.util.Map;

public class VayraMassTransferenceAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // don't need to check *every* frame, just a few times per second is good enough...
    private IntervalUtil timer = new IntervalUtil(0.25f, 0.5f);
           
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }
    
    private Map<String, List<ShipAPI>> getFightersWithinRange(ShipAPI carrier) {
        List<ShipAPI> enemies = new ArrayList<>();
        List<ShipAPI> allies = new ArrayList<>();
        Map<String, List<ShipAPI>> result = new HashMap<>();

        for (ShipAPI fighter : CombatUtils.getShipsWithinRange(carrier.getLocation(), EFFECT_RADIUS)) {
            if (!fighter.isFighter()) {
                continue;
            }
            if (fighter.getWing() == null) {
                continue;
            }
            if (fighter.getWing().getSourceShip() == null) {
                continue;
            }
            if (fighter.getWing().getSourceShip().equals(carrier)) {
                continue;
            }
            if (fighter.getMutableStats().getAcceleration().getPercentMods().keySet().contains(MARTYR_CHANNEL_BUFF_ID + carrier.getOwner())) {
                continue;
            }
            if (fighter.getOwner() == carrier.getOwner()) {
                allies.add(fighter);
            } else {
                enemies.add(fighter);
            }
        }
        
        result.put("enemies", enemies);
        result.put("allies", allies);

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

        boolean useMe = false;
        
        List<ShipAPI> enemies = getFightersWithinRange(ship).get("enemies");
        List<ShipAPI> allies = getFightersWithinRange(ship).get("allies");
        
        if (enemies.size() > 0 && enemies.size() >= allies.size()) {
            useMe = true;
        }

        if (useMe) {
            ship.useSystem();
        }

    }
}
