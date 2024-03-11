package data.missions.vayra_k004;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
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
        api.addBriefingItem("Observe how the target's shield rotates carefully before you strike");
        api.addBriefingItem("The target lacks mobility when not using its shipsystem");
        api.addBriefingItem("Take advantage of the distraction offered by your Hegemony backup");

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
            mercs.add("antediluvian_forlorn_Submersible");
        }
        if (swp) {
            flags.add("swp_excelsior_att");
            flags.add("swp_boss_afflictor_cus");
            flags.add("swp_boss_euryale_cus");
            flags.add("swp_boss_hyperion_cus");
            flags.add("swp_boss_shade_cus");
            mercs.add("swp_boss_lasher_b_cus");
            mercs.add("swp_boss_medusa_cus");
            mercs.add("swp_boss_hammerhead_cus");
            backup.add("swp_hammerhead_xiv_eli");
            backup.add("swp_hammerhead_xiv_eli");
            backup.add("swp_brawler_hegemony_ass");
            backup.add("swp_sunder_xiv_eli");
        }
        if (brdy) {
            flags.add("brdy_imaginos_elite");
            flags.add("brdy_morpheus_proto");
            mercs.add("desdinova_HK");
        }
        if (dme) {
            mercs.add("istl_vesper_6e_elite");
            mercs.add("tempest_righthand");
            flags.add("istl_demon_std");
            flags.add("istlx_braveblade_std");
            mercs.add("istl_ifrit_support");
            mercs.add("istl_imp_proto_test");
            mercs.add("istl_starsylph_deserter_test");
            flags.add("istl_maskirovka_elite");
            mercs.add("istl_snowgoose_elite");
            mercs.add("istl_starsylph_elite");
            mercs.add("istl_vesper_elite");
        }
        if (uw) {
            mercs.add("uw_predator_x_rai");
            mercs.add("uw_venomx_eli");
            mercs.add("uw_afflictor_cabal_cus");
            flags.add("uw_harbinger_cabal_cus");
            flags.add("uw_hyperion_cabal_cus");
            mercs.add("uw_medusa_cabal_cus");
            mercs.add("uw_scarab_cabal_cus");
            mercs.add("uw_tempest_cabal_cus");
        }
        if (dara) {
            mercs.add("dara_lysander_CQ");
            mercs.add("dara_gypsymoth_Scav");
        }
        if (diable) {
            mercs.add("diableavionics_versant_standard");
        }
        if (scy) {
            mercs.add("SCY_stymphalianbird_gunner");
        }
        if (seeker) {
            mercs.add("SKR_aethernium_support");
            mercs.add("SKR_butterfly_assault");
            mercs.add("SKR_hedone_premium");
        }
        if (sylphon) {
            mercs.add("SRD_Celika_uv_racer");
            mercs.add("SRD_Furika_standard");
            mercs.add("SRD_Vril_standard");
            mercs.add("SRD_Tarima_assault");
            mercs.add("SRD_Silverhead_standard");
            mercs.add("SRD_Ascordia_prototype");
        }
        if (tahlan) {
            flags.add("tahlan_darnus_killer");
            mercs.add("tahlan_Exa_Pico_standard");
            mercs.add("tahlan_Haelequin_standard");
            mercs.add("tahlan_Korikaze_ion");
            mercs.add("tahlan_monitor_gh_knight");
            mercs.add("tahlan_Skola_standard");
            mercs.add("tahlan_Tempest_P_standard");
            mercs.add("tahlan_Torii_standard");
            mercs.add("tahlan_Yosei_standard");
            mercs.add("tahlan_Vale_crusader");
        }
        if (shi) {
            mercs.add("ms_shamash_EMP");
        }
        if (swp && ii) {
            flags.add("swp_boss_excelsior_cus");
        }
        if (ii) {
            mercs.add("ii_maximus_str");
            flags.add("ii_lynx_eli");
        }
        if (ora) {
            mercs.add("ora_ascension_control");
        }
        if (templars) {
            mercs.add("tem_jesuit_est");
            mercs.add("tem_crusader_agi");
        }
        if (snsp) {
            mercs.add("snsp_silvestris_default");
        }
        if (vass) {
            flags.add("vass_akrafena_assault");
            flags.add("vass_schiavona_multipurpose");
            mercs.add("vass_makhaira_aggressor");
        }
        if (scalar) {
            flags.add("tahlan_skirt_hunter");
        }
        if (xhan) {
            flags.add("XHAN_Pharrek_variant_EmperorsScalpel");
            flags.add("PAMED_ultra233_liquidator");
        }
        if (vic) {
            flags.add("vic_nybbas_plasma");
        }
        if (ice) {
            flags.add("sun_ice_nightseer_Assualt");
        }
        if (jp) {
            mercs.add("junk_pirates_turbot_Assault");
            flags.add("pack_sharpei_canebianco_Standard");
        }
        if (lta) {
            flags.add("LTA_Epattcudxx_Heavilymodified");
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
        if (fm1.getVariant().getHullVariantId() == null ? ship1 != null : !fm1.getVariant().getHullVariantId().equals(ship1)) log.error("couldn't find variant " + ship1);
        FleetMemberAPI fm2 = api.addToFleet(FleetSide.PLAYER, ship2, FleetMemberType.SHIP, false);
        if (fm2.getVariant().getHullVariantId() == null ? ship2 != null : !fm2.getVariant().getHullVariantId().equals(ship2)) log.error("couldn't find variant " + ship2);
        
        // mercs
        FleetMemberAPI fm3 = api.addToFleet(FleetSide.PLAYER, ship3, FleetMemberType.SHIP, false);
        if (fm3.getVariant().getHullVariantId() == null ? ship3 != null : !fm3.getVariant().getHullVariantId().equals(ship3)) log.error("couldn't find variant " + ship3);
        FleetMemberAPI fm4 = api.addToFleet(FleetSide.PLAYER, ship4, FleetMemberType.SHIP, false);
        if (fm4.getVariant().getHullVariantId() == null ? ship4 != null : !fm4.getVariant().getHullVariantId().equals(ship4)) log.error("couldn't find variant " + ship4);
        
        // backup ships
        api.addToFleet(FleetSide.PLAYER, ship5, FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, ship6, FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, ship7, FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, ship8, FleetMemberType.SHIP, false);

        // enemies
        api.addToFleet(FleetSide.ENEMY, "vayra_caliph_revenant", FleetMemberType.SHIP, "KHS-002 Born of Heaven", true);
        api.addToFleet(FleetSide.ENEMY, "vayra_archimandrite_shockweb", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_sunbird_torpedo", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_crystal", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_falchion_crystal", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_targe_crystal", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_camel_shotgun", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_buzzard_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_crystal", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vayra_hyena_crystal", FleetMemberType.SHIP, false);

        api.defeatOnShipLoss("KHS-002 Born of Heaven");

        // Set up the map.
        float width = 9000f;
        float height = 9000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);
    }

}
