package data.missions.bearsfruit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the map.
        float width = 7500f;
        float height = 7500f;
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

        ModManagerAPI manager = Global.getSettings().getModManager();

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat all enemy forces");
        api.addBriefingItem("Re-select the mission for different enemies");

        // decide if this is a fleet test or a solo test
        boolean fleet = true; //Math.random() > 0.5f;

        // Set up the player fleet up here since it's less complicated
        api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false, 0);
        api.setFleetTagline(FleetSide.PLAYER, "ExtracTech LLC");
        api.addToFleet(FleetSide.PLAYER, "vayra_bear_standard", FleetMemberType.SHIP, true);
        if (fleet) {
            // 100 DP of mining ships, incl. 1 BC, 2 CCs, and 3 DDs
            api.addToFleet(FleetSide.PLAYER, "vayra_pathfinder_artillery", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_pioneer_missile", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_pioneer_support", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_heavy_drone_tender_beam", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_heavy_drone_tender_cs", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_prospector_e", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_groundhog_cs", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_groundhog_cs", FleetMemberType.SHIP, false);
        }

        // pick an enemy faction
        WeightedRandomPicker<String> factions = new WeightedRandomPicker<>();
        factions.add(Factions.TRITACHYON);
        factions.add(Factions.REMNANTS);
        if (manager.isModEnabled("swp")) {
            factions.add(Factions.PERSEAN);
            factions.add(Factions.LUDDIC_CHURCH);
        }
        if (manager.isModEnabled("Imperium")) {
            factions.add("interstellarimperium");
        }
        if (manager.isModEnabled("SEEKER")) {
            factions.add(Factions.PIRATES);
        }
        String faction = factions.pick();

        // stock the enemy fleet
        String enemyPrefix;
        String enemyName = "An error of some sort";
        WeightedRandomPicker<String> flags = new WeightedRandomPicker<>();
        String flag;
        switch (faction) {
            case Factions.TRITACHYON:
                enemyPrefix = "TTS";
                api.initFleet(FleetSide.ENEMY, enemyPrefix, FleetGoal.ATTACK, true, 10);
                enemyName = "Corporate Project B0453";
                api.addToFleet(FleetSide.ENEMY, "paragon_Elite", FleetMemberType.SHIP, true);
                if (fleet) {
                    // 100 DP of hightech, incl. 1 BC, 2 CCs, 1 DD
                    api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "mercury_FS", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "mercury_FS", FleetMemberType.SHIP, false);
                }
                break;
            case Factions.REMNANTS:
                enemyPrefix = "TTDS";
                api.initFleet(FleetSide.ENEMY, enemyPrefix, FleetGoal.ATTACK, true, 10);
                enemyName = "Autonomous Flotilla Unit 'Aleph-Nine'";
                flags.add("radiant_Strike");
                if (manager.isModEnabled("swp")) {
                    flags.add("swp_solar_sta");
                }
                if (manager.isModEnabled("SEEKER")) {
                    flags.add("SKR_nova_falseOmega");
                }
                flag = flags.pick();
                api.addToFleet(FleetSide.ENEMY, flag, FleetMemberType.SHIP, true);
                if (fleet) {
                    // 100 DP of remnant, incl. 2 CCs, 2 DDs
                    api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "glimmer_Support", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "glimmer_Support", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
                }
                break;
            case Factions.PERSEAN:
                enemyPrefix = "PLS";
                api.initFleet(FleetSide.ENEMY, enemyPrefix, FleetGoal.ATTACK, true, 10);
                enemyName = "Kazeron Auxiliary A-54";
                api.addToFleet(FleetSide.ENEMY, "swp_victory_sta", FleetMemberType.SHIP, true);
                if (fleet) {
                    // 99 DP of midline, incl. 1 BC, 2 CCs, 2 DDs
                    api.addToFleet(FleetSide.ENEMY, "conquest_Elite", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "falcon_Attack", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, false);
                }
                break;
            case Factions.LUDDIC_CHURCH:
                enemyPrefix = "CGR";
                api.initFleet(FleetSide.ENEMY, enemyPrefix, FleetGoal.ATTACK, true, 10);
                enemyName = "Blessed Expeditionary Fleet 12";
                api.addToFleet(FleetSide.ENEMY, "swp_cathedral_gra", FleetMemberType.SHIP, true);
                if (fleet) {
                    // 100 DP of lowtech, incl. 1 BCV, 2 CCs, 3 DDs
                    api.addToFleet(FleetSide.ENEMY, "legion_FS", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "venture_Balanced", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "venture_Balanced", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "mule_Fighter_Support", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "mule_Fighter_Support", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "shepherd_Starting", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "shepherd_Starting", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "shepherd_Starting", FleetMemberType.SHIP, false);
                }
                break;
            case "interstellarimperium":
                enemyPrefix = "CGR";
                api.initFleet(FleetSide.ENEMY, enemyPrefix, FleetGoal.ATTACK, true, 10);
                enemyName = "Imperial Classis LXIX";
                api.addToFleet(FleetSide.ENEMY, "ii_matriarch_eli", FleetMemberType.SHIP, true);
                if (fleet) {
                    // 100 DP of imperium, incl. 1 BC, 2 CCs, 1 DD
                    api.addToFleet(FleetSide.ENEMY, "ii_dominus_bal", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "ii_ixon_ass", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "ii_ixon_cs", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "ii_praetorian_sta", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "ii_basileus_cs", FleetMemberType.SHIP, false);
                }
                break;
            case Factions.PIRATES:
                enemyPrefix = "";
                api.initFleet(FleetSide.ENEMY, enemyPrefix, FleetGoal.ATTACK, true, 10);
                enemyName = "Unidentified Pirate Flotilla";
                flags.add("SKR_guardian_advanced");
                flags.add("SKR_guardian_atc_bombardment");
                flags.add("SKR_guardian_broadside_standard");
                flag = flags.pick();
                api.addToFleet(FleetSide.ENEMY, flag, FleetMemberType.SHIP, true);
                if (fleet) {
                    // 100 DP of pirates, incl. 5 CCs, 7 DDs
                    api.addToFleet(FleetSide.ENEMY, "falcon_p_Strike", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "falcon_p_Strike", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "colossus3_Pirate", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "colossus3_Pirate", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "colossus3_Pirate", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "mule_d_pirates_Standard", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "mule_d_pirates_Standard", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "buffalo2_Fighter_Support", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "buffalo2_Fighter_Support", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Overdriven", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Overdriven", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Overdriven", FleetMemberType.SHIP, false);
                    api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Overdriven", FleetMemberType.SHIP, false);
                }
                break;
        }
        
        api.setFleetTagline(FleetSide.ENEMY, enemyName);
    }
}
