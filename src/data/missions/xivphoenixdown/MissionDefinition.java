package data.missions.xivphoenixdown;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import org.lazywizard.lazylib.combat.CombatUtils;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        api.initFleet(FleetSide.PLAYER, "HISS", FleetGoal.ATTACK, false, 0);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 10);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Ochre Jane's 'Fourthenth Baltltegruop'");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony 104th Rim Patrol");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat all enemy forces");
        api.addBriefingItem("Do not allow your station to be destroyed");
        api.addBriefingItem("Ochre Jane's assessment of her forces' relative strength may be slightly optimistic");

        // ships in fleets
        //api.addToFleet(FleetSide.PLAYER, "vayra_galleon_p_dread", FleetMemberType.SHIP, "Flying Dutchman", true);
        api.addToFleet(FleetSide.PLAYER, "vayra_atlas_xiv_rd", FleetMemberType.SHIP, "HSS PRID OV THE 14FH", true);
        api.addToFleet(FleetSide.PLAYER, "vayra_colossus_xiv_rd", FleetMemberType.SHIP, "HHS AKSHUAL DOMANE", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_colossus_xiv_rd", FleetMemberType.SHIP, "HFS VIKTORY 4 TEH FOURTENFH", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_colossus_xiv_rd", FleetMemberType.SHIP, "HISS 14TH 4 EVR", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buffalo_xiv_se", FleetMemberType.SHIP, "HSH REZPEKTED SITISIN", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buffalo_xiv_se", FleetMemberType.SHIP, "HAG DETH 2 TRATERS", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buffalo_xiv_se", FleetMemberType.SHIP, "HAA FEENICKS DOWN", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buffalo_xiv_se", FleetMemberType.SHIP, "AAH SPIRITS OF THE FORTENTH", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mudskipper_xiv_rd", FleetMemberType.SHIP, "SHH DONT LOOK TWO KLOSE", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mudskipper_xiv_rd", FleetMemberType.SHIP, "HGH SEKRIT STENGTH", false);
        api.addToFleet(FleetSide.PLAYER, "vayra_mudskipper_xiv_rd", FleetMemberType.SHIP, "HAH SWODR OF THE PHOENUCKS", false);

        boolean swp = Global.getSettings().getModManager().isModEnabled("swp");

        api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
        if (swp) {
            api.addToFleet(FleetSide.ENEMY, "swp_gryphon_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "swp_gryphon_xiv_eli", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "mora_Assault", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.ENEMY, "mora_Support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_subjugator_a", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_persecutor_a", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_intimidator_b", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
        if (swp) {
            api.addToFleet(FleetSide.ENEMY, "swp_hammerhead_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "swp_sunder_xiv_eli", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);
        if (swp) {
            api.addToFleet(FleetSide.ENEMY, "swp_lasher_xiv_eli", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "swp_brawler_hegemony_ass", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "swp_alastor_xiv_eli", FleetMemberType.SHIP, false);
        } else {
            api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_CS", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);

        // Set up the map.
        float width = 9000f;
        float height = 9000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        // Add an asteroid field
        api.addAsteroidField(0, 0, -69, 3500f,
                50f, 200f, 200);

        // Add custom plugin
        api.addPlugin(new Plugin());
    }

    public final static class Plugin extends BaseEveryFrameCombatPlugin {

        private static boolean runOnce = false;
        private ShipAPI station = null;

        @Override
        public void init(CombatEngineAPI engine) {
            runOnce = false;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {

            CombatEngineAPI engine = Global.getCombatEngine();

            if (!runOnce) {
                runOnce = true;

                station = CombatUtils.spawnShipOrWingDirectly(
                        "station1_Standard",
                        FleetMemberType.SHIP,
                        FleetSide.PLAYER,
                        0.3f,
                        Misc.ZERO,
                        90f);
                
                station.getFleetMember().setShipName("HGG OLD EARTH 2");
                station.setAlly(true);
            }
            
            if (station != null && (!station.isAlive() || station.isHulk())) {
                engine.endCombat(5f, FleetSide.ENEMY);
            }
        }
    }
}
