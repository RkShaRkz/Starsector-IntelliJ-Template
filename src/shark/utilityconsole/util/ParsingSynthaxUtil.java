package shark.utilityconsole.util;

import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.console.Console;
import shark.utilityconsole.util.searching.ParameterCriterion;

import static shark.utilityconsole.util.searching.ParameterCriterion.CriteriaParameter.ShipParameter.*;
import static shark.utilityconsole.util.searching.ParameterCriterion.CriteriaParameter.WeaponParameter.*;

public class ParsingSynthaxUtil {

    public static boolean isQuantitySymbol(String symbol) {
        /**
         * <, <=, =, >, >=
         */
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("<") || symbol.equalsIgnoreCase("<=") || symbol.equalsIgnoreCase("=") || symbol.equalsIgnoreCase(">") || symbol.equalsIgnoreCase(">=");

        return retVal;
    }

    public static ParameterCriterion.CriteriaQuantity.Quantity remapQuantitySymbol(String symbol) {
        /**
         * <, <=, =, >, >=
         */
        ParameterCriterion.CriteriaQuantity.Quantity retVal = null;
        if (symbol.equalsIgnoreCase("<")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.LESS_THAN;
        } else if (symbol.equalsIgnoreCase("<=")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.LESS_OR_EQUAL;
        } else if (symbol.equalsIgnoreCase("=")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.EXACTLY;
        } else if (symbol.equalsIgnoreCase(">=")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.MORE_OR_EQUAL;
        } else if (symbol.equalsIgnoreCase(">")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.MORE_THAN;
        } else {
            Console.showMessage("Invalid symbol to remap to QuantitySymbol! Received " + symbol);
        }

        return retVal;
    }

    public static boolean isWeaponSizeSymbol(String symbol) {
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("large") || symbol.equalsIgnoreCase("medium") || symbol.equalsIgnoreCase("small");

        return retVal;
    }

    public static WeaponAPI.WeaponSize remapWeaponSizeSymbol(String symbol) {
        WeaponAPI.WeaponSize retVal = null;
        if (symbol.equalsIgnoreCase("small")) {
            retVal = WeaponAPI.WeaponSize.SMALL;
        } else if (symbol.equalsIgnoreCase("medium")) {
            retVal = WeaponAPI.WeaponSize.MEDIUM;
        } else if (symbol.equalsIgnoreCase("large")) {
            retVal = WeaponAPI.WeaponSize.LARGE;
        } else {
            Console.showMessage("Invalid symbol to remap to WeaponSize! Received " + symbol);
        }

        return retVal;
    }

    public static boolean isWeaponSymbol(String symbol) {
        /**
         * 		BALLISTIC("Ballistic"),
         * 		ENERGY("Energy"),
         * 		MISSILE("Missile"),
         * 		LAUNCH_BAY("Launch Bay"),
         * 		UNIVERSAL("Universal"),
         * 		HYBRID("Hybrid"),
         * 		SYNERGY("Synergy"),
         * 		COMPOSITE("Composite"),
         * 		BUILT_IN("Built in"),
         * 		DECORATIVE("Decorative"),
         * 		SYSTEM("System"),
         * 		STATION_MODULE("Station Module");
         */

        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("Ballistic")
            || symbol.equalsIgnoreCase("Energy")
            || symbol.equalsIgnoreCase("Missile")
            || symbol.equalsIgnoreCase("LaunchBay") || symbol.equalsIgnoreCase("Launch-Bay") || symbol.equalsIgnoreCase("Launch_Bay") || symbol.equalsIgnoreCase("LaunchBays")
            || symbol.equalsIgnoreCase("Universal")
            || symbol.equalsIgnoreCase("Hybrid")
            || symbol.equalsIgnoreCase("Synergy")
            || symbol.equalsIgnoreCase("Composite")
            || symbol.equalsIgnoreCase("Builtin")
            || symbol.equalsIgnoreCase("Decorative")
            || symbol.equalsIgnoreCase("System")
            || symbol.equalsIgnoreCase("StationModule");

        return retVal;
    }

