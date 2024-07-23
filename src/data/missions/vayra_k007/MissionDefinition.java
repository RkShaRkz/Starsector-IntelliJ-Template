package data.missions.vayra_k007;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "KHS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "KHS-001 Hand of God with surviving Theocracy loyalists");
        api.setFleetTagline(FleetSide.ENEMY, "The godless thinking machine");

        api.addBriefingItem("Destroy the enemy fleet");
        api.addBriefingItem("KHS-001 Hand of God must survive");
        api.addBriefingItem("Optional mod support: Ship & Weapon Pack, Vayra's Ship Pack");

        api.addToFleet(FleetSide.PLAYER, "vayra_caliph_revenant", FleetMemberType.SHIP, "KHS-001 Hand of God", true).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 5, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 5, new Random()));
        api.defeatOnShipLoss("KHS-001 Hand of God");
        
        api.addToFleet(FleetSide.PLAYER, "vayra_prophet_line", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_ziz_support", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 4, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));

        api.addToFleet(FleetSide.PLAYER, "vayra_sphinx_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_golem_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_standard", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_rukh_heavy", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 3, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));

        api.addToFleet(FleetSide.PLAYER, "vayra_archimandrite_artillery", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_sunbird_fs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_line", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_targe_cs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_bomber", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_shirdal_fighter", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_microfission", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_falchion_microfission", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));

        api.addToFleet(FleetSide.PLAYER, "vayra_camel_assault", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_camel_shotgun", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_pd", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_buzzard_pd", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_shotgun", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));
        api.addToFleet(FleetSide.PLAYER, "vayra_hyena_rod", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 1, OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 0, new Random()));

        boolean seeker = Global.getSettings().getModManager().isModEnabled("SEEKER");
        boolean swp = Global.getSettings().getModManager().isModEnabled("swp");

        // if we have mods, pick a better flagship
        if (seeker && swp) {
            FleetMemberAPI member1 = api.addToFleet(FleetSide.ENEMY, "SKR_nova_falseOmega", FleetMemberType.SHIP, true);
            member1.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member1), true, null, true, false, 8, new Random()));
            member1.getCaptain().setPersonality(Personalities.RECKLESS);
            member1.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
            FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "swp_solar_ass", FleetMemberType.SHIP, false);
            member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 8, new Random()));
            member2.getCaptain().setPersonality(Personalities.RECKLESS);
            member2.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
        } else if (seeker) {
            FleetMemberAPI member1 = api.addToFleet(FleetSide.ENEMY, "SKR_nova_falseOmega", FleetMemberType.SHIP, true);
            member1.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member1), true, null, true, false, 8, new Random()));
            member1.getCaptain().setPersonality(Personalities.RECKLESS);
            member1.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
            FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
            member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 8, new Random()));
            member2.getCaptain().setPersonality(Personalities.RECKLESS);
            member2.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
        } else if (swp) {
            FleetMemberAPI member0 = api.addToFleet(FleetSide.ENEMY, "swp_solar_ass", FleetMemberType.SHIP, true);
            member0.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member0), true, null, true, false, 8, new Random()));
            member0.getCaptain().setPersonality(Personalities.RECKLESS);
            member0.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
            FleetMemberAPI member1 = api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
            member1.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member1), true, null, true, false, 8, new Random()));
            member1.getCaptain().setPersonality(Personalities.RECKLESS);
            member1.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
            FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
            member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 8, new Random()));
            member2.getCaptain().setPersonality(Personalities.RECKLESS);
            member2.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
        } else {
            FleetMemberAPI member0 = api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, true);
            member0.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member0), true, null, true, false, 8, new Random()));
            member0.getCaptain().setPersonality(Personalities.RECKLESS);
            member0.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
            FleetMemberAPI member1 = api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
            member1.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member1), true, null, true, false, 8, new Random()));
            member1.getCaptain().setPersonality(Personalities.RECKLESS);
            member1.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
            FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "radiant_Standard", FleetMemberType.SHIP, false);
            member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 8, new Random()));
            member2.getCaptain().setPersonality(Personalities.RECKLESS);
            member2.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
        }

        FleetMemberAPI member3 = api.addToFleet(FleetSide.ENEMY, "radiant_Standard", FleetMemberType.SHIP, false);
        member3.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 8, FleetFactoryV3.getSkillPrefForShip(member3), true, null, true, false, 8, new Random()));
        member3.getCaptain().setPersonality(Personalities.RECKLESS);
        member3.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai2.png");
        FleetMemberAPI member4 = api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
        member4.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 6, FleetFactoryV3.getSkillPrefForShip(member4), true, null, true, false, 6, new Random()));
        member4.getCaptain().setPersonality(Personalities.RECKLESS);
        member4.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai3.png");
        FleetMemberAPI member5 = api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
        member5.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 6, FleetFactoryV3.getSkillPrefForShip(member5), true, null, true, false, 6, new Random()));
        member5.getCaptain().setPersonality(Personalities.RECKLESS);
        member5.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai3.png");
        
        FleetMemberAPI member6 = api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false);
        member6.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member6), true, null, true, false, 4, new Random()));
        member6.getCaptain().setPersonality(Personalities.RECKLESS);
        member6.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member7 = api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false);
        member7.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member7), true, null, true, false, 4, new Random()));
        member7.getCaptain().setPersonality(Personalities.RECKLESS);
        member7.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member8 = api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
        member8.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member8), true, null, true, false, 4, new Random()));
        member8.getCaptain().setPersonality(Personalities.RECKLESS);
        member8.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member9 = api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
        member9.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member9), true, null, true, false, 4, new Random()));
        member9.getCaptain().setPersonality(Personalities.RECKLESS);
        member9.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member10 = api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false);
        member10.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member10), true, null, true, false, 4, new Random()));
        member10.getCaptain().setPersonality(Personalities.RECKLESS);
        member10.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member11 = api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false);
        member11.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member11), true, null, true, false, 4, new Random()));
        member11.getCaptain().setPersonality(Personalities.RECKLESS);
        member11.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member12 = api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
        member12.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member12), true, null, true, false, 4, new Random()));
        member12.getCaptain().setPersonality(Personalities.RECKLESS);
        member12.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member13 = api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
        member13.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member13), true, null, true, false, 4, new Random()));
        member13.getCaptain().setPersonality(Personalities.RECKLESS);
        member13.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");

        FleetMemberAPI member14 = api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
        member14.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member14), true, null, true, false, 4, new Random()));
        member14.getCaptain().setPersonality(Personalities.RECKLESS);
        member14.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member15 = api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
        member15.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member15), true, null, true, false, 4, new Random()));
        member15.getCaptain().setPersonality(Personalities.RECKLESS);
        member15.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member16 = api.addToFleet(FleetSide.ENEMY, "glimmer_Support", FleetMemberType.SHIP, false);
        member16.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member16), true, null, true, false, 4, new Random()));
        member16.getCaptain().setPersonality(Personalities.RECKLESS);
        member16.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member17 = api.addToFleet(FleetSide.ENEMY, "glimmer_Support", FleetMemberType.SHIP, false);
        member17.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member17), true, null, true, false, 4, new Random()));
        member17.getCaptain().setPersonality(Personalities.RECKLESS);
        member17.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member18 = api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
        member18.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member18), true, null, true, false, 4, new Random()));
        member18.getCaptain().setPersonality(Personalities.RECKLESS);
        member18.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member19 = api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
        member19.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member19), true, null, true, false, 4, new Random()));
        member19.getCaptain().setPersonality(Personalities.RECKLESS);
        member19.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");
        FleetMemberAPI member20 = api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
        member20.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("remnant"), 4, FleetFactoryV3.getSkillPrefForShip(member20), true, null, true, false, 4, new Random()));
        member20.getCaptain().setPersonality(Personalities.RECKLESS);
        member20.getCaptain().setPortraitSprite("graphics/portraits/portrait_ai1.png");

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
