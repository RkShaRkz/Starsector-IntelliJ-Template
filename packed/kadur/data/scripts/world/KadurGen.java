package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import static data.scripts.KadurModPlugin.KADUR_ID;
import data.scripts.world.systems.KadurGehennaSystem;
import data.scripts.world.systems.KadurMirageSystem;

@SuppressWarnings("unchecked")
public class KadurGen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {

        new KadurMirageSystem().generate(sector);
        new KadurGehennaSystem().generate(sector);

            SharedData.getData().getPersonBountyEventData().addParticipatingFaction(KADUR_ID);

            FactionAPI kadur_remnant = sector.getFaction(KADUR_ID);
            
            // friends
            kadur_remnant.setRelationship("shadow_industry", 0.45f);              // longtime kadur trade partner and maybe the only good guys in the sector
            kadur_remnant.setRelationship("blackrock_driveyards", 0.45f);         // longtime kadur trade partner, they are kinda bad ppl tho
            kadur_remnant.setRelationship("dassault_mikoyan", 0.2f);              // kadur would be willing to trade with the russian french vector people
            kadur_remnant.setRelationship("6eme_bureau", 0.2f);                   // subfaction
            kadur_remnant.setRelationship("junk_pirates", 0.2f);                  // we are not so different, you and i
            kadur_remnant.setRelationship("pack", 0.2f);                          // we are not so different, you and i
            kadur_remnant.setRelationship("nomads", 0.2f);                        // we are not so different, you and i
            kadur_remnant.setRelationship("mayasura", 0.2f);                      // we are not so different, you and i

            // suspicious/inhospitable
            kadur_remnant.setRelationship("player", -0.2f);                         // who are you? we don't trust you
            kadur_remnant.setRelationship("independent", -0.2f);                    // who are you? we don't trust you
            kadur_remnant.setRelationship("scavengers", -0.2f);                     // who are you? we don't trust you
            kadur_remnant.setRelationship("asp_syndicate", -0.2f);                  // the theocracy's tradepartners, but they broke contact after the regime change...
            kadur_remnant.setRelationship("persean_league", -0.2f);                 // unsure if this is a league of independent worlds or a new tyranny
            // assume if your mod faction isn't listed they should be suspicious too
            // i just didn't want to clutter too much... heh
            // Tyrador
            // Tiandong
            // Metelson
            // Crystanite
            // Corvus Scavengers
            kadur_remnant.setRelationship("ORA", -0.45f);                           // something seems off about these people. unsettling
            kadur_remnant.setRelationship("SCY", -0.45f);                           // they did what they had to but they did some bad stuff
            kadur_remnant.setRelationship("sylphon", -0.45f);                       // AI ships? why would you do that. come on. you know what happens.
            kadur_remnant.setRelationship("exlane", -0.45f);                        // 
            kadur_remnant.setRelationship("fob", -0.45f);                           // do you really expect me to believe you come in peace

            // hostile/vengeful
            kadur_remnant.setRelationship("luddic_church", -0.65f);                 // the idolatry of Ludd is partially to blame for the state of the sector
            kadur_remnant.setRelationship("knights_of_ludd", -0.65f);                   // subfaction
            kadur_remnant.setRelationship("al_ars", -0.65f);                        // they're pirates... maybe?
            kadur_remnant.setRelationship("sad", -0.65f);                           // they're pirates... maybe?
            kadur_remnant.setRelationship("neutrinocorp", -0.65f);                  // bad bad corporation
            kadur_remnant.setRelationship("pirates", -0.65f);                        // they're pirates
            kadur_remnant.setRelationship("HMI", -0.65f);                            // they're pirates
            kadur_remnant.setRelationship("thulelegacy", -0.65f);                    // all who pretend to the throne of the Domain are tyrants, as they were
            kadur_remnant.setRelationship("interstellarimperium", -0.65f);           // all who pretend to the throne of the Domain are tyrants, as they were
            kadur_remnant.setRelationship("sindrian_diktat", -0.65f);                // all who pretend to the throne of the Domain are tyrants, as they were
            kadur_remnant.setRelationship("lions_guard", -0.65f);                        // subfaction
            kadur_remnant.setRelationship("tritachyon", -0.65f);                    // a phase cruiser was seen at the battle of kadur. who makes those?
            kadur_remnant.setRelationship("approlight", -0.65f);                    // AI ships AND fucky aliens
            kadur_remnant.setRelationship("immortallight", -1f);                    // AI ships AND fucky aliens AND evil
            kadur_remnant.setRelationship("cabal", -1917f);                             // special case subfaction; eat the rich
            kadur_remnant.setRelationship("hegemony", -196f);                       // we will fight you to the bitter end. oasis will be free once more
            kadur_remnant.setRelationship("luddic_path", -1f);                      // bloodthirsty heretics
            kadur_remnant.setRelationship("nullorder", -1f);                        // wait... are these the good guys? oh well. colorcoded evil, right?
            kadur_remnant.setRelationship("draco", -1f);                            // literal monsters
            kadur_remnant.setRelationship("fang", -1f);                             // literal monsters
            kadur_remnant.setRelationship("infected", -1f);                         // literal monsters
            kadur_remnant.setRelationship("derelict", -1f);                         // there is something profoundly unholy about the thinking machine
            kadur_remnant.setRelationship("remnant", -1f);                          // there is something profoundly unholy about the thinking machine
            kadur_remnant.setRelationship("mess", -1f);                             // there is something profoundly unholy about the thinking machine
            kadur_remnant.setRelationship("diableavionics", -1f);                   // garden worlds are too rare to defile, and we cannot abide slavery
            kadur_remnant.setRelationship("tahlan_legioinfernalis", -2f);           // not just pirates, space fascist ex-domain pirates that use AI
            kadur_remnant.setRelationship("blade_breakers", -1f);                   // not sure if these are AI or pirates or what, but we don't like 'em
            kadur_remnant.setRelationship("templars", -666f);                       // if there is a hell, they are born of it
            kadur_remnant.setRelationship("gmda", -1312f);                          // ACAB
            kadur_remnant.setRelationship("COPS", -1312f);                          // ACAB
            kadur_remnant.setRelationship("new_galactic_order", -1945f);            // NAZI NERDS FUCK OFF

            
    }    
}
