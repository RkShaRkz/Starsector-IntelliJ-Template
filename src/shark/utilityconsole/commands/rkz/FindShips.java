package shark.utilityconsole.commands.rkz;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import shark.utilityconsole.util.CommonUtil;
import shark.utilityconsole.util.searching.ParameterCriterion;
import shark.utilityconsole.util.searching.SearchCriteria;

import java.util.*;

import static shark.utilityconsole.util.searching.ParameterCriterion.CriteriaParameter.Criteria.*;
import static shark.utilityconsole.util.searching.ParameterCriterion.CriteriaParameter.ShipParameter.*;

public class FindShips implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        CommandResult retVal = null;
        /**
         *
         * findships launchbay                  // lists all ships with launchbays, starting from most to least
         * findships launchbay 4                //lists all ships with 4+ launchbays
         * findships {ballistic,hybrid}         //lists all ships with ballistic and hybrid bays
         * findships fuel 500                   // lists all ships that can carry more than 500 fuel
         *
         * findships { ballistic > 3,hybrid = 1 } // lists all ships with exactly 1 hybrid and over 3 ballistic slots
         * findships { ballistic > 2,cargo > 500 }// lists all ships with at least 2 ballistic and at least 500 cargo
         *
         * findships large ballistic > 2        // lists all ships with over 2 large ballistic slots
         *
         * findships capital
         *
         * Now, regarding the grammar. It is obvious that if we,re looking for more than one parameter, that it will start
         * with a { - so that should be checked first.
         *
         * Afterwards, we should look for size, as it is optional.
         */
        ParameterCriterion.CriteriaParameter.Criteria criteria = null;

        WeaponAPI.WeaponSize weaponSize = null;
        WeaponAPI.WeaponType weaponType = null;

        ParameterCriterion.CriteriaParameter.ShipParameter shipParameter = null;

        ParameterCriterion.CriteriaQuantity criteriaQuantity = null;
        ParameterCriterion.CriteriaQuantity.Quantity quantityType = null;
        int quantity = 0;

        // SPECIAL CASE massaging:
        // in case we're dealing with a SIZE parameter, the rest of the code expects a number for everything
        // however, it would be more natural to use the following command
        //
        // findships size > frigate
        //
        // so the code that follows will fail for more complex queries such as
        // findships {size > frigate,ballistic > 3}
        // so it's best if we just look for ship-size related words in the input
        // and immediatelly just map them to numbers to make everything easier.
        if (containsShipSizes(args)) {
            // an even more special case
            // consider the fact that the queries could be
            //
            // findships capital
            // findships size > frigate
            //
            // in first case, we want to replace it with "size = 5"
            // in second case, we just want to replace "frigate" with "2".
            //
            // so if it contains only shipsize words without the "size" param, replace with full expression
            // otherwise, replace with ordinal instead. sheesh.
            if (args.toLowerCase().contains("size")) {
                // hopefully has "size <quantity>" so just replace the shipsize with ordinal
                args = replaceShipSizesWithOrdinals(args);
            } else {
                // replace the ship size with a "size = <ordinal> expression
                args = replaceShipSizeWithExpression(args);
            }
        }

        if (args.startsWith("{")) {
            // we are dealing with a list of params
            // also make sure it ends with } and call bad syntax if it does not.
        } else {
            // only one parameter (but would still be a list of keywords)
            String[] symbols = args.split(" ");
            // Lets turn this into a list, since we can remove from lists
            ArrayList<String> symbolList = new ArrayList<String>(Arrays.asList(symbols));
            // check first symbol for size
            if (isSizeSymbol(symbolList.get(0).trim())) {
                // since it is a size, we can now determine two things:
                // 1. we are going to be dealing with a WEAPON_WITH_SIZE query
                // 2. the actual size as well
                criteria = WEAPON_WITH_SIZE;
                weaponSize = remapSizeSymbol(symbolList.get(0).trim());

                // remove this element so the rest of the code can be much more straightforward
                symbolList.remove(0);

                // at this point, our query could have ended. in that case, the implicit criteria quantity is AT_LEAST 0
                // otherwise, parse the criteria quantity as well
            }

            // Now we should check if the next symbol is a weapon, or a ship parameter
            // at this point, there is nothing else our grammar supports, so if it's neither - syntax error
            if (isWeaponSymbol(symbolList.get(0).trim())) {
                // Since it's a weapon, we can determine two things:
                // 1. we are going to be dealing with a WEAPON query
                // 2. the actual weapon
                criteria = WEAPON;
                weaponType = remapWeaponSymbol(symbolList.get(0).trim());

                // remove this element so the rest of the code can be much more straightforward
                symbolList.remove(0);
            } else if (isShipParameterSymbol(symbolList.get(0).trim())) {
                // Since it's a ship parameter, we can determine two things:
                // 1. we're going to be dealing with a SHIP_PARAMETER query
                // 2. the actual parameter
                criteria = SHIP_PARAMETER;
                shipParameter = remapShipParameterSymbol(symbolList.get(0).trim());

                // remove this element so the rest of the code can be much more straightforward
                symbolList.remove(0);
            } else {
                retVal = CommandResult.BAD_SYNTAX;
            }

            // Now, presuming everything was alright, we have three possible scenarios:
            // 1. the syntax was bad, so just return BAD_SYNTAX result
            // 2. the query is over, so we're dealing with a query that has an implicit quantity of AT_LEAST 0
            // 3. the query is not over, so we should parse the CriteriaQuantity

            // if retVal is set - do an early return; ugly but easy way out.
            if (retVal != null) return retVal;

            if (symbolList.isEmpty()) {
                criteriaQuantity = new ParameterCriterion.CriteriaQuantity(
                        ParameterCriterion.CriteriaQuantity.Quantity.AT_LEAST,
                        0
                );
            } else {
                // parse quantity - we should have exactly 2 symbols now.
                // first should be a Quantity symbol, the other should be quantity.
                // verify for size, if sizecheck fails - set retval;

                if (symbolList.size() == 2) {
                    // great, parse them if they pass the rules
                    if (isQuantitySymbol(symbolList.get(0).trim())) {
                        quantityType = remapQuantitySymbol(symbolList.get(0).trim());

                        try {
                            quantity = Integer.parseInt(symbolList.get(1).trim());
                        } catch (NumberFormatException nfe) {
                            Console.showMessage("Wrong input for quantity! Expected a number, received " + symbolList.get(1));
                            retVal = CommandResult.BAD_SYNTAX;
                        }
                    }

                    criteriaQuantity = new ParameterCriterion.CriteriaQuantity(
                            quantityType,
                            quantity
                    );
                } else {
                    Console.showMessage("Wrong input for quantity check! Expected an operation (< or = or >) followed by a number, received " + Arrays.deepToString(symbolList.toArray()));
                    retVal = CommandResult.BAD_SYNTAX;
                }
            }

            //TODO move this outside of this if/else below
            // Now that we have everything, lets run the query.
            ParameterCriterion searchParams = constructParameterCriteria(criteria, weaponSize, weaponType, shipParameter, criteriaQuantity);
            final SearchCriteria searchCriteria = new SearchCriteria(Collections.singletonList(searchParams));

            CommonUtil.findShips(searchCriteria, new CommonUtil.FindShipsListener() {
                @Override
                public void onShipsFound(List<ShipHullSpecAPI> foundShips, int queriedShips) {
                    StringBuilder sb = new StringBuilder();
                    sb
                            .append("Found ").append(foundShips.size())
                            .append(" results out of ").append(queriedShips)
                            .append(" queried entries that meet the criteria\n");

                    // Now, lets sort the list from highest to lowest, starting with the biggest ships.
                    // "highest to lowest" as in "if we were looking for more than 3 ballistic slots, ships with 5
                    // go before those with 4 which go before those with 3".
                    Comparator<ShipHullSpecAPI> comparator = new Comparator<ShipHullSpecAPI>() {
                        @Override
                        public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                            // So far the idea is simple; we will rely on the user to have entered the criteria from
                            // highest priority to lowest priority.
                            //
                            // As such, any case where there are multiple criteriums will be resolved by their
                            // ordering in the criterium list
                            return searchCriteria.compareResults(o1, o2);
                        }
                    };
                    Collections.sort(foundShips, comparator);

                    for(ShipHullSpecAPI ship : foundShips) {
                        stringifyShipIntoStringBuilder(sb, ship);
                    }
                    Console.showMessage(sb.toString());
                }
            });
        }
        //TODO move here

        /*
        CommonUtil.getAllShips(
                new SearchCriteria(
                        Arrays.asList(
                                new ParameterCriterion(
                                        new ParameterCriterion.CriteriaParameter(
                                                WEAPON_WITH_SIZE,
                                                new ParameterCriterion.CriteriaParameter.WeaponAndSizeCriteriaData(
                                                        WeaponAPI.WeaponType.BALLISTIC,
                                                        WeaponAPI.WeaponSize.LARGE
                                                )
                                        ),
                                        new ParameterCriterion.CriteriaQuantity(
                                                ParameterCriterion.CriteriaQuantity.Quantity.AT_LEAST,
                                                2
                                        )
                                ),
                                new ParameterCriterion(
                                        new ParameterCriterion.CriteriaParameter(
                                                WEAPON_WITH_SIZE,
                                                new ParameterCriterion.CriteriaParameter.WeaponAndSizeCriteriaData(
                                                        WeaponAPI.WeaponType.BALLISTIC,
                                                        WeaponAPI.WeaponSize.LARGE
                                                )
                                        ),
                                        new ParameterCriterion.CriteriaQuantity(
                                                ParameterCriterion.CriteriaQuantity.Quantity.AT_LEAST,
                                                2
                                        )
                                )
                        )
                )
        );
         */

        return CommandResult.SUCCESS;
    }

    private boolean isSizeSymbol(String symbol) {
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("large") || symbol.equalsIgnoreCase("medium") || symbol.equalsIgnoreCase("small");

        return retVal;
    }

    private WeaponAPI.WeaponSize remapSizeSymbol(String symbol) {
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

    private boolean isWeaponSymbol(String symbol) {

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
                || symbol.equalsIgnoreCase("LaunchBay") || symbol.equalsIgnoreCase("Launch-Bay") || symbol.equalsIgnoreCase("Launch_Bay")
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

    private WeaponAPI.WeaponType remapWeaponSymbol(String symbol) {
        WeaponAPI.WeaponType retVal = null;

        if (symbol.equalsIgnoreCase("Ballistic")) {
            retVal = WeaponAPI.WeaponType.BALLISTIC;
        } else if (symbol.equalsIgnoreCase("Energy")) {
            retVal = WeaponAPI.WeaponType.ENERGY;
        } else if (symbol.equalsIgnoreCase("Missile")) {
            retVal = WeaponAPI.WeaponType.MISSILE;
        } else if (symbol.equalsIgnoreCase("LaunchBay") || symbol.equalsIgnoreCase("Launch-Bay") || symbol.equalsIgnoreCase("Launch_Bay")) {
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

    private boolean isShipParameterSymbol(String symbol) {
        /**
         * CARGO, FUEL, CREW, HITPOINTS, ARMOR, FLUX_CAPACITY, FLUX_DISSIPATION, SIZE
         */
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("CARGO")
                || symbol.equalsIgnoreCase("FUEL")
                || symbol.equalsIgnoreCase("CREW")
                || symbol.equalsIgnoreCase("HITPOINTS")
                || symbol.equalsIgnoreCase("ARMOR")
                || symbol.equalsIgnoreCase("FLUX_CAPACITY") || symbol.equalsIgnoreCase("FLUX-CAPACITY") || symbol.equalsIgnoreCase("FLUXCAPACITY")
                || symbol.equalsIgnoreCase("FLUX_DISSIPATION") || symbol.equalsIgnoreCase("FLUX-DISSIPATION") || symbol.equalsIgnoreCase("FLUXDISSIPATION")
                || symbol.equalsIgnoreCase("SIZE");

        return retVal;
    }

    private ParameterCriterion.CriteriaParameter.ShipParameter remapShipParameterSymbol(String symbol) {
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
        } else if (symbol.equalsIgnoreCase("SIZE")) {
            retVal = SIZE;
        } else {
            Console.showMessage("Invalid symbol to remap to ShipParameter! Received " + symbol);
        }

        return retVal;
    }

    private boolean isQuantitySymbol(String symbol) {
        /**
         * <, =, >
         */
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("<") || symbol.equalsIgnoreCase("=") || symbol.equalsIgnoreCase(">");

        return retVal;
    }

    private ParameterCriterion.CriteriaQuantity.Quantity remapQuantitySymbol(String symbol) {
        /**
         * <, =, >
         */
        ParameterCriterion.CriteriaQuantity.Quantity retVal = null;
        if (symbol.equalsIgnoreCase("<")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.AT_MOST;
        } else if (symbol.equalsIgnoreCase("=")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.EXACTLY;
        } else if (symbol.equalsIgnoreCase(">")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.AT_LEAST;
        } else {
            Console.showMessage("Invalid symbol to remap to QuantitySymbol! Received " + symbol);
        }

        return retVal;
    }

    //constructSearchCriteria(criteria, weaponSize, weaponType, shipParameter, criteriaQuantity, quantityType, quantity);
    private ParameterCriterion constructParameterCriteria(ParameterCriterion.CriteriaParameter.Criteria criteria,
                                                          WeaponAPI.WeaponSize weaponSize,
                                                          WeaponAPI.WeaponType weaponType,
                                                          ParameterCriterion.CriteriaParameter.ShipParameter shipParameter,
                                                          ParameterCriterion.CriteriaQuantity criteriaQuantity) {
        ParameterCriterion retVal = null;

        // now make a context-sensitive ParameterCriterion based on the data we have parsed from the input data.console command
        // Depending on the criteria, we will have to instantiate different data containers
        ParameterCriterion.CriteriaParameter.CriteriaData data = null;

        switch (criteria) {
            case WEAPON:
                data = new ParameterCriterion.CriteriaParameter.WeaponCriteriaData(weaponType);
                break;
            case WEAPON_WITH_SIZE:
                data = new ParameterCriterion.CriteriaParameter.WeaponAndSizeCriteriaData(weaponType, weaponSize);
                break;
            case SHIP_PARAMETER:
                data = new ParameterCriterion.CriteriaParameter.ShipParameterCriteriaData(shipParameter);
                break;

            default:
                Console.showMessage("Unsupported criteria encountered in constructParameterCriteria! Received " + criteria);
        }
        ParameterCriterion.CriteriaParameter cp = new ParameterCriterion.CriteriaParameter(criteria, data);
        retVal = new ParameterCriterion(cp, criteriaQuantity);

        return retVal;
    }

    public boolean containsShipSizes(String fullInputString) {
        boolean retVal = false;
        retVal = fullInputString.toLowerCase().contains("fighter")
                || fullInputString.toLowerCase().contains("frigate")
                || fullInputString.toLowerCase().contains("destroyer")
                || fullInputString.toLowerCase().contains("cruiser")
                || fullInputString.toLowerCase().contains("capital") || fullInputString.toLowerCase().contains("capital-ship") || fullInputString.toLowerCase().contains("capital_ship");

        return retVal;
    }

    public String replaceShipSizesWithOrdinals(String fullInputString) {
        // We don't really care about the position of the shipsize word,
        // just replace all of them in one go, it'll hit what it hits, and call it done.
        return fullInputString.toLowerCase()
                .replaceAll("fighter", String.valueOf(ShipAPI.HullSize.FIGHTER.ordinal()))
                .replaceAll("frigate", String.valueOf(ShipAPI.HullSize.FRIGATE.ordinal()))
                .replaceAll("destroyer", String.valueOf(ShipAPI.HullSize.DESTROYER.ordinal()))
                .replaceAll("cruiser", String.valueOf(ShipAPI.HullSize.CRUISER.ordinal()))
                .replaceAll("capital", String.valueOf(ShipAPI.HullSize.CAPITAL_SHIP.ordinal()))
                .replaceAll("capital-ship", String.valueOf(ShipAPI.HullSize.CAPITAL_SHIP.ordinal()))
                .replaceAll("capital_ship", String.valueOf(ShipAPI.HullSize.CAPITAL_SHIP.ordinal()));
    }

    public String replaceShipSizeWithExpression(String fullInputString) {
        // While we generally don't care about the position of the "just the shipsize word",
        // this time we want to replace it with an expression that limits to just that particular size
        return fullInputString.toLowerCase()
                .replaceAll("fighter", "size = " + ShipAPI.HullSize.FIGHTER.ordinal())
                .replaceAll("frigate", "size = " + ShipAPI.HullSize.FRIGATE.ordinal())
                .replaceAll("destroyer", "size = " + ShipAPI.HullSize.DESTROYER.ordinal())
                .replaceAll("cruiser", "size = " + ShipAPI.HullSize.CRUISER.ordinal())
                .replaceAll("capital", "size = " + ShipAPI.HullSize.CAPITAL_SHIP.ordinal())
                .replaceAll("capital-ship", "size = " + ShipAPI.HullSize.CAPITAL_SHIP.ordinal())
                .replaceAll("capital_ship", "size = " + ShipAPI.HullSize.CAPITAL_SHIP.ordinal());
    }

    public void stringifyShipIntoStringBuilder(StringBuilder sb, ShipHullSpecAPI ship) {
        sb
                .append("Hull name: ").append(String.format("%32s",ship.getHullName()))
                .append("\t\t\tHull ID: ").append(String.format("%64s",ship.getHullId()))
                .append("\n");
    }
}
