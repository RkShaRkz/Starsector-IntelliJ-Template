package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraScreeningFormationAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private float avgBaseRange = -1f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused() || target == null || !AIUtils.canUseSystemThisFrame(ship)) {
            return;
        }

        if (avgBaseRange < 0f) {
            float totalRange = 0f;
            for (FighterWingAPI wing : ship.getAllWings()) {
                totalRange += wing.getRange();
            }
            avgBaseRange = totalRange / ship.getVariant().getFittedWings().size();
        }

        if (target.getOwner() != ship.getOwner()
                && !ship.getSystem().isActive()
                && Misc.getDistance(ship.getLocation(), target.getLocation()) < avgBaseRange) {
            ship.useSystem(); // toggle on
        } else if (ship.getSystem().isActive()
                && AIUtils.getNearbyEnemies(ship, avgBaseRange).isEmpty()) {
            ship.useSystem(); // toggle off
        }
    }
}
