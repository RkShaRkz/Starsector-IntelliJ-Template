package data.missions.vayra_k001;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

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

        // ships in fleets
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_torpedo", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_interceptor", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_camel_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_rod", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_shotgun", FleetMemberType.SHIP, false);

        boolean swp = Global.getSettings().getModManager().isModEnabled("swp");

        api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, "HSS Mighty Fist", true);
        api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, "HSS Left Hook", false);
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
    }

}
