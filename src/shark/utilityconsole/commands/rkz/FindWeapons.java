package shark.utilityconsole.commands.rkz;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import shark.utilityconsole.util.CommonUtil;
import shark.utilityconsole.util.searching.*;

import java.util.*;

import static shark.utilityconsole.util.searching.ParameterCriterion.CriteriaParameter.Criteria.*;
import static shark.utilityconsole.util.searching.ParameterCriterion.CriteriaParameter.WeaponParameter.*;

public class FindWeapons implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        /**
         * findweapons energy                   // lists all energy weapons
         * findweapons { large missile, range > 1000 }  // lists all large missile weapons with range > 1000
         */

        List<ParameterCriterion> criteriumList = new ArrayList<>();

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
            if (args.endsWith("}")) {
                // start extracting
                // Expected syntax 'findships { <expr>,<expr>,<expr> }`
                // 1. get rid of { and } and trim the string to get rid of leading/trailing spaces between expressions and { or }
                // 2. split the string on comma (,) to get individual expressions
                // 3. convert each expression into a ParameterCriterion and add them to the 'criteriumList'
                args = args.replace("{", "");
                args = args.replace("}", "");
                args = args.trim();
                String[] expressions = args.split(",");
                Console.showMessage("Received input: " + args + ", extracted expression list: " + Arrays.asList(expressions));
                for (String expression : expressions) {
                    ExpressionProcessingResult result = processSingleExpressionIntoParameterCriterion(expression);
                    if (result.isSuccess()) {
                        criteriumList.add(result.getResult());
                    } else {
                        return result.getCommandResult();
                    }
                }
                // That's it, by this point we'd have compiled a list of processed criteriums for searching or had returned an error
            } else {
                return CommandResult.BAD_SYNTAX;
            }
        } else {
            // only one parameter (but would still be a list of keywords)
            ExpressionProcessingResult result = processSingleExpressionIntoParameterCriterion(args);
            if (result.isSuccess()) {
                criteriumList.add(result.getResult());
            } else {
                return result.getCommandResult();
            }
        }
        final SearchCriteria searchCriteria = new SearchCriteria(criteriumList);

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

                for (ShipHullSpecAPI ship : foundShips) {
                    stringifyShipIntoStringBuilder(sb, ship);
                }
                Console.showMessage(sb.toString());
            }
        });

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

    private WeaponAPI.WeaponType remapWeaponSymbol(String symbol) {
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

    private boolean isWeaponParameterSymbol(String symbol) {
        /**
         * range, ammo, type, damagetype, turnrate, size, rarity, beam, mounttype,
         */
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("AMMO")
                || symbol.equalsIgnoreCase("RANGE")
                || symbol.equalsIgnoreCase("CREW")
                || symbol.equalsIgnoreCase("rarity")
                || symbol.equalsIgnoreCase("mounttype") || symbol.equalsIgnoreCase("mount-type") || symbol.equalsIgnoreCase("mount_type")
                || symbol.equalsIgnoreCase("damagetype") || symbol.equalsIgnoreCase("damage-type") || symbol.equalsIgnoreCase("damage_type")
                || symbol.equalsIgnoreCase("turnrate") || symbol.equalsIgnoreCase("turn-rate") || symbol.equalsIgnoreCase("turn_rate")
                || symbol.equalsIgnoreCase("SIZE");

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

    private boolean isValidSymbol(String symbol) {
        return isWeaponSymbol(symbol) || isSizeSymbol(symbol) || isShipParameterSymbol(symbol);
    }

    private ParameterCriterion.CriteriaParameter.WeaponParameter remapWeaponParameterSymbol(String symbol) {
        /**
         * RANGE, AMMO, TYPE, DAMAGE_TYPE, TURN_RATE, SIZE, RARITY, BEAM, MOUNT_TYPE
         */
        /**
         * CARGO, FUEL, CREW, HITPOINTS, ARMOR, FLUX_CAPACITY, FLUX_DISSIPATION, SIZE
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
            retVal = SIZE;
        } else if (symbol.equalsIgnoreCase("RARITY")) {
            retVal = RARITY;
        } else if (symbol.equalsIgnoreCase("BEAM")) {
            retVal = BEAM;
        } else {
            Console.showMessage("Invalid symbol to remap to WeaponParameter! Received " + symbol);
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
                                                          ParameterCriterion.CriteriaParameter.WeaponParameter weaponParameter,
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
            case WEAPON_PARAMETER:
                data = new ParameterCriterion.CriteriaParameter.WeaponParameterCriteriaData(weaponParameter);
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
                .append("Hull name: ").append(String.format("%32s", ship.getHullName()))
                .append("\t\t\tHull ID: ").append(String.format("%64s", ship.getHullId()))
                .append("\n");
    }

    public ExpressionProcessingResult processSingleExpressionIntoParameterCriterion(String expression) {
        // Start by trimming the expression just in case spaces were involved.
        expression = expression.trim();
        ParameterCriterion.CriteriaParameter.Criteria criteria = null;

        WeaponAPI.WeaponSize weaponSize = null;
        WeaponAPI.WeaponType weaponType = null;

        ParameterCriterion.CriteriaParameter.WeaponParameter weaponParameter = null;

        ParameterCriterion.CriteriaQuantity criteriaQuantity = null;
        ParameterCriterion.CriteriaQuantity.Quantity quantityType = null;
        int quantity = 0;

        // only one parameter (but would still be a list of keywords)
        String[] symbols = expression.split(" ");
        // Lets turn this into a list, since we can remove from lists
        ArrayList<String> symbolList = new ArrayList<String>(Arrays.asList(symbols));
        // sanity-check - is it even a valid symbol? e.g. flightbay is an invalid symbol that causes NPEs somehow.
        if (!isValidSymbol(symbolList.get(0).trim())) {
            Console.showMessage("Invalid token " + symbolList.get(0));
            return new FailedExpressionProcessingResult(CommandResult.BAD_SYNTAX);
        }

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
            // 1. we are going to be dealing with a WEAPON query, only if we didn't set it to WEAPON_WITH_SIZE before
            // 2. the actual weapon
            if (criteria == null) {
                criteria = WEAPON;
            }
            weaponType = remapWeaponSymbol(symbolList.get(0).trim());

            // remove this element so the rest of the code can be much more straightforward
            symbolList.remove(0);
        } else if (isWeaponParameterSymbol(symbolList.get(0).trim())) {
            // Since it's a weapon parameter, we can determine two things:
            // 1. we're going to be dealing with a WEAPON_PARAMETER query
            // 2. the actual parameter
            criteria = WEAPON_PARAMETER;
            weaponParameter = remapWeaponParameterSymbol(symbolList.get(0).trim());

            // remove this element so the rest of the code can be much more straightforward
            symbolList.remove(0);
        } else {
            //TODO fix this error message
            Console.showMessage("Invalid expression format. Parsable expression format is: [<size>] [<weapon> or <ship parameter>] [<op> <quantity>]\nTry 'findships <size> <weapon> or <ship parameter>' or 'findships { <expression>,<expression>,...,<expression> }'");
            return new FailedExpressionProcessingResult(CommandResult.BAD_SYNTAX);
        }

        // Now, presuming everything was alright, we have three possible scenarios:
        // 1. the syntax was bad, so just return BAD_SYNTAX result
        // 2. the query is over, so we're dealing with a query that has an implicit quantity of AT_LEAST 0
        // 3. the query is not over, so we should parse the CriteriaQuantity
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
                        return new FailedExpressionProcessingResult(CommandResult.BAD_SYNTAX);
                    }
                }

                criteriaQuantity = new ParameterCriterion.CriteriaQuantity(
                        quantityType,
                        quantity
                );
            } else {
                Console.showMessage("Wrong input for quantity check! Expected an operation (< or = or >) followed by a number, received " + Arrays.deepToString(symbolList.toArray()));
                return new FailedExpressionProcessingResult(CommandResult.BAD_SYNTAX);
            }
        }

        // Only if it wasn't set to failure are we allowed to do this here safely
        return new SuccessfulExpressionProcessingResult(
                constructParameterCriteria(criteria, weaponSize, weaponType, shipParameter, criteriaQuantity)
        );
    }
}