    public static WeaponAPI.WeaponType remapWeaponSymbol(String symbol) {
        WeaponAPI.WeaponType retVal = null;

        if (symbol.equalsIgnoreCase("Ballistic")) {
            retVal = WeaponAPI.WeaponType.BALLISTIC;
        } else if (symbol.equalsIgnoreCase("Energy")) {
            retVal = WeaponAPI.WeaponType.ENERGY;
        } else if (symbol.equalsIgnoreCase("Missile")) {
            retVal = WeaponAPI.WeaponType.MISSILE;
        } else if (symbol.equalsIgnoreCase("LaunchBay") || symbol.equalsIgnoreCase("Launch-Bay") || symbol.equalsIgnoreCase("Launch_Bay") || symbol.equalsIgnoreCase("LaunchBays")) {
            retVal = WeaponAPI.WeaponType.LAUNCH_BAY;
        } else if (symbol.equalsIgnoreCase("Universal")) {
            retVal = WeaponAPI.WeaponType.UNIVERSAL;
        } else if (symbol.equalsIgnoreCase("Hybrid")) {
            retVal = WeaponAPI.WeaponType.HYBRID;
        } else if (symbol.equalsIgnoreCase("Synergy")) {
            retVal = WeaponAPI.WeaponType.SYNERGY;
        } else if (symbol.equalsIgnoreCase("Composite")) {
            retVal = WeaponAPI.WeaponType.COMPOSITE;
        } else if (symbol.equalsIgnoreCase("Builtin")) {
            retVal = WeaponAPI.WeaponType.BUILT_IN;
        } else if (symbol.equalsIgnoreCase("Decorative")) {
            retVal = WeaponAPI.WeaponType.DECORATIVE;
        } else if (symbol.equalsIgnoreCase("System")) {
            retVal = WeaponAPI.WeaponType.SYSTEM;
        } else if (symbol.equalsIgnoreCase("StationModule")) {
            retVal = WeaponAPI.WeaponType.STATION_MODULE;
        } else {
            Console.showMessage("Invalid symbol to remap to WeaponType! Received " + symbol);
        }

        return retVal;
    }

    public static boolean isShipParameterSymbol(String symbol) {
        /**
         * CARGO, FUEL, CREW, HITPOINTS, ARMOR, FLUX_CAPACITY, FLUX_DISSIPATION, SIZE, ORDNANCE_POINTS, DEPLOYMENT_POINTS
         */
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("CARGO")
            || symbol.equalsIgnoreCase("FUEL")
            || symbol.equalsIgnoreCase("CREW")
            || symbol.equalsIgnoreCase("HITPOINTS")
            || symbol.equalsIgnoreCase("ARMOR")
            || symbol.equalsIgnoreCase("FLUX_CAPACITY") || symbol.equalsIgnoreCase("FLUX-CAPACITY") || symbol.equalsIgnoreCase("FLUXCAPACITY")
            || symbol.equalsIgnoreCase("FLUX_DISSIPATION") || symbol.equalsIgnoreCase("FLUX-DISSIPATION") || symbol.equalsIgnoreCase("FLUXDISSIPATION")
            || symbol.equalsIgnoreCase("ORDNANCE_POINTS") || symbol.equalsIgnoreCase("ORDNANCE-POINTS") || symbol.equalsIgnoreCase("ORDNANCEPOINTS") || symbol.equalsIgnoreCase("OP")
            || symbol.equalsIgnoreCase("SIZE")
            || symbol.equalsIgnoreCase("DEPLOYMENT_POINTS") || symbol.equalsIgnoreCase("DEPLOYMENT-POINTS") || symbol.equalsIgnoreCase("DEPLOYMENTPOINTS") || symbol.equalsIgnoreCase("DP")
        ;


        return retVal;
    }

