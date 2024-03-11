package data.missions.vayra_k003;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

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

        // ships in fleets
        api.addToFleet(FleetSide.PLAYER, "vayra_seraph_standard", FleetMemberType.SHIP, "KHS Holy Sword", true);
        api.addToFleet(FleetSide.PLAYER, "vayra_ziz_support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_sphinx_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_golem_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_heavy", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_light", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_torpedo", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_line", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_interceptor", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_rocket", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_antifighter", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_microfission", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.ENEMY, "legion_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "legion_Escort", FleetMemberType.SHIP, false);
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
    }

}
