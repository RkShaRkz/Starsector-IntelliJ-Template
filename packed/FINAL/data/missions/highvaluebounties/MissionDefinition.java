package data.missions.highvaluebounties;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Ping Rubin#0864 with bugs (not the insect kind please)

public class MissionDefinition implements MissionDefinitionPlugin {

    String VAYRA_SECTOR_MOD_ID = "vayrasector";    
    String BOUNTY_PATH = "data/config/vayraBounties/unique_bounty_data.csv";
    
    public static Logger log = Global.getLogger(MissionDefinition.class);
 
    // Adapted/stolen from DR's SWP Tester mission.
    // Orders by DP (supplies to recover) then by name
    public static final Comparator<FleetMemberAPI> PRIORITY = new Comparator<FleetMemberAPI>() {
        @Override
        public int compare(FleetMemberAPI member1, FleetMemberAPI member2) {
            float wt1 = member1.getStats().getSuppliesToRecover().getBaseValue();
            float wt2 = member2.getStats().getSuppliesToRecover().getBaseValue();
            if (Float.compare(wt2, wt1) == 0) {
                if (member1.getHullSpec().getHullName().compareTo(member2.getHullSpec().getHullName()) != 0) {
                    return member1.getHullSpec().getHullName().compareTo(member2.getHullSpec().getHullName());
                } else {
                    return member1.getId().compareTo(member2.getId());
                }
            } else {
                return Float.compare(wt2, wt1);
            }
        }
    };    
    
    private final Set<FleetMemberAPI> ships = new TreeSet<>(PRIORITY);
    private final List<String> sources = new ArrayList<>();
    
    public List<ModSpecAPI> enabledMods = Global.getSettings().getModManager().getEnabledModsCopy();
    
    @Override
    public void defineMission(MissionDefinitionAPI api) {
         
        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "HVB", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "TAR", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "HVB Ships");
        api.setFleetTagline(FleetSide.ENEMY, "Target Practice");

        
        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        //api.addBriefingItem("HVB Mods enabled: "+<<FIGURE OUT HOW TO DO THIS SMARTLY>>);
        
        //Grab all list of HVB ships
        try {
            JSONArray bounty = Global.getSettings().getMergedSpreadsheetDataForMod("bounty_id", BOUNTY_PATH, VAYRA_SECTOR_MOD_ID);
            int numRows = bounty.length();
            for (int i=0; i<numRows; i++) {
                JSONObject row = bounty.getJSONObject(i);
                String variantName = row.getString("flagshipVariantId");
                String shipName = row.getString("flagshipName");
                if (!variantName.isEmpty()) {
                    FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantName);
                    member.setShipName(shipName);
                    ships.add(member);
                }
                String source = row.getString("fs_rowSource");
                sources.add(source);
            }            
        } catch (IOException | JSONException ex) {
            log.info("Couldn't fetch HVBs.");
        }
        
        //Figure out which mods add HVBs
        List<String> modsWithHVBs = new ArrayList<>();
        Map<String, String> nameAndPath = new HashMap<>();
        for (ModSpecAPI mod : enabledMods) {    
            String name = mod.getName();
            String path = mod.getPath();
            nameAndPath.put(path, name);
        } 
        for (String source : sources) {
            for (String path : nameAndPath.keySet()) {
                String modName = nameAndPath.get(path);
                if (source.contains(path) && !modsWithHVBs.contains(modName)) {
                    modsWithHVBs.add(modName);
                }
            }
        }
        
        api.addBriefingItem("");
        api.addBriefingItem("Added HVB flagships from: "+modsWithHVBs);

        boolean FIRST = true;
        for (FleetMemberAPI ship : ships) {
            String variant = ship.getVariant().getHullVariantId();
            String name = ship.getShipName();
            api.addToFleet(FleetSide.PLAYER, variant, FleetMemberType.SHIP, name, FIRST);
            if (FIRST) {
                FIRST = false;
            }
        }

        // Set up the enemy fleet.
        //api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, "Fire of the Phoenix", false);
        //api.addToFleet(FleetSide.ENEMY, "paragon_Elite", FleetMemberType.SHIP, "Cosmic Monarch", false);
        api.addToFleet(FleetSide.ENEMY, "radiant_Standard", FleetMemberType.SHIP, "40REM", false);
        //api.addToFleet(FleetSide.ENEMY, "guardian_Standard", FleetMemberType.SHIP, "40DER", false);

        // Set up the map.
        float width = 12000f;
        float height = 12000f;
        api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        float minX = -width/2;
        float minY = -height/2;

        api.addPlanet(0, 0, 50f, StarTypes.RED_GIANT, 250f, true);

    }

}