package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

public class VayraCheckFactions implements BaseCommand
{
    private final String DASH_LINE = "----------------------------------------------------------------------------------------------------\n";

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context)
    {
        /**
         *                 SectorManager.DO_NOT_RESPAWN_FACTIONS.add("science_fuckers");
         *                 SectorManager.DO_NOT_RESPAWN_FACTIONS.add("warhawk_republic");
         *                 SectorManager.DO_NOT_RESPAWN_FACTIONS.add("almighty_dollar");
         *                 SectorManager.DO_NOT_RESPAWN_FACTIONS.add("ashen_keepers");
         *                 SectorManager.DO_NOT_RESPAWN_FACTIONS.add("communist_clouds");
         */
        /**
         * faction,system,entityID
         * communist_clouds,Askonia,umbra
         * science_fuckers,Hybrasil,crom_cruach
         * warhawk_republic,Mayasura,mairaath
         * almighty_dollar,Aztlan,chicomoztoc
         * ashen_keepers,Eos Exodus,baetis
         * kadur_remnant,Gehenna,vayra_refugestation
         */
        // Obviously, we can run this only while on the campaign map
        if( context != CommandContext.CAMPAIGN_MAP )
        {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
        // Check the status of the following factions, print out their status and return success.
        /*
        FactionAPI scienceFuckers = Global.getSector().getFaction("science_fuckers");
        if( scienceFuckers != null )
        {
            // Probably enabled?
            FactionSpecAPI scienceFuckersSpec = scienceFuckers.getFactionSpec();
            MarketAPI market = Global.getSector().getEconomy().getMarket("crom_cruach");
            boolean marketExists = market != null;
            if( marketExists )
            {
                String marketFactionID = market.getFactionId();
                FactionAPI marketFaction = market.getFaction();
                boolean marketFactionMatchesScienceFuckers = marketFaction.equals(scienceFuckers);
                LocationAPI marketLocation = market.getContainingLocation();
                Vector2f marketLocationVector = market.getLocation();
                // Stringify all this shit
            }
        }
        */
        checkFactionAndMarketStatusAndStringifyAllToConsole("communist_clouds", "umbra");
        checkFactionAndMarketStatusAndStringifyAllToConsole("science_fuckers", "crom_cruach");
        checkFactionAndMarketStatusAndStringifyAllToConsole("warhawk_republic", "mairaath");
        checkFactionAndMarketStatusAndStringifyAllToConsole("almighty_dollar", "chicomoztoc");
        checkFactionAndMarketStatusAndStringifyAllToConsole("ashen_keepers", "baetis");
        checkFactionAndMarketStatusAndStringifyAllToConsole("kadur_remnant", "vayra_refugestation");

        return CommandResult.SUCCESS;
    }

    private void checkFactionAndMarketStatusAndStringifyAllToConsole(String factionId, String marketId) {
        StringBuilder sb = new StringBuilder();

        FactionAPI faction = Global.getSector().getFaction(factionId);
        boolean factionExists = faction != null;
        // Add some strings
        sb.append("Faction ID:\t").append(factionId).append("\n");
        sb.append(DASH_LINE);
        sb.append("\tFaction enabled:\t").append(factionExists).append("\n");
        if( factionExists )
        {
            // Probably enabled?
            FactionSpecAPI factionSpec = faction.getFactionSpec();
            String factionDisplayName = factionSpec.getDisplayName();
            String factionDisplayNameLong = factionSpec.getDisplayNameLong();

            // Stringify some shit
            sb.append("\tFaction spec:\t").append(factionSpec).append("\n");
            sb.append("\tFaction display name:\t").append(factionDisplayName).append("\n");
            sb.append("\tFaction long name:\t").append(factionDisplayNameLong).append("\n");
            sb.append("\n");

            // Assume faction hasn't spawned yet
            boolean factionSpawnedAndIsActive = false;
            MarketType targetMarketType;

            // And now, start asking questions about the faction and determine whether it is spawned and active
            MarketAPI targetMarket = Global.getSector().getEconomy().getMarket(marketId);
            boolean targetMarketExists = targetMarket != null;
            // If target market exists, great, move on, otherwise, check if it's a station instead
            MarketAPI market;
            if (!targetMarketExists) {
                SectorEntityToken station = Global.getSector().getEntityById(marketId);
                targetMarketType = MarketType.STATION;
                market = station.getMarket();
            } else {
                market = targetMarket;
                targetMarketType = MarketType.PLANET;
            }
            // Now carry on with the unified flow
            boolean marketExists = market != null;
            sb.append("\tFaction (target) market:\t").append(market).append("\n");
            sb.append("\tFaction (target) market type:\t").append(targetMarketType).append("\n");
            sb.append("\tMarket exists:\t").append(marketExists).append("\n");
            if( marketExists )
            {
                String marketFactionID = market.getFactionId();
                FactionAPI marketFaction = market.getFaction();
                boolean marketFactionMatchesActualFaction = marketFaction.equals(faction);
                boolean marketFactionIDMatchesActualFactionID = marketFactionID.equals(factionId);
                LocationAPI marketLocation = market.getContainingLocation();
                Vector2f marketLocationVector = market.getLocation();
                // Stringify all this shit
                sb.append("\tMarket Faction ID:\t").append(marketFactionID).append("\n");
                sb.append("\tMarket Faction:\t").append(marketFaction).append("\n");
                sb.append("\tMarket Faction matches actual Faction:\t").append(marketFactionMatchesActualFaction).append("\n");
                sb.append("\tMarket Faction ID matches actual Faction ID:\t").append(marketFactionIDMatchesActualFactionID).append("\n");
                sb.append("\tMarket Location:\t").append(marketLocation).append("\n");
                sb.append("\tMarket Location (vector):\t").append(marketLocationVector).append("\n");

                // Faction is spawned and active if the market's faction equals the faction we're interested in
                factionSpawnedAndIsActive = marketFactionMatchesActualFaction;
            }
            //TODO if still inactive, try to search through *ALL* planets and stations to find anything theirs
            // just to be sure
            sb.append("\n");
            sb.append("\tFaction spawned and is active: ").append(factionSpawnedAndIsActive);
            sb.append("\n\n");
            Console.showMessage(sb.toString());
        }
    }

    private enum MarketType { PLANET, STATION }
}
