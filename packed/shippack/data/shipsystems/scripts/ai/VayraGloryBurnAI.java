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

public class VayraGloryBurnAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;

    // only check every half-second (for optimization)
    private IntervalUtil timer = new IntervalUtil(0.5f, 0.5f);
    
    private static final ArrayList<AIFlags> pro = new ArrayList<>();
    private static final ArrayList<AIFlags> con = new ArrayList<>();
    static {
        pro.add(AIFlags.PURSUING);
        pro.add(AIFlags.HARASS_MOVE_IN);
        pro.add(AIFlags.RUN_QUICKLY);
        pro.add(AIFlags.TURN_QUICKLY);
        con.add(AIFlags.DO_NOT_PURSUE);
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
        
        for (AIFlags f : pro) {
            if (flags.hasFlag(f)) useMe = true;
        }
        
        for (AIFlags f : con) {
            if (flags.hasFlag(f)) useMe = false;
        }

        if (useMe) {
            ship.useSystem();
        }

    }
}
