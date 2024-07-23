package data.missions.vayra_k006;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
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
        api.setFleetTagline(FleetSide.PLAYER, "Surviving Kadur defense auxiliaries rallying with Oasis parish");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Ground Assault Force with incoming heavy backup");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("The KHS-001 Hand of God must survive.");
        api.addBriefingItem("Save as many personnel transports as you can");
        api.addBriefingItem("All other ships are expendable");

        // ships in fleets
        api.addToFleet(FleetSide.PLAYER, "vayra_ziz_support", FleetMemberType.SHIP, true).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));;
        api.addToFleet(FleetSide.PLAYER, "vayra_sphinx_assault", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_eagle_k_line", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_falcon_k_torpedo", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_fs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_disabler", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_antifighter", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_camel_assault", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_fs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_rod", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_mendicant_refugee", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mendicant_refugee", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mendicant_refugee", FleetMemberType.SHIP, false);

        FleetMemberAPI caliph = api.addToFleet(FleetSide.PLAYER, "vayra_caliph_revenant", FleetMemberType.SHIP, "KHS-001 Hand of God", false);
        caliph.getRepairTracker().setMothballed(true);
        PersonAPI officer = Global.getSettings().createPerson();
        officer.setPortraitSprite(OfficerManagerEvent.pickPortrait(Global.getSector().getFaction("kadur_remnant"), (new Random().nextFloat() >= 0.5f) ? FullName.Gender.MALE : FullName.Gender.FEMALE));
        officer.getStats().setLevel(2);
        officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2); //self-repair important...
        officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1); //easier difficulty to speed it up
        caliph.setCaptain(officer);
        api.defeatOnShipLoss("KHS-001 Hand of God");
        boolean vsp = Global.getSettings().getModManager().isModEnabled("vayrashippack");
        api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
        api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, true);
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
        api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
			}
			public void advance(float amount, List events) {
                            if (Global.getCombatEngine().isPaused()) {
                                return;
                            }
                            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                                if (ship.getCustomData().get("poopystinky") == null) {
                                    if (ship.getCaptain() != null && ship.getOwner() == 0 && ship.getCaptain().getStats().getSkillsCopy().size() > 4) {
                                        String text = "";
                                        for (int u = 4; u < ship.getCaptain().getStats().getSkillsCopy().size(); u++) {
											if (u < ship.getCaptain().getStats().getSkillsCopy().size()-1) {text = text+(((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getLevel() > 1 ?  ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+"+, " :  ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+", ");} else {text = text+(((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getLevel() > 1 ? ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+"+." :  ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+".");}
                                        }
									if (ship.getFleetMember() != null) {
									//Global.getCombatEngine().getCombatUI().addMessage(1, ship.getFleetMember(), Misc.getPositiveHighlightColor(), ship.getName(), Misc.getTextColor(), "", Global.getSettings().getColor("standardTextColor"), "is skilled in "+text); We censor this because there is important dialogue going on!
                                                                        }
                                    }
                                    ship.setCurrentCR(ship.getCurrentCR()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //Properly adds the max CR, for some reason it cannot be caught as FleetMemberAPI or this would have been easier...
                                    ship.setCRAtDeployment(ship.getCRAtDeployment()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //This only affects the "score" result of said mission, but the algorithm is mostly 100% since you have to basically LOSE ships to lose score. I don't think this needs setting, but eh couldn't help but tried.
                                    ship.setCustomData("poopystinky", true); //Fires once per ship.
                                }
                            }
                        }
		});
        api.addPlugin(new Plugin());
    }
    

    public final static class Plugin extends BaseEveryFrameCombatPlugin {

        private static boolean runOnce = false;
        private static int wave = 0;
        private static int dda = 0;
        private static int caa = 0;
        private static int bba = 0;
        private static boolean reinforcementarrived = false;

        private static final IntervalUtil TIMER = new IntervalUtil(30f, 50f);

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
        private static final WeightedRandomPicker<String> refugeequote = new WeightedRandomPicker<>();
        private static final WeightedRandomPicker<String> qamarquote = new WeightedRandomPicker<>();
        private static final WeightedRandomPicker<String> hegemonyquote = new WeightedRandomPicker<>();

        @Override
        public void init(CombatEngineAPI engine) {
            runOnce = false;
            reinforcementarrived = false;
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

            BB.add("onslaught_xiv_Elite", 0.25f);
            BB.add("onslaught_Standard", 0.5f);
            BB.add("onslaught_Outdated", 0.5f);

            CIV.add("mudskipper_Standard");
            CIV.add("valkyrie_Elite");
            CIV.add("nebula_Standard");
            CIV.add("starliner_Standard");
            CIV.add("vayra_mendicant_refugee");

            refugeequote.add("Watchers keep us safe!");
            refugeequote.add("Please help us!");
            refugeequote.add("What.. has happened to our warriors in the sky?");

            qamarquote.add("We were once all sons and daughters of Kadur. \n I offer my service of vengeance on those who destroyed our home.");
            qamarquote.add("What they have done is unforgiveable. \n I will overlook our differences for our people. For Kadur!");

            hegemonyquote.add("More hostiles coming our way!");
            hegemonyquote.add("Brace yourself, they're coming!");
            hegemonyquote.add("How much more can we take.. more are coming!");
            hegemonyquote.add("On our tail, they're coming after us!");
            hegemonyquote.add("More hostiles ahead! Keep the Caliph alive!");

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
                        ship.setHeavyDHullOverlay();
                        ship.setCurrentCR(0f);
                        ship.setCRAtDeployment(0f);
                    }
                }
                Global.getCombatEngine().getCombatUI().addMessage(1, Global.getCombatEngine().getPlayerShip().getFleetMember(), Misc.getPositiveHighlightColor(), Global.getCombatEngine().getPlayerShip().getName(), Misc.getTextColor(), ":", Global.getSettings().getColor("standardTextColor"), "This is Archmandrite Umayya to all Kadur vessels. Rally at the Hand of God's coordinates. \n This is not over. We will fight another day, but we must evacuate now.");
            }

            if (TIMER.intervalElapsed()) {
                Global.getSoundPlayer().playUISound("cr_allied_critical", 0.77f, 10f);
                wave++;
                Vector2f botMid = new Vector2f(0f, -(engine.getMapHeight() / 2f));
                if (wave % 2f == 1f) { //no more survivors after this.
                Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).setSuppressDeploymentMessages(true);
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
                Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).setSuppressDeploymentMessages(false);
                Global.getCombatEngine().getCombatUI().addMessage(1, civ.getFleetMember(), Misc.getHighlightColor(), civ.getName(), Misc.getTextColor(), ":", Global.getSettings().getColor("standardTextColor"), refugeequote.pick());}
                MID_LEFT.set(-(engine.getMapWidth() / 2f), 0f);
                MID_RIGHT.set((engine.getMapWidth() / 2f), 0f);
                if (wave > 5 && !reinforcementarrived) {
                    reinforcementarrived = true;
                    Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).setSuppressDeploymentMessages(true);
                    ShipAPI ally1 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_camel_qamar_assault",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.75f,
                            new Vector2f(-(engine.getMapWidth() / 2f), 300f),
                            0f);
                    ally1.setOriginalOwner(0);
                    ally1.setOwner(0);
                    ally1.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
                    ShipAPI ally2 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_camel_qamar_missile",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.75f,
                            new Vector2f(-(engine.getMapWidth() / 2f), 600f),
                            0f);
                    ally2.setOriginalOwner(0);
                    ally2.setOwner(0);
                    ally2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
                    ShipAPI ally3 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_camel_qamar_torpedo",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.75f,
                            new Vector2f(-(engine.getMapWidth() / 2f), 900f),
                            0f);
                    ally3.setOriginalOwner(0);
                    ally3.setOwner(0);
                    ally3.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
                    ShipAPI ally4 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_mendicant_qamar_assault",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.75f,
                            new Vector2f((engine.getMapWidth() / 2f), 300f),
                            180f);
                    ally4.setOriginalOwner(0);
                    ally4.setOwner(0);
                    ally4.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
                    ShipAPI ally5 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_mendicant_qamar_missile",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.75f,
                            new Vector2f((engine.getMapWidth() / 2f), 600f),
                            180f);
                    ally5.setOriginalOwner(0);
                    ally5.setOwner(0);
                    ally5.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
                    ShipAPI ally6 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_mendicant_qamar_strike",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.75f,
                            new Vector2f((engine.getMapWidth() / 2f), 900f),
                            180f);
                    ally6.setOriginalOwner(0);
                    ally6.setOwner(0);
                    ally6.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
                    ShipAPI ally7 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_prophet_qamar_retribution",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.85f,
                            new Vector2f(-(engine.getMapWidth() / 2f), 1200f),
                            0f);
                    ally7.setOriginalOwner(0);
                    ally7.setOwner(0);
                    ally7.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 3, new Random()));
                    ShipAPI ally8 = CombatUtils.spawnShipOrWingDirectly(
                            "vayra_prophet_qamar_retribution",
                            FleetMemberType.SHIP,
                            FleetSide.PLAYER,
                            0.85f,
                            new Vector2f((engine.getMapWidth() / 2f), 1200f),
                            180f);
                    ally8.setOriginalOwner(0);
                    ally8.setOwner(0);
                    ally8.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("qamar_insurgency"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
                    Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).setSuppressDeploymentMessages(false);
                    Global.getCombatEngine().getCombatUI().addMessage(1, ally7.getFleetMember(), Misc.getHighlightColor(), ally7.getName(), Misc.getTextColor(), ":", Global.getSettings().getColor("standardTextColor"), qamarquote.pick());
                }
                boolean doonce = false;
                if (Global.getCombatEngine().getShips().size() <= 80) { //Prevents spam, but players can probably exploit their way... eh whatever.
                    for (Vector2f loc : LOCS) {
                        if (wave % 2f == 0f) {
                            List<String> toSpawn = new ArrayList<>();
                            //for (int i = 0; i < wave; i++) { This caused the weird duplication of ships to basically... overwhelm you.
                            for (int ff = 0; ff < wave / 2; ff++) {
                                toSpawn.add(FF.pick());
                            }
                            if (dda < 9) for (int dd = 0; dd < wave / 3; dd++) { //Spawn 9 destroyers
                                toSpawn.add(DD.pick());
                                dda++;
                            }
                            if (caa < 6) {for (int ca = 0; ca < wave / 4; ca++) { //Spawn 6 cruisers
                                toSpawn.add(CA.pick());
                                caa++;
                            }}
                            if (bba < 3) {for (int bb = 0; bb < wave / 5; bb++) { //Spawn 3 capitals
                                toSpawn.add(BB.pick());
                                bba++;
                            }}
                            //}
                            for (String var : toSpawn) {
                                Vector2f point = MathUtils.getRandomPointInCircle(loc, 2000f);
                                ShipAPI enemy = CombatUtils.spawnShipOrWingDirectly(
                                        var,
                                        FleetMemberType.SHIP,
                                        FleetSide.ENEMY,
                                        0.5f,
                                        point,
                                        VectorUtils.getAngle(point, engine.getPlayerShip().getLocation()));
                                enemy.setOriginalOwner(1);
                                enemy.setOwner(1);
                            }
                        if (Global.getCombatEngine().getPlayerShip() != null && Global.getCombatEngine().getPlayerShip().getFleetMember() != null && !doonce) {Global.getCombatEngine().getCombatUI().addMessage(1, Global.getCombatEngine().getPlayerShip().getFleetMember(), Misc.getPositiveHighlightColor(), Global.getCombatEngine().getPlayerShip().getName(), Misc.getTextColor(), ":", Global.getSettings().getColor("standardTextColor"), hegemonyquote.pick());doonce = true;}
                        }
                    }
                }
            }
        }
    }
}
