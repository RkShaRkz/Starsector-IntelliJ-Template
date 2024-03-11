package data.missions.vayra_k002;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ESCAPE, false, 10);
        api.initFleet(FleetSide.ENEMY, "KHS", FleetGoal.ATTACK, true, 5);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Hegemony Grand Invasion Vanguard");
        api.setFleetTagline(FleetSide.ENEMY, "Kadur light combat patrol plus incoming reinforcements");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("HSS Spirit of Jangala and HSS Mighty Fist must survive");
        api.addBriefingItem("At least 25% of the Hegemony forces must escape");

        boolean swp = Global.getSettings().getModManager().isModEnabled("swp");
        boolean vsp = Global.getSettings().getModManager().isModEnabled("vayrashippack");

        // ships in fleets
        // reinforcements
        api.addToFleet(FleetSide.PLAYER, "dominator_XIV_Elite", FleetMemberType.SHIP, "HSS Spirit of Jangala", true);
        if (vsp) {
            api.addToFleet(FleetSide.PLAYER, "vayra_oppressor_s", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_subjugator_s", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.PLAYER, "mora_Support", FleetMemberType.SHIP, false);
        if (swp) {
            api.addToFleet(FleetSide.PLAYER, "swp_gryphon_xiv_eli", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.PLAYER, "gryphon_Standard", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.PLAYER, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);

        if (vsp && swp) {
            api.addToFleet(FleetSide.PLAYER, "swp_brawler_hegemony_ass", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "swp_brawler_hegemony_ass", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "swp_lasher_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_bruiser_cs", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_hatchetman_a", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_hatchetman_a", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
        } else if (vsp) {
            api.addToFleet(FleetSide.PLAYER, "vayra_bruiser_cs", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_bruiser_cs", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_hatchetman_a", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "vayra_hatchetman_a", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "wolf_hegemony_CS", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "wolf_hegemony_PD", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
        } else if (swp) {
            api.addToFleet(FleetSide.PLAYER, "swp_brawler_hegemony_ass", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "swp_brawler_hegemony_ass", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "swp_lasher_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "swp_lasher_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "swp_alastor_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "swp_alastor_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "centurion_Assault", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "wolf_hegemony_CS", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "wolf_hegemony_PD", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
        }

        // original recon force minus casualties
        api.addToFleet(FleetSide.PLAYER, "falcon_xiv_Elite", FleetMemberType.SHIP, "HSS Mighty Fist", false);
        if (swp) {
            api.addToFleet(FleetSide.PLAYER, "swp_hammerhead_xiv_eli", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);
        }

        // reinforcements
        api.addToFleet(FleetSide.ENEMY, "vayra_sphinx_artillery", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.ENEMY, "vayra_rukh_light", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_eagle_k_line", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_eagle_k_line", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_falcon_k_torpedo", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_falcon_k_torpedo", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_sunbird_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_shirdal_interceptor", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_shirdal_fighter", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_rod", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_rod", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_shotgun", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_shotgun", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_overdriven", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_overdriven", FleetMemberType.SHIP, false);

        // original patrol minus fire support ships
        api.addToFleet(FleetSide.ENEMY, "vayra_sunbird_torpedo", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_camel_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_rod", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_shotgun", FleetMemberType.SHIP, false);

        // Set up the map.
        float width = 18000f;
        float height = 18000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 1200);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2300);
        api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2600);

        // Add objectives
        api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.25f, "nav_buoy");
        api.addObjective(minX + width * 0.4f + 500, minY + height * 0.45f, "sensor_array");
        api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.35f, "nav_buoy");

        // Add two big nebula clouds
        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2300);
        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 1800);

        // Add an asteroid field
        api.addRingAsteroids(0, 0, 45, 6000f,
                20f, 70f, 400);

        api.addPlanet(0, 0, 600f, "ice_giant", 50f, true);

        BattleCreationContext context = new BattleCreationContext(null, null, null, null);
        context.setInitialEscapeRange(7000f);
        api.addPlugin(new EscapeRevealPlugin(context));
    }

}
