package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static data.scripts.VayraShipPackModPlugin.getFighters;
import java.awt.Color;

public class VayraScreeningFormation extends BaseShipSystemScript {

    public static final float ENGAGEMENT_RANGE_MULT = 0.25f;
    public static final float DAMAGE_TAKEN_MULT = 0.5f;
    public static final float DAMAGE_BONUS_PERCENT = 100f; // bonus damage to missiles and enemy fighters

    public static final Color JITTER_COLOR = new Color(255, 165, 90, 50);
    public static final Color JITTER_UNDER_COLOR = new Color(255, 165, 90, 150);

    public static final Color SHIELD_RING_COLOR = new Color(255, 255, 255, 255);
    public static final Color SHIELD_INNER_COLOR = new Color(255, 100, 255, 75);

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
        float defenseMult = 1f - (DAMAGE_TAKEN_MULT * effectLevel);
        float attackMult = DAMAGE_BONUS_PERCENT * effectLevel;

        stats.getFighterWingRange().modifyMult(id, ENGAGEMENT_RANGE_MULT);

        for (ShipAPI fighter : getFighters(ship)) {

            if (fighter.isHulk()) {
                continue;
            }

            MutableShipStatsAPI fStats = fighter.getMutableStats();

            fStats.getShieldDamageTakenMult().modifyMult(id, defenseMult);
            fStats.getArmorDamageTakenMult().modifyMult(id, defenseMult);
            fStats.getHullDamageTakenMult().modifyMult(id, defenseMult);
            fStats.getEmpDamageTakenMult().modifyMult(id, defenseMult);

            fStats.getShieldArcBonus().modifyFlat(id, 360f);

            fStats.getDamageToFighters().modifyPercent(id, attackMult);
            fStats.getDamageToMissiles().modifyPercent(id, attackMult);

            fighter.setJitterUnder(id, JITTER_UNDER_COLOR, effectLevel, 5, 0f, 5f);
            fighter.setJitter(id, JITTER_COLOR, effectLevel, 2, 0f, 7f);

            ShieldAPI shield = fighter.getShield();
            if (shield != null && (shield.getType() == ShieldType.FRONT || shield.getType() == ShieldType.OMNI)) {
                shield.setRingColor(SHIELD_RING_COLOR);
                shield.setInnerColor(SHIELD_INNER_COLOR);
            }

            Global.getSoundPlayer().playLoop("system_damper_loop", ship, 1f, effectLevel * 0.5f, fighter.getLocation(), fighter.getVelocity());
            Global.getSoundPlayer().playLoop("system_fortress_shield_loop", ship, 1f, effectLevel * 0.5f, fighter.getLocation(), fighter.getVelocity());
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        stats.getFighterWingRange().unmodify(id);

        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (fighter.isHulk()) {
                continue;
            }
            if (!fighter.isFighter()) {
                continue;
            }
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getShieldDamageTakenMult().unmodify(id);
            fStats.getArmorDamageTakenMult().unmodify(id);
            fStats.getHullDamageTakenMult().unmodify(id);
            fStats.getEmpDamageTakenMult().unmodify(id);

            fStats.getShieldArcBonus().unmodify(id);

            fStats.getDamageToFighters().unmodify(id);
            fStats.getDamageToMissiles().unmodify(id);
        }
    }

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("-75% engagement range", false);
        }
        if (index == 1) {
            return new ShipSystemStatsScript.StatusData("-" + (int) (DAMAGE_TAKEN_MULT * effectLevel * 100f) + "% damage taken by fighters", false);
        }
        if (index == 2) {
            return new ShipSystemStatsScript.StatusData("+" + (int) (DAMAGE_BONUS_PERCENT * effectLevel) + "% fighter damage dealt to fighters and missiles", false);
        }
        return null;
    }

}
