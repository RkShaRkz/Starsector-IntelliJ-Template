package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class VayraDamagedAmmo extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        CompromisedStructure.modifyCost(hullSize, stats, id);

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats == null || engine == null || engine.isPaused()) {
            return;
        }

        for (DamagingProjectileAPI p : engine.getProjectiles()) {
            if (ship.equals(p.getSource()) && p.getWeapon() != null) {
                if (p.getDamageType() != DamageType.FRAGMENTATION 
                        && p.getDamageType() != DamageType.OTHER
                        && (p.getWeapon().getType() == WeaponType.BALLISTIC 
                        || p.getWeapon().getType() == WeaponType.MISSILE)) {
                    p.getDamage().setType(DamageType.FRAGMENTATION);
                }
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {

        if (index == 0) {
            return "Fragmentation";
        }
        if (index >= 1) {
            return CompromisedStructure.getCostDescParam(index, 1);
        }
        return null;
    }
}
