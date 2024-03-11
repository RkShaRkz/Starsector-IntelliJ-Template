package data.missions.vayra_k006;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

@SuppressWarnings("unchecked")
public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        api.initFleet(FleetSide.PLAYER, "KHS", FleetGoal.ESCAPE, false, 10);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 20);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Kadur defense auxiliaries, Oasis parish");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Ground Assault Force with incoming heavy backup");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("The KHS-001 Hand of God must survive");
        api.addBriefingItem("Save as many personnel transports as you can");
        api.addBriefingItem("All other ships are expendable");

        // ships in fleets
        api.addToFleet(FleetSide.PLAYER, "vayra_ziz_support", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "vayra_sphinx_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_eagle_k_line", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_falcon_k_torpedo", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_disabler", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_antifighter", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_camel_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_rod", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mendicant_refugee", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mendicant_refugee", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mendicant_refugee", FleetMemberType.SHIP, false);

        FleetMemberAPI caliph = api.addToFleet(FleetSide.PLAYER, "vayra_caliph_revenant", FleetMemberType.SHIP, "KHS-001 Hand of God", false);
        caliph.getRepairTracker().setMothballed(true);
        api.defeatOnShipLoss("KHS-001 Hand of God");
        boolean vsp = Global.getSettings().getModManager().isModEnabled("vayrashippack");

        api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        if (vsp) {
            api.addToFleet(FleetSide.ENEMY, "vayra_henchman_s", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "vayra_henchman_l", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.ENEMY, "valkyrie_Elite", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "valkyrie_Elite", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "valkyrie_Elite", FleetMemberType.SHIP, false);
        }

        // Set up the map.
        float width = 12000f;
        float height = 19000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 2000);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2000);

        // Add objectives
        api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.55f, "nav_buoy");
        api.addObjective(minX + width * 0.4f + 500, minY + height * 0.75f, "sensor_array");
        api.addObjective(minX + width * 0.6f - 500, minY + height * 0.65f, "nav_buoy");
        api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.85f, "sensor_array");

        // Add two big nebula clouds
        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2500);
        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 1500);

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(0, 0, 400f, "jungle", 350f, true);

        BattleCreationContext context = new BattleCreationContext(null, null, null, null);
        context.setInitialEscapeRange(7000f);
        api.addPlugin(new EscapeRevealPlugin(context));

        // Add custom plugin
        api.addPlugin(new Plugin());
    }

    public final static class Plugin extends BaseEveryFrameCombatPlugin {

        private static boolean runOnce = false;
        private static int wave = 0;

        private static final IntervalUtil TIMER = new IntervalUtil(40f, 60f);

        private static final Vector2f BOT_LEFT = new Vector2f();
        private static final Vector2f BOT_RIGHT = new Vector2f();
        private static final Vector2f MID_LEFT = new Vector2f();
        private static final Vector2f MID_RIGHT = new Vector2f();
        private static final List<Vector2f> LOCS = new ArrayList(Arrays.asList(
                BOT_LEFT,
                BOT_RIGHT,
                MID_LEFT,
                MID_RIGHT));

        private static final WeightedRandomPicker<String> FF = new WeightedRandomPicker<>();
        private static final WeightedRandomPicker<String> DD = new WeightedRandomPicker<>();
        private static final WeightedRandomPicker<String> CA = new WeightedRandomPicker<>();
        private static final WeightedRandomPicker<String> BB = new WeightedRandomPicker<>();
        private static final WeightedRandomPicker<String> CIV = new WeightedRandomPicker<>();
        private static final WeightedRandomPicker<String> CIV_NAME = new WeightedRandomPicker<>();

        @Override
        public void init(CombatEngineAPI engine) {
            runOnce = false;
            wave = 0;

            BOT_LEFT.set(-(engine.getMapWidth() / 2f), -(engine.getMapHeight() / 2f));
            BOT_RIGHT.set((engine.getMapWidth() / 2f), -(engine.getMapHeight() / 2f));
            MID_LEFT.set(-(engine.getMapWidth() / 2f), 0f);
            MID_RIGHT.set((engine.getMapWidth() / 2f), 0f);

            FF.add("hound_hegemony_Standard", 2f);
            FF.add("kite_hegemony_Interceptor", 2f);
            FF.add("lasher_CS", 0.25f);
            FF.add("lasher_Strike", 0.25f);
            FF.add("lasher_Assault", 0.25f);
            FF.add("centurion_Assault", 0.25f);
            FF.add("brawler_Assault", 0.25f);

            DD.add("enforcer_XIV_Elite");
            DD.add("enforcer_Elite", 0.25f);
            DD.add("enforcer_Assault", 0.25f);
            DD.add("enforcer_Balanced", 0.25f);
            DD.add("enforcer_CS", 0.25f);
            DD.add("hammerhead_Support", 0.25f);
            DD.add("hammerhead_Balanced", 0.25f);
            DD.add("sunder_Assault", 0.25f);
            DD.add("sunder_CS", 0.25f);
            DD.add("condor_Attack", 0.5f);
            DD.add("condor_Strike", 0.5f);
            DD.add("condor_Support", 0.5f);
            DD.add("vayra_hegbinger_s");

            CA.add("dominator_XIV_Elite");
            CA.add("dominator_Assault", 0.25f);
            CA.add("dominator_Support", 0.25f);
            CA.add("dominator_AntiCV", 0.25f);
            CA.add("dominator_Outdated", 0.25f);
            CA.add("vayra_subjugator_a", 0.5f);
            CA.add("vayra_subjugator_s", 0.5f);
            CA.add("eagle_xiv_Elite");
            CA.add("eagle_Assault", 0.25f);
            CA.add("eagle_Balanced", 0.25f);
            CA.add("falcon_xiv_Elite");
            CA.add("falcon_xiv_Escort");
            CA.add("falcon_Attack", 0.25f);
            CA.add("falcon_CS", 0.25f);
            CA.add("mora_Assault", 0.25f);
            CA.add("mora_Strike", 0.25f);
            CA.add("mora_Support", 0.25f);
            CA.add("gryphon_FS", 0.25f);
            CA.add("gryphon_Standard", 0.25f);
            CA.add("heron_Attack", 0.5f);
            CA.add("heron_Strike", 0.5f);

            BB.add("onslaught_xiv_Elite", 0.5f);
            BB.add("onslaught_Standard", 0.5f);
            BB.add("onslaught_Outdated", 0.5f);

            CIV.add("mudskipper_Standard");
            CIV.add("valkyrie_Elite");
            CIV.add("nebula_Standard");
            CIV.add("starliner_Standard");
            CIV.add("vayra_mendicant_refugee");

            boolean swp = Global.getSettings().getModManager().isModEnabled("swp");

            if (swp) {
                FF.add("swp_alastor_xiv_eli");
                FF.add("swp_alastor_ass", 0.25f);
                FF.add("swp_alastor_eli", 0.25f);
                FF.add("swp_alastor_sta", 0.25f);
                FF.add("swp_brawler_hegemony_ass");
                FF.add("swp_lasher_xiv_eli");

                DD.add("swp_hammerhead_xiv_eli");
                DD.add("swp_sunder_xiv_eli");

                CA.add("swp_gryphon_xiv_eli", 0.5f);
                CA.add("swp_vindicator_o_sta", 0.5f);
            }
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {

            CombatEngineAPI engine = Global.getCombatEngine();

            if (CIV_NAME.isEmpty()) {
                CIV_NAME.add("ISS The Great Escape");
                CIV_NAME.add("ISS Get In The Car We're Leaving");
                CIV_NAME.add("ISS Nothing Ventured");
                CIV_NAME.add("ISS Squalor Queen");
                CIV_NAME.add("ISS Philanthropist");
                CIV_NAME.add("ISS Escape From New York");
                CIV_NAME.add("ISS Runaway");
                CIV_NAME.add("ISS Tactical Retreat");
                CIV_NAME.add("ISS Coward's Luck");
                CIV_NAME.add("ISS Dove");
                CIV_NAME.add("ISS Dublin Again");
                CIV_NAME.add("ISS Kangaroo");
                CIV_NAME.add("ISS Mother Bear");
                CIV_NAME.add("ISS Messiah Complex");
                CIV_NAME.add("ISS White Knight");
                CIV_NAME.add("ISS Rock Me Mama");
            }

            if (!engine.isPaused()) {
                TIMER.advance(amount);
            }

            if (!runOnce) {
                TIMER.forceIntervalElapsed();
                runOnce = true;
                for (ShipAPI ship : engine.getShips()) {

                    // blow up Caliph shield generator
                    if (ship.getHullSpec().getHullId().equals("vayra_caliph_shieldgenerator") && ship.isAlive()) {
                        ship.setHitpoints(1f);
                        engine.applyDamage(
                                ship,
                                ship.getLocation(),
                                4000f,
                                DamageType.HIGH_EXPLOSIVE,
                                0,
                                true,
                                false,
                                null);
                    }

                    // disable all Caliph guns
                    if (((ship.getParentStation() != null && ship.getParentStation().getHullSpec().getHullId().equals("vayra_caliph"))
                            || ship.getHullSpec().getHullId().equals("vayra_caliph")) && ship.isAlive()) {
                        for (WeaponAPI weapon : ship.getAllWeapons()) {
                            if (!weapon.isPermanentlyDisabled()) {
                                engine.applyDamage(
                                        ship,
                                        weapon.getLocation(),
                                        50f,
                                        DamageType.HIGH_EXPLOSIVE,
                                        0,
                                        true,
                                        false,
                                        null);
                                weapon.disable(true);
                            }
                        }
                    }
                }
            }

            if (TIMER.intervalElapsed()) {
                Global.getSoundPlayer().playUISound("cr_allied_critical", 0.77f, 10f);
                wave++;
                Vector2f botMid = new Vector2f(0f, -(engine.getMapHeight() / 2f));
                ShipAPI civ = CombatUtils.spawnShipOrWingDirectly(
                        CIV.pick(),
                        FleetMemberType.SHIP,
                        FleetSide.PLAYER,
                        0.3f,
                        botMid,
                        90f);
                civ.getFleetMember().setShipName(CIV_NAME.pickAndRemove());
                civ.setAlly(true);
                civ.setRetreating(true, true);
                civ.setControlsLocked(true);
                for (Vector2f loc : LOCS) {
                    if (wave % 2f == 0f) {
                        List<String> toSpawn = new ArrayList<>();
                        for (int i = 0; i < wave; i++) {
                            for (int ff = 0; ff < wave / 2; ff++) {
                                toSpawn.add(FF.pick());
                            }
                            for (int dd = 0; dd < wave / 3; dd++) {
                                toSpawn.add(DD.pick());
                            }
                            for (int ca = 0; ca < wave / 4; ca++) {
                                toSpawn.add(CA.pick());
                            }
                            for (int bb = 0; bb < wave / 5; bb++) {
                                toSpawn.add(BB.pick());
                            }
                        }
                        for (String var : toSpawn) {
                            Vector2f point = MathUtils.getRandomPointInCircle(loc, 2000f);
                            ShipAPI enemy = CombatUtils.spawnShipOrWingDirectly(
                                    var,
                                    FleetMemberType.SHIP,
                                    FleetSide.ENEMY,
                                    0.6f,
                                    point,
                                    VectorUtils.getAngle(point, engine.getPlayerShip().getLocation()));
                            enemy.setOriginalOwner(1);
                            enemy.setOwner(1);
                        }
                    }
                }
            }
        }
    }
}
