// significant parts of the below are made of tomatopaste
package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import java.util.ArrayList;
import java.util.List;

public class VayraGunshipHandlerPlugin extends BaseEveryFrameCombatPlugin {

    private CombatEngineAPI engine;

    //////////////////////////////BITS//////////////////////////////
    public static class GunshipData {

        public ShipAPI ship;
        public ShipAPI carrier;
        public ShipAPI target;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) {
            return;
        }

        List<DamagingProjectileAPI> projToRemove = new ArrayList<>();

        // catch all projectiles and loop through them
        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            String projId = proj.getProjectileSpecId();

        }
    }
}
