package shark.utilityconsole.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.sun.javafx.beans.annotations.NonNull;
import org.lazywizard.console.BaseCommand;
import shark.utilityconsole.util.searching.ParameterCriterion;
import shark.utilityconsole.util.searching.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    /**
     * Listener for {@link #getAllPlayerShips(BaseCommand.CommandContext, GetAllShipsListener)}
     */
    public interface GetAllShipsListener {

        /**
         * Called when ships are fetched while we're in combat
         * @param combatShips all ships (currently in the combat) as {@link ShipAPI}
         * @param fleetMembers all ships (currently in the combat) as {@link FleetMemberAPI}
         */
        void onGetAllShipsCombat(List<ShipAPI> combatShips, List<FleetMemberAPI> fleetMembers);

        /**
         * Called when ships are fetched while we're in the campaign screen (outside of combat)
         * @param campaignShips all owned ships as {@link FleetMemberAPI}
         */
        void onGetAllShipsCampaign(List<FleetMemberAPI> campaignShips);
    }

    /**
     * Method that returns either all deployed ships, or all owned fleet ships
     * @param context  the command context, so it knows whether to get combat ships or campaign ships
     * @param listener listener to be invoked
     */
    public static void getAllPlayerShips(BaseCommand.CommandContext context, GetAllShipsListener listener) {
        if (context.isInCombat()) {
            List<ShipAPI> combatShips = new ArrayList<ShipAPI>();
            List<FleetMemberAPI> combatFleetMembers = new ArrayList<FleetMemberAPI>();
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                FleetMemberAPI fleetMember = Global.getCombatEngine().getFleetManager(ship.getOwner()).getDeployedFleetMember(ship).getMember();
                combatShips.add(ship);
                combatFleetMembers.add(fleetMember);
            }
            listener.onGetAllShipsCombat(combatShips, combatFleetMembers);
        } else {
            CampaignFleetAPI fleetAPI = Global.getSector().getPlayerFleet();
            listener.onGetAllShipsCampaign(fleetAPI.getMembersWithFightersCopy());
        }
    }

    /**
     * Listener for {@link #findShips(SearchCriteria, FindShipsListener)}
     */
    public interface FindShipsListener {
        /**
         * Called when the searching has been done
         * @param foundShips List containing ships that matched the {@link SearchCriteria}
         * @param queriedShips number of ships that were queried for meeting the criteria
         */
        void onShipsFound(List<ShipHullSpecAPI> foundShips, int queriedShips);
    }


    /**
     * Returns all {@link ShipHullSpecAPI}s matchin a certain {@link SearchCriteria}.
     * @param searchCriteria the criteria that needs to be satisfied for the ship to be included in the returned list
     */
    public static void findShips(@NonNull SearchCriteria searchCriteria,@NonNull FindShipsListener listener) {
        List<ShipHullSpecAPI> shipSpecs = Global.getSettings().getAllShipHullSpecs();
        List<ShipHullSpecAPI> filteredResults = new ArrayList<ShipHullSpecAPI>();

        // We will go through all ship hull specifications, and add all the ones that pass requirements to a filtered list
        for (ShipHullSpecAPI ship : shipSpecs) {
            if (searchCriteria.matches(ship)) {
                filteredResults.add(ship);
            }
        }

        listener.onShipsFound(filteredResults, shipSpecs.size());
    }

    /**
     * Listener for {@link #findShips(SearchCriteria, FindShipsListener)}
     */
    public interface FindWeaponsListener {
        /**
         * Called when the searching has been done
         * @param foundWeapons List containing weapons that matched the {@link SearchCriteria}
         * @param queriedWeapons number of weapons that were queried for meeting the criteria
         */
        void onWeaponsFound(List<WeaponSpecAPI> foundWeapons, int queriedWeapons);
    }

    public static void findWeapons(@NonNull SearchCriteria searchCriteria, @NonNull FindWeaponsListener listener ) {
        List<WeaponSpecAPI> weaponSpecs = Global.getSettings().getAllWeaponSpecs();
        List<WeaponSpecAPI> filteredResults = new ArrayList<>();

        // We will go through all ship hull specifications, and add all the ones that pass requirements to a filtered list
        for (WeaponSpecAPI weapon : weaponSpecs) {
            if (searchCriteria.matches(weapon)) {
                filteredResults.add(weapon);
            }
        }

        listener.onWeaponsFound(filteredResults, weaponSpecs.size());
    }
}
