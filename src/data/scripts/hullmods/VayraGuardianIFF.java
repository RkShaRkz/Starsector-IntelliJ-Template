package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class VayraGuardianIFF extends BaseHullMod {

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSize() != HullSize.CAPITAL_SHIP);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null && ship.getHullSize() == HullSize.CAPITAL_SHIP) {
            return "Cannot be installed on capital ships";
        }

        return null;
    }
}
