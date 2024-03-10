package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class vayra_DamperShieldStats extends BaseShipSystemScript {

    private static final Map<HullSize, Float> mag = new HashMap<>();

    static {
        mag.put(HullSize.FIGHTER, 0.5f);
        mag.put(HullSize.FRIGATE, 0.2f);
        mag.put(HullSize.DESTROYER, 0.3f);
        mag.put(HullSize.CRUISER, 0.4f);
        mag.put(HullSize.CAPITAL_SHIP, 0.5f);
    }

    protected Object STATUSKEY1 = new Object();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float mult = mag.get(ShipAPI.HullSize.CRUISER);
        if (stats.getVariant() != null) {
            mult = mag.get(stats.getVariant().getHullSize());
        }
        stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);

        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        }
        if (player && effectLevel > 0f) {
            ShipSystemAPI system = ship.getPhaseCloak();
            if (system != null) {
                float percent = (1f - mult) * effectLevel * 100;
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                        system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                        Math.round(percent) + "% less damage taken", false);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
    }
}
