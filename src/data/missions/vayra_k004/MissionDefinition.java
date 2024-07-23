package data.missions.vayra_k004;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;

public class MissionDefinition implements MissionDefinitionPlugin {
    
    public static Logger log = Global.getLogger(MissionDefinition.class);

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false, 20);
        api.initFleet(FleetSide.ENEMY, "KHS", FleetGoal.ATTACK, true, 10);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "The best-paid fighting force in the Persean Sector");
        api.setFleetTagline(FleetSide.ENEMY, "KHS-002 Born of Heaven plus light escort");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Destroy the KHS-002 Born of Heaven");
        api.addBriefingItem("Observe the target's shield and mobility.");
        api.addBriefingItem("Take advantage of the distraction offered by your Hegemony backup");
        api.addBriefingItem("Optional mod support: Too much to list, some faction mods supported");

        boolean vsp = Global.getSettings().getModManager().isModEnabled("vayrashippack");
        boolean swp = Global.getSettings().getModManager().isModEnabled("swp");
        boolean brdy = Global.getSettings().getModManager().isModEnabled("blackrock_driveyards");
        boolean dme = Global.getSettings().getModManager().isModEnabled("istl_dam");
        boolean uw = Global.getSettings().getModManager().isModEnabled("underworld");
        boolean dara = Global.getSettings().getModManager().isModEnabled("DisassembleReassemble");
        boolean diable = Global.getSettings().getModManager().isModEnabled("diableavionics");
        boolean scy = Global.getSettings().getModManager().isModEnabled("SCY");
        boolean seeker = Global.getSettings().getModManager().isModEnabled("SEEKER");
        boolean sylphon = Global.getSettings().getModManager().isModEnabled("Sylphon_RnD");
        boolean tahlan = Global.getSettings().getModManager().isModEnabled("tahlan");
        boolean shi = Global.getSettings().getModManager().isModEnabled("shadow_ships");
        boolean ora = Global.getSettings().getModManager().isModEnabled("ORA");
        boolean ii = Global.getSettings().getModManager().isModEnabled("Imperium");
        boolean templars = Global.getSettings().getModManager().isModEnabled("Templars");
        boolean snsp = Global.getSettings().getModManager().isModEnabled("snsp");
        boolean vass = Global.getSettings().getModManager().isModEnabled("the_vass");
        boolean scalar = Global.getSettings().getModManager().isModEnabled("tahlan_scalartech");
        boolean xhan = Global.getSettings().getModManager().isModEnabled("XhanEmpire");
        boolean vic = Global.getSettings().getModManager().isModEnabled("vic");
        boolean ice = Global.getSettings().getModManager().isModEnabled("nbj_ice");   
        boolean jp = Global.getSettings().getModManager().isModEnabled("junk_pirates_release");  
        boolean lta = Global.getSettings().getModManager().isModEnabled("LTA");      
        boolean irs = Global.getSettings().getModManager().isModEnabled("timid_xiv");      

        WeightedRandomPicker<String> flags = new WeightedRandomPicker<>();
        WeightedRandomPicker<String> mercs = new WeightedRandomPicker<>();
        WeightedRandomPicker<String> backup = new WeightedRandomPicker<>();

        mercs.add("hyperion_Strike");
        mercs.add("afflictor_Strike");
        mercs.add("shade_Assault");
        mercs.add("omen_PD");
        mercs.add("tempest_Attack");
        flags.add("harbinger_Strike");
        flags.add("vayra_hegbinger_strike");
        backup.add("enforcer_XIV_Elite");
        backup.add("enforcer_XIV_Elite");
        backup.add("monitor_Escort");

        if (vsp) {
            if (Global.getSettings().getVariant("antediluvian_forlorn_Submersible") != null) mercs.add("antediluvian_forlorn_Submersible");
        }
        if (swp) {
            if (Global.getSettings().getVariant("swp_excelsior_att") != null) flags.add("swp_excelsior_att");
            if (Global.getSettings().getVariant("swp_boss_afflictor_cus") != null) flags.add("swp_boss_afflictor_cus");
            if (Global.getSettings().getVariant("swp_boss_euryale_cus") != null) flags.add("swp_boss_euryale_cus");
            if (Global.getSettings().getVariant("swp_boss_hyperion_cus") != null) flags.add("swp_boss_hyperion_cus");
            if (Global.getSettings().getVariant("swp_boss_shade_cus") != null) flags.add("swp_boss_shade_cus");
            if (Global.getSettings().getVariant("swp_boss_lasher_b_cus") != null) mercs.add("swp_boss_lasher_b_cus");
            if (Global.getSettings().getVariant("swp_boss_medusa_cus") != null) mercs.add("swp_boss_medusa_cus");
            if (Global.getSettings().getVariant("swp_boss_hammerhead_cus") != null) mercs.add("swp_boss_hammerhead_cus");
            if (Global.getSettings().getVariant("swp_hammerhead_xiv_eli") != null) backup.add("swp_hammerhead_xiv_eli");
            if (Global.getSettings().getVariant("swp_hammerhead_xiv_eli") != null) backup.add("swp_hammerhead_xiv_eli");
            if (Global.getSettings().getVariant("swp_brawler_hegemony_ass") != null) backup.add("swp_brawler_hegemony_ass");
            if (Global.getSettings().getVariant("swp_sunder_xiv_eli") != null) backup.add("swp_sunder_xiv_eli");
        }
        if (brdy) {
            if (Global.getSettings().getVariant("brdy_imaginos_elite") != null) flags.add("brdy_imaginos_elite");
            if (Global.getSettings().getVariant("brdy_morpheus_proto") != null) flags.add("brdy_morpheus_proto");
            if (Global.getSettings().getVariant("desdinova_HK") != null) mercs.add("desdinova_HK");
        }
        if (dme) {
            if (Global.getSettings().getVariant("istl_vesper_6e_elite") != null) mercs.add("istl_vesper_6e_elite");
            if (Global.getSettings().getVariant("tempest_righthand") != null) mercs.add("tempest_righthand");
            if (Global.getSettings().getVariant("istl_demon_std") != null) flags.add("istl_demon_std");
            if (Global.getSettings().getVariant("istlx_braveblade_std") != null) flags.add("istlx_braveblade_std");
            if (Global.getSettings().getVariant("istl_ifrit_support") != null) mercs.add("istl_ifrit_support");
            if (Global.getSettings().getVariant("istl_imp_proto_test") != null) mercs.add("istl_imp_proto_test");
            if (Global.getSettings().getVariant("istl_starsylph_deserter_test") != null) mercs.add("istl_starsylph_deserter_test");
            if (Global.getSettings().getVariant("istl_maskirovka_elite") != null) flags.add("istl_maskirovka_elite");
            if (Global.getSettings().getVariant("istl_snowgoose_elite") != null) mercs.add("istl_snowgoose_elite");
            if (Global.getSettings().getVariant("istl_starsylph_elite") != null) mercs.add("istl_starsylph_elite");
            if (Global.getSettings().getVariant("istl_vesper_elite") != null) mercs.add("istl_vesper_elite");
        }
        if (uw) {
            if (Global.getSettings().getVariant("uw_predator_x_rai") != null) mercs.add("uw_predator_x_rai");
            if (Global.getSettings().getVariant("uw_venomx_eli") != null) mercs.add("uw_venomx_eli");
            if (Global.getSettings().getVariant("uw_afflictor_cabal_cus") != null) mercs.add("uw_afflictor_cabal_cus");
            if (Global.getSettings().getVariant("uw_harbinger_cabal_cus") != null) flags.add("uw_harbinger_cabal_cus");
            if (Global.getSettings().getVariant("uw_hyperion_cabal_cus") != null) flags.add("uw_hyperion_cabal_cus");
            if (Global.getSettings().getVariant("uw_medusa_cabal_cus") != null) mercs.add("uw_medusa_cabal_cus");
            if (Global.getSettings().getVariant("uw_scarab_cabal_cus") != null) mercs.add("uw_scarab_cabal_cus");
            if (Global.getSettings().getVariant("uw_tempest_cabal_cus") != null) mercs.add("uw_tempest_cabal_cus");
        }
        if (dara) {
            if (Global.getSettings().getVariant("dara_lysander_CQ") != null) mercs.add("dara_lysander_CQ");
            if (Global.getSettings().getVariant("dara_gypsymoth_Scav") != null) mercs.add("dara_gypsymoth_Scav");
        }
        if (diable) {
            if (Global.getSettings().getVariant("diableavionics_versant_standard") != null) mercs.add("diableavionics_versant_standard");
        }
        if (scy) {
            if (Global.getSettings().getVariant("SCY_stymphalianbird_gunner") != null) mercs.add("SCY_stymphalianbird_gunner");
        }
        if (seeker) {
            if (Global.getSettings().getVariant("SKR_aethernium_support") != null) mercs.add("SKR_aethernium_support");
            if (Global.getSettings().getVariant("SKR_butterfly_assault") != null) mercs.add("SKR_butterfly_assault");
            if (Global.getSettings().getVariant("SKR_hedone_premium") != null) mercs.add("SKR_hedone_premium");
        }
        if (sylphon) {
            if (Global.getSettings().getVariant("SRD_Celika_uv_racer") != null) mercs.add("SRD_Celika_uv_racer");
            if (Global.getSettings().getVariant("SRD_Furika_standard") != null) mercs.add("SRD_Furika_standard");
            if (Global.getSettings().getVariant("SRD_Vril_standard") != null) mercs.add("SRD_Vril_standard");
            if (Global.getSettings().getVariant("SRD_Tarima_assault") != null) mercs.add("SRD_Tarima_assault");
            if (Global.getSettings().getVariant("SRD_Silverhead_standard") != null) mercs.add("SRD_Silverhead_standard");
            if (Global.getSettings().getVariant("SRD_Ascordia_prototype") != null) mercs.add("SRD_Ascordia_prototype");
        }
        if (tahlan) {
            if (Global.getSettings().getVariant("tahlan_darnus_killer") != null) flags.add("tahlan_darnus_killer");
            if (Global.getSettings().getVariant("tahlan_Exa_Pico_standard") != null) mercs.add("tahlan_Exa_Pico_standard");
            if (Global.getSettings().getVariant("tahlan_Haelequin_standard") != null) mercs.add("tahlan_Haelequin_standard");
            if (Global.getSettings().getVariant("tahlan_Korikaze_ion") != null) mercs.add("tahlan_Korikaze_ion");
            if (Global.getSettings().getVariant("tahlan_monitor_gh_knight") != null) mercs.add("tahlan_monitor_gh_knight");
            if (Global.getSettings().getVariant("tahlan_Skola_standard") != null) mercs.add("tahlan_Skola_standard");
            if (Global.getSettings().getVariant("tahlan_Tempest_P_standard") != null) mercs.add("tahlan_Tempest_P_standard");
            if (Global.getSettings().getVariant("tahlan_Torii_standard") != null) mercs.add("tahlan_Torii_standard");
            if (Global.getSettings().getVariant("tahlan_Yosei_standard") != null) mercs.add("tahlan_Yosei_standard");
            if (Global.getSettings().getVariant("tahlan_Vale_crusader") != null) mercs.add("tahlan_Vale_crusader");
        }
        if (shi) {
            if (Global.getSettings().getVariant("ms_shamash_EMP") != null) mercs.add("ms_shamash_EMP");
        }
        if (swp && ii) {
            if (Global.getSettings().getVariant("swp_boss_excelsior_cus") != null) flags.add("swp_boss_excelsior_cus");
        }
        if (ii) {
            if (Global.getSettings().getVariant("ii_maximus_str") != null) mercs.add("ii_maximus_str");
            if (Global.getSettings().getVariant("ii_lynx_eli") != null) flags.add("ii_lynx_eli");
        }
        if (ora) {
            if (Global.getSettings().getVariant("ora_ascension_control") != null) mercs.add("ora_ascension_control");
        }
        if (templars) {
            if (Global.getSettings().getVariant("tem_jesuit_est") != null) mercs.add("tem_jesuit_est");
            if (Global.getSettings().getVariant("tem_crusader_agi") != null) mercs.add("tem_crusader_agi");
        }
        if (snsp) {
            if (Global.getSettings().getVariant("snsp_silvestris_default") != null) mercs.add("snsp_silvestris_default");
        }
        if (vass) {
            if (Global.getSettings().getVariant("vass_akrafena_assault") != null) flags.add("vass_akrafena_assault");
            if (Global.getSettings().getVariant("vass_schiavona_multipurpose") != null) flags.add("vass_schiavona_multipurpose");
            if (Global.getSettings().getVariant("vass_makhaira_aggressor") != null) mercs.add("vass_makhaira_aggressor");
        }
        if (scalar) {
            if (Global.getSettings().getVariant("tahlan_skirt_hunter") != null) flags.add("tahlan_skirt_hunter");
        }
        if (xhan) {
            if (Global.getSettings().getVariant("XHAN_Pharrek_variant_EmperorsScalpel") != null) flags.add("XHAN_Pharrek_variant_EmperorsScalpel");
            if (Global.getSettings().getVariant("PAMED_ultra233_liquidator") != null) flags.add("PAMED_ultra233_liquidator");
        }
        if (vic) {
            if (Global.getSettings().getVariant("vic_nybbas_plasma") != null) flags.add("vic_nybbas_plasma");
        }
        if (ice) {
            flags.add("sun_ice_nightseer_Assualt");
        }
        if (jp) {
            if (Global.getSettings().getVariant("junk_pirates_turbot_Assault") != null) mercs.add("junk_pirates_turbot_Assault");
            if (Global.getSettings().getVariant("pack_sharpei_canebianco_Standard") != null) flags.add("pack_sharpei_canebianco_Standard");
        }
        if (lta) {
            if (Global.getSettings().getVariant("LTA_Epattcudxx_Heavilymodified") != null) flags.add("LTA_Epattcudxx_Heavilymodified");
        }
        if (irs) {
            if (Global.getSettings().getVariant("eis_valorous_standard") != null) flags.add("eis_valorous_standard");
        }
        String ship1 = flags.pickAndRemove();
        String ship2 = flags.pickAndRemove();
        String ship3 = mercs.pickAndRemove();
        String ship4 = mercs.pickAndRemove();
        String ship5 = backup.pick();
        String ship6 = backup.pick();
        String ship7 = backup.pick();
        String ship8 = backup.pick();

        // ships in fleets
        
        // flags
        FleetMemberAPI fm1 = api.addToFleet(FleetSide.PLAYER, ship1, FleetMemberType.SHIP, true);
        if (fm1.getVariant().getHullVariantId() == null ? ship1 != null : !fm1.getVariant().getHullVariantId().equals(ship1)) {log.error("couldn't find variant " + ship1);} else {fm1.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("independent"), 4, FleetFactoryV3.getSkillPrefForShip(fm1), true, null, true, true, 3, new Random()));}
        FleetMemberAPI fm2 = api.addToFleet(FleetSide.PLAYER, ship2, FleetMemberType.SHIP, false);
        if (fm2.getVariant().getHullVariantId() == null ? ship2 != null : !fm2.getVariant().getHullVariantId().equals(ship2)) {log.error("couldn't find variant " + ship2);} else {fm2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("independent"), 4, FleetFactoryV3.getSkillPrefForShip(fm2), true, null, true, true, 3, new Random()));}
        
        // mercs
        FleetMemberAPI fm3 = api.addToFleet(FleetSide.PLAYER, ship3, FleetMemberType.SHIP, false);
        if (fm3.getVariant().getHullVariantId() == null ? ship3 != null : !fm3.getVariant().getHullVariantId().equals(ship3)) {log.error("couldn't find variant " + ship3);} else {fm3.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("independent"), 3, FleetFactoryV3.getSkillPrefForShip(fm3), true, null, true, true, 1, new Random()));}
        FleetMemberAPI fm4 = api.addToFleet(FleetSide.PLAYER, ship4, FleetMemberType.SHIP, false);
        if (fm4.getVariant().getHullVariantId() == null ? ship4 != null : !fm4.getVariant().getHullVariantId().equals(ship4)) {log.error("couldn't find variant " + ship4);}  else {fm4.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("independent"), 3, FleetFactoryV3.getSkillPrefForShip(fm4), true, null, true, true, 1, new Random()));}
        
        // backup ships
        api.addToFleet(FleetSide.PLAYER, ship5, FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, SkillPickPreference.ANY, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, ship6, FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, SkillPickPreference.ANY, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, ship7, FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, SkillPickPreference.ANY, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.PLAYER, ship8, FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("hegemony"), 3, SkillPickPreference.ANY, true, null, true, true, 1, new Random()));
        

        // enemies
        api.addToFleet(FleetSide.ENEMY, "vayra_caliph_revenant", FleetMemberType.SHIP, "KHS-002 Born of Heaven", true).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 5, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 5, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_archimandrite_shockweb", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_sunbird_torpedo", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_crystal", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_crystal", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_targe_crystal", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_camel_shotgun", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_buzzard_fs", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_crystal", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_crystal", FleetMemberType.SHIP, false).setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction("kadur_remnant"), 2, SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_NO_DEFENSE, true, null, true, true, 1, new Random()));

        api.defeatOnShipLoss("KHS-002 Born of Heaven");

        // Set up the map.
        float width = 9000f;
        float height = 9000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);
        
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
