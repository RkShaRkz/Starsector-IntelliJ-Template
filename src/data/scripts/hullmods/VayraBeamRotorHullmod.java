package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VayraBeamRotorHullmod extends BaseHullMod {

    private final List<ShipAPI> deadChopperStorage = new ArrayList<>();

    private static final float ROTOR_FLING_CHANCE = 1f;
    private static final String ROTOR_FLING_SOUND = "ui_refit_slot_cleared_small";
    private static final String ROTOR_WEAPON_ID = "vayra_rotor_shrapnel";

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        if (!deadChopperStorage.isEmpty()) {
            deadChopperStorage.clear();
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (stats == null || engine == null || engine.isPaused()) {
            return;
        }

        if (!ship.isAlive() && !deadChopperStorage.contains(ship)) {

            deadChopperStorage.add(ship);
            float roll = (float) Math.random();
            float chance = ROTOR_FLING_CHANCE;

            if (roll < chance) {
                Vector2f loc = ship.getLocation();
                Vector2f vel = ship.getVelocity();
                Global.getSoundPlayer().playSound(ROTOR_FLING_SOUND, 1f, 0.75f, loc, vel);
                engine.spawnExplosion(loc, vel, new Color(255, 150, 0, 255), 100, 0.25f);
                MissileAPI rotor = (MissileAPI) engine.spawnProjectile(ship, null, ROTOR_WEAPON_ID, loc, (float) (Math.random() * 360f), vel);
                rotor.setArmingTime(0f);
                rotor.setArmedWhileFizzling(true);
                rotor.setAngularVelocity(666f);
            }
        }
    }
}
