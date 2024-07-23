package data.missions.vayra_k003;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
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

        // Set up the fleets so we can add ships and fighter wings to them.
        api.initFleet(FleetSide.PLAYER, "KHS", FleetGoal.ATTACK, false, 10);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 10);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Kadur fleet under Cardinal Avengant Osman Abbas III");
        api.setFleetTagline(FleetSide.ENEMY, "Godless Hegemony invasion vanguard");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Destroy all enemy forces");
        api.addBriefingItem("No ship is unexpendable");
        api.addBriefingItem("You start with Carrier Group. The enemy start with Support Doctrine.");
        api.addBriefingItem("Optional mod support: Vayra's Ship Pack");

        // ships in fleets
        api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
        FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "vayra_seraph_standard", FleetMemberType.SHIP, "KHS Holy Sword", true);
        PersonAPI officer = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, true, 2, new Random());
        officer.getName().setFirst("Osman");
        officer.getName().setLast("Abbas III");
        officer.setGender(FullName.Gender.MALE);
        officer.setPortraitSprite(OfficerManagerEvent.pickPortrait(Global.getSector().getFaction("kadur_remnant"), FullName.Gender.MALE));
        member.setCaptain(officer);
        api.addToFleet(FleetSide.PLAYER, "vayra_ziz_support", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 5, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_sphinx_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_golem_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_heavy", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_standard", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_light", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_torpedo", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_line", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_interceptor", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_rocket", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_antifighter", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_microfission", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));

        api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
        api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, true).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 5, SkillPickPreference.ANY, true, null, true, true, 3, new Random()));
        api.addToFleet(FleetSide.ENEMY, "legion_Assault", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 4, SkillPickPreference.ANY, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "legion_Escort", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 4, SkillPickPreference.ANY, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "dominator_AntiCV", FleetMemberType.SHIP, false);
        if (vsp) {
            api.addToFleet(FleetSide.ENEMY, "vayra_subjugator_a", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "vayra_subjugator_s", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.ENEMY, "dominator_Fighter_Support", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "dominator_Outdated", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.ENEMY, "eagle_xiv_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "mora_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "heron_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Support", FleetMemberType.SHIP, false);

        // Set up the map.
        float width = 16000f;
        float height = 16000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        api.setHyperspaceMode(true);
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
