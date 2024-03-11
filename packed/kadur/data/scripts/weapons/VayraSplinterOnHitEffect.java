// Based on the sylphon Veritas script by Nicke535
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.scripts.plugins.VayraSplinterPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import java.util.HashMap;
import java.util.Map;

public class VayraSplinterOnHitEffect implements OnHitEffectPlugin {

    public static final float PROJ_TIME_TO_LIVE = 2.0f;
    
    private static final float PROJ_SPRITE_HEIGHT = 20f;
    private static final float PROJ_SPRITE_WIDTH = 7f;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (shieldHit || target == null) {
            return;
        }

        if (target instanceof ShipAPI) {
            ShipAPI targetShip = (ShipAPI) target;

            if (targetShip.isPiece() || targetShip.isHulk()) {
                return;
            }

            Vector2f pivot = VectorUtils.rotateAroundPivot(point, targetShip.getLocation(), -targetShip.getFacing(), new Vector2f(0f, 0f));
            pivot.x -= targetShip.getLocation().x;
            pivot.y -= targetShip.getLocation().y;
            
            Map<String, Object> splinterData = new HashMap<>();
            splinterData.put("ttl", PROJ_TIME_TO_LIVE);
            splinterData.put("width", PROJ_SPRITE_WIDTH);
            splinterData.put("height", PROJ_SPRITE_HEIGHT);
            splinterData.put("relativex", pivot.x);
            splinterData.put("relativey", pivot.y);
            splinterData.put("relativeangle", projectile.getFacing() - targetShip.getFacing() + MathUtils.getRandomNumberInRange(-2.5f, 2.5f));
            splinterData.put("basedmg", projectile.getDamageAmount());
            splinterData.put("explosiondmg", projectile.getDamageAmount());
            splinterData.put("source", projectile.getSource());
            VayraSplinterPlugin.addSplinter(splinterData, targetShip);
        }
    }
}
