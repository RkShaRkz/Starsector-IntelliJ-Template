package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.util.Iterator;
import java.util.List;

public class VayraCaliphPDAI extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getRecoilPerShotMult().modifyMult(id, 0.5f);
        stats.getRecoilDecayMult().modifyMult(id, 2f);
        stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
        stats.getDynamic().getMod(Stats.PD_BEST_TARGET_LEADING).modifyFlat(id, 1f);
    }

    // makes all nonlarge nonmissile weapons into PD because fuck you that's why
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        List<WeaponAPI> weapons = ship.getAllWeapons();
        Iterator<WeaponAPI> iter = weapons.iterator();
        while (iter.hasNext()) {
            WeaponAPI weapon = iter.next();
            boolean large = weapon.getSize() == WeaponAPI.WeaponSize.LARGE;
            if (!large && weapon.getType() != WeaponAPI.WeaponType.MISSILE) {
                weapon.setPD(true);
            }
        }
    }
}
