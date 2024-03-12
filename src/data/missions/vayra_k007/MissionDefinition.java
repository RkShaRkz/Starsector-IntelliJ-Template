package data.missions.vayra_k007;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "KHS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "KHS-001 Hand of God with surviving Theocracy loyalists");
        api.setFleetTagline(FleetSide.ENEMY, "The godless thinking machine");

        api.addBriefingItem("Destroy the enemy fleet");
        api.addBriefingItem("KHS-001 Hand of God must survive");

        api.addToFleet(FleetSide.PLAYER, "vayra_caliph_revenant", FleetMemberType.SHIP, "KHS-001 Hand of God", true);
        api.defeatOnShipLoss("KHS-001 Hand of God");
        
        api.addToFleet(FleetSide.PLAYER, "vayra_prophet_line", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_ziz_support", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "vayra_sphinx_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_golem_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_heavy", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_line", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_bomber", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_fighter", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_microfission", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_microfission", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "vayra_camel_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_camel_shotgun", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_pd", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_pd", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_shotgun", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_rod", FleetMemberType.SHIP, false);

        boolean seeker = Global.getSettings().getModManager().isModEnabled("SEEKER");
        boolean swp = Global.getSettings().getModManager().isModEnabled("swp");

        // if we have mods, pick a better flagship
        if (seeker && swp) {
            api.addToFleet(FleetSide.ENEMY, "SKR_nova_falseOmega", FleetMemberType.SHIP, true).getCaptain().setPersonality(Personalities.RECKLESS);
            api.addToFleet(FleetSide.ENEMY, "swp_solar_ass", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        } else if (seeker) {
            api.addToFleet(FleetSide.ENEMY, "SKR_nova_falseOmega", FleetMemberType.SHIP, true).getCaptain().setPersonality(Personalities.RECKLESS);
            api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        } else if (swp) {
            api.addToFleet(FleetSide.ENEMY, "swp_solar_ass", FleetMemberType.SHIP, true).getCaptain().setPersonality(Personalities.RECKLESS);
            api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
            api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        } else {
            api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, true).getCaptain().setPersonality(Personalities.RECKLESS);
            api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
            api.addToFleet(FleetSide.ENEMY, "radiant_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        }

        api.addToFleet(FleetSide.ENEMY, "radiant_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        
        api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);

        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);

        api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "glimmer_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "glimmer_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);

        float width = 20000f;
        float height = 15000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        api.addObjective(width / 2f * -0.333f, height / 2f * -0.333f, "nav_buoy");
        api.addObjective(width / 2f * -0.333f, height / 2f * 0.333f, "nav_buoy");
        api.addObjective(width / 2f * 0.333f, height / 2f * -0.333f, "nav_buoy");
        api.addObjective(width / 2f * 0.333f, height / 2f * 0.333f, "nav_buoy");

        for (int i = 0; i < 20; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float size = 333f + (float) Math.random() * 666f;
            api.addNebula(x, y, size);
        }

        api.getContext().aiRetreatAllowed = false;
        api.getContext().enemyDeployAll = true;
        api.getContext().fightToTheLast = true;

        // Add custom plugin
        api.addPlugin(new Plugin());
    }

    public final static class Plugin extends BaseEveryFrameCombatPlugin {

        @Override
        public void init(CombatEngineAPI engine) {
            engine.getContext().aiRetreatAllowed = false;
            engine.getContext().enemyDeployAll = true;
            engine.getContext().fightToTheLast = true;
        }
    }
}
