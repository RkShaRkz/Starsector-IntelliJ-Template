package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

public class vayra_MartyrChannelStats extends BaseShipSystemScript {

    public static final Object KEY_JITTER = new Object();

    public static final String MARTYR_CHANNEL_BUFF_ID = "kadur_martyr_channel_buff";
    
    public static final float CHECK_RADIUS = 1000f;

    public static final float MAX_FIGHTER_DAMAGE_INCREASE_PERCENT = 100f;
    public static final float MAX_MANEUVERABILITY_INCREASE_PERCENT = 100f;
    public static final float MAX_SPEED_INCREASE_PERCENT = 50f;

    public static final Color JITTER_UNDER_COLOR = new Color(33, 106, 109, 255);
    public static final Color JITTER_COLOR = new Color(33, 106, 109, 255);
    
    private boolean useful;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (effectLevel <= 0f) {
            return;
        }
        
        useful = false;
        
        for (ShipAPI fighter : getFighters(ship)) {

            if (fighter.isHulk()) {
                continue;
            }

            Map<String, List<ShipAPI>> withinRange = getFightersWithinRange(fighter);
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            List<ShipAPI> enemies = withinRange.get("enemies");
            if (enemies.size() <= 1) {
                continue;
            }

            List<ShipAPI> allies = withinRange.get("allies");

            float ratio = (float) enemies.size() / (float) Math.max(1f, allies.size());
            ratio = Math.max(0f, ratio - 1f);
            float buffLevel = MathUtils.clamp(ratio, 0f, effectLevel);
            if (buffLevel <= 0f) {
                continue;
            }
            
            useful = true;

            fStats.getDamageToFighters().modifyPercent(MARTYR_CHANNEL_BUFF_ID, MAX_FIGHTER_DAMAGE_INCREASE_PERCENT * buffLevel);
            fStats.getAcceleration().modifyPercent(MARTYR_CHANNEL_BUFF_ID, MAX_MANEUVERABILITY_INCREASE_PERCENT * buffLevel);
            fStats.getDeceleration().modifyPercent(MARTYR_CHANNEL_BUFF_ID, MAX_MANEUVERABILITY_INCREASE_PERCENT * buffLevel);
            fStats.getMaxTurnRate().modifyPercent(MARTYR_CHANNEL_BUFF_ID, MAX_MANEUVERABILITY_INCREASE_PERCENT * buffLevel);
            fStats.getTurnAcceleration().modifyPercent(MARTYR_CHANNEL_BUFF_ID, MAX_MANEUVERABILITY_INCREASE_PERCENT * buffLevel);
            fStats.getMaxSpeed().modifyPercent(MARTYR_CHANNEL_BUFF_ID, MAX_SPEED_INCREASE_PERCENT * buffLevel);

            float maxRangeBonus = 20f;
            float jitterRangeBonus = buffLevel * maxRangeBonus;
            fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, buffLevel, 5, 0f, jitterRangeBonus);
            fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, buffLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
            Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
        }
    }

    private Map<String, List<ShipAPI>> getFightersWithinRange(ShipAPI source) {
        List<ShipAPI> enemies = new ArrayList<>();
        List<ShipAPI> allies = new ArrayList<>();
        Map<String, List<ShipAPI>> result = new HashMap<>();

        for (ShipAPI fighter : CombatUtils.getShipsWithinRange(source.getLocation(), CHECK_RADIUS)) {
            if (!fighter.isFighter()) {
                continue;
            }
            if (fighter.getWing() == null) {
                continue;
            }
            if (fighter.getWing().getSourceShip() == null) {
                continue;
            }
            if (fighter.getOwner() == source.getOwner()) {
                allies.add(fighter);
            } else {
                enemies.add(fighter);
            }
        }

        result.put("enemies", enemies);
        result.put("allies", allies);

        return result;
    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (fighter.isHulk()) {
                continue;
            }
            if (!fighter.isFighter()) {
                continue;
            }
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getArmorDamageTakenMult().unmodify(MARTYR_CHANNEL_BUFF_ID + fighter.getOwner());
            fStats.getHullDamageTakenMult().unmodify(MARTYR_CHANNEL_BUFF_ID + fighter.getOwner());
            fStats.getAcceleration().unmodify(MARTYR_CHANNEL_BUFF_ID + fighter.getOwner());
            fStats.getDeceleration().unmodify(MARTYR_CHANNEL_BUFF_ID + fighter.getOwner());
            fStats.getTurnAcceleration().unmodify(MARTYR_CHANNEL_BUFF_ID + fighter.getOwner());
            fStats.getMaxTurnRate().unmodify(MARTYR_CHANNEL_BUFF_ID + fighter.getOwner());
            fStats.getMaxSpeed().unmodify(MARTYR_CHANNEL_BUFF_ID + fighter.getOwner());
        }
    }

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (useful) {
            if (index == 1) {
                return new ShipSystemStatsScript.StatusData("outnumbered? i like those odds", false);
            }
            if (index == 0) {
                return new ShipSystemStatsScript.StatusData("i have the power of god and anime on my side", false);
            }
        } else {
            if (index == 1) {
                return new ShipSystemStatsScript.StatusData("what's the point? we outnumber them", false);
            }
            if (index == 0) {
                return new ShipSystemStatsScript.StatusData("mommy told me to always fight fair", false);
            }
        }
        return null;
    }

}
