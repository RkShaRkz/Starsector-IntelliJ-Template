package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraFluxOverdriveAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;

    // only check every tenth-second (for optimization and, hopefully, synchronization)
    private final IntervalUtil timer = new IntervalUtil(0.1f, 0.1f);
    
    private static final ArrayList<AIFlags> PRO = new ArrayList<>();
    private static final ArrayList<AIFlags> CON = new ArrayList<>();
    static {
        PRO.add(AIFlags.PURSUING);
        PRO.add(AIFlags.HARASS_MOVE_IN);
        PRO.add(AIFlags.RUN_QUICKLY);
        PRO.add(AIFlags.TURN_QUICKLY);
        PRO.add(AIFlags.BACK_OFF);
        PRO.add(AIFlags.BACK_OFF_MIN_RANGE);
        PRO.add(AIFlags.BACKING_OFF);
        PRO.add(AIFlags.SAFE_VENT);
        PRO.add(AIFlags.SAFE_FROM_DANGER_TIME);
        PRO.add(AIFlags.DO_NOT_USE_SHIELDS);
        PRO.add(AIFlags.DO_NOT_USE_FLUX);
        CON.add(AIFlags.DO_NOT_VENT);
        CON.add(AIFlags.DO_NOT_PURSUE);
        CON.add(AIFlags.KEEP_SHIELDS_ON);
    }
        
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
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

        boolean useMe = false;
        
        AssignmentInfo assignment = engine.getFleetManager(ship.getOwner()).getTaskManager(ship.isAlly()).getAssignmentFor(ship);

        if (assignment != null && assignment.getType() == CombatAssignmentType.RETREAT) {
            useMe = true;
        } 
        
        for (AIFlags f : PRO) {
            if (flags.hasFlag(f)) useMe = true;
        }
        
        for (AIFlags f : CON) {
            if (flags.hasFlag(f)) useMe = false;
        }

        if (useMe) {
            ship.useSystem();
        }

    }
}
