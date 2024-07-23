package data.missions.vayra_k005;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        boolean vsp = Global.getSettings().getModManager().isModEnabled("vayrashippack");

        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false, 10);
        api.initFleet(FleetSide.ENEMY, "KHS", FleetGoal.ATTACK, true, 10);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "Hegemony Grand Invasion Force");
        api.setFleetTagline(FleetSide.ENEMY, "Kadur Holy Fleet strongpointed by KHS-001 Hand of God");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat all enemy forces. Kadur must fall!");
        api.addBriefingItem("You start with Coordinated Manuevers and Support Doctrine. The enemy has Carrier Group.");
        api.addBriefingItem("Optional mod support: Vayra's Ship Pack");

        // Set up the player's fleet
        api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
        api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
        api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.BEST_OF_THE_BEST, 1); //We won't tell the player this because the current system favors Kadur numbers that will let you deploy more than Hegemony. This skill will let them deploy almost equivalent enough to win this war!
        api.addToFleet(FleetSide.PLAYER, "onslaught_xiv_Elite", FleetMemberType.SHIP, true).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 5, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 5, new Random()));
        api.addToFleet(FleetSide.PLAYER, "onslaught_Elite", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 4, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 3, new Random()));
        api.addToFleet(FleetSide.PLAYER, "onslaught_Standard", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 4, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 3, new Random()));
        api.addToFleet(FleetSide.PLAYER, "onslaught_Outdated", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 4, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 3, new Random()));
        api.addToFleet(FleetSide.PLAYER, "legion_Strike", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 4, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 3, new Random()));
        api.addToFleet(FleetSide.PLAYER, "legion_FS", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 4, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 3, new Random()));
        api.addToFleet(FleetSide.PLAYER, "dominator_XIV_Elite", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.PLAYER, "dominator_AntiCV", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
        if (vsp) {
            api.addToFleet(FleetSide.PLAYER, "vayra_subjugator_a", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
            api.addToFleet(FleetSide.PLAYER, "vayra_subjugator_s", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
        } else {
            api.addToFleet(FleetSide.PLAYER, "dominator_Fighter_Support", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
            api.addToFleet(FleetSide.PLAYER, "dominator_Outdated", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
        }
        api.addToFleet(FleetSide.PLAYER, "eagle_xiv_Elite", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.PLAYER, "falcon_CS", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.PLAYER, "falcon_CS", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, OfficerManagerEvent.SkillPickPreference.ANY, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.PLAYER, "mora_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "mora_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "sunder_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);

        // Set up the enemy fleet
        api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
        //api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1); Made the mission incredibly hard.
        api.addToFleet(FleetSide.ENEMY, "vayra_caliph_revenant", FleetMemberType.SHIP, "KHS-001 Hand of God", true).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 5, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 5, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_prophet_line", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_ziz_strike", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_seraph_standard", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_sphinx_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_golem_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_rukh_standard", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_rukh_heavy", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_rukh_interceptor", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_rukh_light", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 2, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_archimandrite_shockweb", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_sunbird_torpedo", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_sunbird_fs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_antifighter", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_microfission", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_targe_cs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_targe_cs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_targe_line", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_targe_line", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_shirdal_fighter", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_shirdal_bomber", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_shirdal_rocket", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_camel_assault", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_camel_shotgun", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_buzzard_fs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_buzzard_fs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_rod", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_shotgun", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));

        // Set up the map.
        float width = 24000f;
        float height = 18000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        for (int i = 0; i < 15; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 900f;
            api.addNebula(x, y, radius);
        }

        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 2000);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2000);

        api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.4f, "nav_buoy");
        api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.6f, "nav_buoy");
        api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.3f, "comm_relay");
        api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.7f, "comm_relay");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
        api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.5f, "sensor_array");

        // Add an asteroid field
        api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
                20f, 70f, 50);

        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(0, 0, 200f, "desert", 350f, true);
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
									Global.getCombatEngine().getCombatUI().addMessage(1, ship.getFleetMember(), Misc.getPositiveHighlightColor(), ship.getName(), Misc.getTextColor(), "", Global.getSettings().getColor("standardTextColor"), "is skilled in "+text);}
                                    }
                                    ship.setCurrentCR(ship.getCurrentCR()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //Properly adds the max CR, for some reason it cannot be caught as FleetMemberAPI or this would have been easier...
                                    ship.setCRAtDeployment(ship.getCRAtDeployment()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //This only affects the "score" result of said mission, but the algorithm is mostly 100% since you have to basically LOSE ships to lose score. I don't think this needs setting, but eh couldn't help but tried.
                                    ship.setCustomData("poopystinky", true); //Fires once per ship.
                                }
                            }
                        }
		});
    }

}
