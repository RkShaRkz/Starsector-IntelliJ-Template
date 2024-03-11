package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.ArrayList;
import java.util.List;

public class vayra_targeting_pods extends BaseHullMod {

    public static String POD_ID = "vayra_targeting_pod_p";
    public static String STATUS_TITLE = "Targeting Pods";
    public static String SPRITE_NAME = "graphics/icons/hullsys/drone_sensor.png";
    public static int BONUS_PER_POD = 150;

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) {
            return;
        }

        MutableShipStatsAPI stats = ship.getMutableStats();

        int podCount = 0;

        for (ShipAPI fighter : getFighters(ship)) {
            if (!fighter.isHulk()) {
                if (fighter.getHullSpec().getBaseHullId().equalsIgnoreCase(POD_ID)) {
                    podCount++;
                }
            }
        }

        float rangeBonus = (float) podCount * BONUS_PER_POD;
        stats.getBallisticWeaponRangeBonus().modifyFlat(this.toString(), rangeBonus);
        stats.getEnergyWeaponRangeBonus().modifyFlat(this.toString(), rangeBonus);
        
        if (Global.getCombatEngine().getPlayerShip().equals(ship) && rangeBonus > 100) {
            Global.getCombatEngine().maintainStatusForPlayerShip(this, SPRITE_NAME, STATUS_TITLE, getData(podCount), false);
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

    private String getData(int fighterCount) {
        String result = "+" + fighterCount * BONUS_PER_POD + " weapon range";
        return result;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

}