    public static ParameterCriterion.CriteriaParameter.ShipParameter remapShipParameterSymbol(String symbol) {
        /**
         * CARGO, FUEL, CREW, HITPOINTS, ARMOR, FLUX_CAPACITY, FLUX_DISSIPATION, SIZE
         */
        ParameterCriterion.CriteriaParameter.ShipParameter retVal = null;
        if (symbol.equalsIgnoreCase("CARGO")) {
            retVal = CARGO;
        } else if (symbol.equalsIgnoreCase("FUEL")) {
            retVal = FUEL;
        } else if (symbol.equalsIgnoreCase("CREW")) {
            retVal = CREW;
        } else if (symbol.equalsIgnoreCase("HITPOINTS")) {
            retVal = HITPOINTS;
        } else if (symbol.equalsIgnoreCase("ARMOR")) {
            retVal = ARMOR;
        } else if (symbol.equalsIgnoreCase("FLUX_CAPACITY") || symbol.equalsIgnoreCase("FLUX-CAPACITY") || symbol.equalsIgnoreCase("FLUXCAPACITY")) {
            retVal = FLUX_CAPACITY;
        } else if (symbol.equalsIgnoreCase("FLUX_DISSIPATION") || symbol.equalsIgnoreCase("FLUX-DISSIPATION") || symbol.equalsIgnoreCase("FLUXDISSIPATION")) {
            retVal = FLUX_DISSIPATION;
        } else if (symbol.equalsIgnoreCase("ORDNANCE_POINTS") || symbol.equalsIgnoreCase("ORDNANCE-POINTS") || symbol.equalsIgnoreCase("ORDNANCEPOINTS") || symbol.equalsIgnoreCase("OP")) {
            retVal = ORDNANCE_POINTS;
        } else if (symbol.equalsIgnoreCase("SIZE")) {
            retVal = ParameterCriterion.CriteriaParameter.ShipParameter.SIZE;
        } else if (symbol.equalsIgnoreCase("DEPLOYMENT_POINTS") || symbol.equalsIgnoreCase("DEPLOYMENT-POINTS") || symbol.equalsIgnoreCase("DEPLOYMENTPOINTS") || symbol.equalsIgnoreCase("DP")) {
            retVal = DEPLOYMENT_POINTS;
        } else {
            Console.showMessage("Invalid symbol to remap to ShipParameter! Received " + symbol);
        }

        return retVal;
    }

    public static boolean isWeaponParameterSymbol(String symbol) {
        /**
         * RANGE AMMO TYPE DAMAGE_TYPE  DAMAGE-TYPE DAMAGETYPE TURN_RATE TURN-RATE TURNRATE MOUNT_TYPE MOUNT-TYPE MOUNTTYPE SIZE  RARITY BEAM
         */
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("AMMO")
            || symbol.equalsIgnoreCase("RANGE")
            || symbol.equalsIgnoreCase("BEAM")
            || symbol.equalsIgnoreCase("rarity")
            || symbol.equalsIgnoreCase("type")
            || symbol.equalsIgnoreCase("mounttype") || symbol.equalsIgnoreCase("mount-type") || symbol.equalsIgnoreCase("mount_type")
            || symbol.equalsIgnoreCase("damagetype") || symbol.equalsIgnoreCase("damage-type") || symbol.equalsIgnoreCase("damage_type")
            || symbol.equalsIgnoreCase("turnrate") || symbol.equalsIgnoreCase("turn-rate") || symbol.equalsIgnoreCase("turn_rate")
            || symbol.equalsIgnoreCase("SIZE");

        return retVal;
    }

    public static ParameterCriterion.CriteriaParameter.WeaponParameter remapWeaponParameterSymbol(String symbol) {
        /**
         * RANGE, AMMO, TYPE, DAMAGE_TYPE, TURN_RATE, MOUNT_TYPE, SIZE, RARITY, BEAM
         */

        ParameterCriterion.CriteriaParameter.WeaponParameter retVal = null;
        if (symbol.equalsIgnoreCase("RANGE")) {
            retVal = RANGE;
        } else if (symbol.equalsIgnoreCase("AMMO")) {
            retVal = AMMO;
        } else if (symbol.equalsIgnoreCase("TYPE")) {
            retVal = TYPE;
        } else if (symbol.equalsIgnoreCase("DAMAGE_TYPE") || symbol.equalsIgnoreCase("DAMAGE-TYPE") || symbol.equalsIgnoreCase("DAMAGETYPE")) {
            retVal = DAMAGE_TYPE;
        } else if (symbol.equalsIgnoreCase("TURN_RATE") || symbol.equalsIgnoreCase("TURN-RATE") || symbol.equalsIgnoreCase("TURNRATE")) {
            retVal = TURN_RATE;
        } else if (symbol.equalsIgnoreCase("MOUNT_TYPE") || symbol.equalsIgnoreCase("MOUNT-TYPE") || symbol.equalsIgnoreCase("MOUNTTYPE")) {
            retVal = MOUNT_TYPE;
        } else if (symbol.equalsIgnoreCase("SIZE")) {
            retVal = ParameterCriterion.CriteriaParameter.WeaponParameter.SIZE;
        } else if (symbol.equalsIgnoreCase("RARITY")) {
            retVal = RARITY;
        } else if (symbol.equalsIgnoreCase("BEAM")) {
            retVal = BEAM;
        } else {
            Console.showMessage("Invalid symbol to remap to WeaponParameter! Received " + symbol);
        }

        return retVal;
    }
}
