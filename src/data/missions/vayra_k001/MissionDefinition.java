package data.missions.vayra_k001;

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
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        api.initFleet(FleetSide.PLAYER, "KHS", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 10);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Paladin Abbasid's Blessed Deep-Watchers");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Deep Reconnaissance Flotilla");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat all enemy forces");
        api.addBriefingItem("Optional mod support: Ship & Weapon Pack");

        // ships in fleets
        
        FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "vayra_falchion_standard", FleetMemberType.SHIP, true);
        PersonAPI officer = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, true, 2, new Random());
        officer.getName().setFirst("Peiman");
        officer.getName().setLast("Abbasid");
        officer.setGender(FullName.Gender.MALE);
        officer.setPortraitSprite(OfficerManagerEvent.pickPortrait(Global.getSector().getFaction("kadur_remnant"), FullName.Gender.MALE));
        member.setCaptain(officer);
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_torpedo", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_interceptor", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_camel_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_rod", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_shotgun", FleetMemberType.SHIP, false);
        
        boolean swp = Global.getSettings().getModManager().isModEnabled("swp");

        FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, "HSS Mighty Fist", true);
        PersonAPI officer2 = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 0, new Random());
        member2.setCaptain(officer2);
        FleetMemberAPI member3 = api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, "HSS Left Hook", false);
        PersonAPI officer3 = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, FleetFactoryV3.getSkillPrefForShip(member3), true, null, true, false, 0, new Random());
        member3.setCaptain(officer3);
        if (swp) {
            api.addToFleet(FleetSide.ENEMY, "swp_hammerhead_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "swp_hammerhead_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "swp_sunder_xiv_eli", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);

        // Set up the map.
        float width = 12000f;
        float height = 12000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 2000);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2000);

        // Add objectives
        api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.55f, "nav_buoy");
        api.addObjective(minX + width * 0.4f + 500, minY + height * 0.75f, "sensor_array");

        // Add two big nebula clouds
        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2500);
        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 1500);

        // Add an asteroid field
        api.addRingAsteroids(0, 0, 45, 6000f,
                20f, 70f, 400);
        
    
    
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
