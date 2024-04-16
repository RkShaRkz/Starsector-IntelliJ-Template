package shark.utilityconsole.util.searching;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.lazywizard.console.Console;

public class ParameterCriterion {
    private final CriteriaParameter criteriaParameter;
    private final CriteriaQuantity criteriaQuantity;

    public ParameterCriterion(CriteriaParameter parameter, CriteriaQuantity quantity) {
        this.criteriaParameter = parameter;
        this.criteriaQuantity = quantity;
    }

    public int compareResults(ShipHullSpecAPI ship1, ShipHullSpecAPI ship2) {
        // The idea is, depending on the criteria used, to sort from highest to lowest.
        // We can establish ordering based on how many matches each of these results have.
        return Integer.compare(countMatches(ship1), countMatches(ship2));
    }

    public int compareResults(WeaponSpecAPI weapon1, WeaponSpecAPI weapon2) {
        // The idea is, depending on the criteria used, to sort from highest to lowest.
        // We can establish ordering based on how many matches each of these results have.
        return Integer.compare(countMatches(weapon1), countMatches(weapon2));
    }

    public static class CriteriaParameter {
        public enum Criteria {
            /**
             * Actually maps to {@link com.fs.starfarer.api.combat.WeaponAPI.WeaponType}
             */
            WEAPON,

            /**
             * Actually maps to both {@link com.fs.starfarer.api.combat.WeaponAPI.WeaponType} and {@link com.fs.starfarer.api.combat.WeaponAPI.WeaponSize}
             */
            WEAPON_WITH_SIZE,
            SHIP_PARAMETER,
            WEAPON_PARAMETER
        }

        public enum ShipParameter {
            CARGO, FUEL, CREW, HITPOINTS, ARMOR, FLUX_CAPACITY, FLUX_DISSIPATION, SIZE
        }

        public enum WeaponParameter {
            //  range, ammo, type, damagetype, turnrate, size, rarity, beam, mounttype,
            RANGE, AMMO, TYPE, DAMAGE_TYPE, TURN_RATE, SIZE, RARITY, BEAM, MOUNT_TYPE
        }

        private final Criteria criteria;
        private final CriteriaData criteriaData;


        public static abstract class CriteriaData {
        }

        public static class WeaponCriteriaData extends CriteriaData {
            private final WeaponAPI.WeaponType type;

            public WeaponCriteriaData(WeaponAPI.WeaponType type) {
                this.type = type;
            }

            public WeaponAPI.WeaponType getType() {
                return type;
            }
        }

        public static class WeaponAndSizeCriteriaData extends CriteriaData {
            private final WeaponAPI.WeaponType type;
            private final WeaponAPI.WeaponSize size;

            public WeaponAndSizeCriteriaData(WeaponAPI.WeaponType type, WeaponAPI.WeaponSize size) {
                this.type = type;
                this.size = size;
            }

            public WeaponAPI.WeaponType getType() {
                return type;
            }

            public WeaponAPI.WeaponSize getSize() {
                return size;
            }
        }

        public static class ShipParameterCriteriaData extends CriteriaData {
            private final ShipParameter parameter;

            public ShipParameterCriteriaData(ShipParameter parameter) {
                this.parameter = parameter;
            }

            public ShipParameter getParameter() {
                return parameter;
            }
        }

        public static class WeaponParameterCriteriaData extends CriteriaData {
            private final WeaponParameter parameter;

            public WeaponParameterCriteriaData(WeaponParameter parameter) {
                this.parameter = parameter;
            }

            public WeaponParameter getParameter() {
                return parameter;
            }
        }

        public CriteriaParameter(Criteria criteria, CriteriaData data) {
            this.criteria = criteria;
            this.criteriaData = data;

            // These can't be final and initialized at the same time;
            // because we assign only some of them in different cases
            // the variables will either report a "cannot set value to final variable" error
            // or the "Variable might not have been initialized" error.
            //
            // So, lets get rid of "final" and hope I don't shoot myself in the foot somewhere.
            switch (criteria) {
                case WEAPON:
                    // The 'data' will surely be WeaponCriteriaData, so cast it first
                    if (!(data instanceof WeaponCriteriaData)) {
                        Console.showMessage("Invalid CriteriaData passed for WEAPON CriteriaParameter");
                    }
                    break;
                case WEAPON_WITH_SIZE:
                    // The 'data' will surely be WeaponAndSizeCriteriaData, so cast it first
                    if (!(data instanceof WeaponAndSizeCriteriaData)) {
                        Console.showMessage("Invalid CriteriaData passed for WEAPON_WITH_SIZE CriteriaParameter");
                    }
                    break;
                case SHIP_PARAMETER:
                    // The 'data' will surely be ShipParameterCriteriaData, so cast it first
                    if (!(data instanceof ShipParameterCriteriaData)) {
                        Console.showMessage("Invalid CriteriaData passed for SHIP_PARAMETER CriteriaParameter");
                    }
                    break;
                case WEAPON_PARAMETER:
                    // The 'data' will surely be WeaponParameterCriteriaData, so cast it first
                    if (!(data instanceof WeaponParameterCriteriaData)) {
                        Console.showMessage("Invalid CriteriaData passed for WEAPON_PARAMETER CriteriaParameter");
                    }
                    break;

                default: // we don't want to throw
                    Console.showMessage("Unsupported criteria used! Received " + criteria);
            }
        }
    }

    public static class CriteriaQuantity {
        public enum Quantity {
            AT_LEAST, EXACTLY, AT_MOST
        }

        final Quantity criteria;
        final int quantity;

        public CriteriaQuantity(Quantity criteria, int quantity) {
            this.criteria = criteria;
            this.quantity = quantity;
        }
    }

    public boolean matches(ShipHullSpecAPI ship) {
        boolean retVal = false;

        switch (this.criteriaParameter.criteria) {
            case WEAPON: {
                CriteriaParameter.WeaponCriteriaData actualData = (CriteriaParameter.WeaponCriteriaData) criteriaParameter.criteriaData;
                int matches = 0;
                // Iterate through all of ship's weapons and make sure they match the criteria
                for (WeaponSlotAPI weaponSlot : ship.getAllWeaponSlotsCopy()) {
                    if (weaponSlot.getWeaponType() == actualData.getType()) {
                        matches++;
                    }
                }

                // Now, make sure they match the necessary quantity as well
                retVal = matchesCriteriaQuantity(matches);
            }
            break;

            case WEAPON_WITH_SIZE: {
                CriteriaParameter.WeaponAndSizeCriteriaData actualData = (CriteriaParameter.WeaponAndSizeCriteriaData) criteriaParameter.criteriaData;
                int matches = 0;
                // Iterate through all of ship's weapons and make sure they match the criteria
                for (WeaponSlotAPI weaponSlot : ship.getAllWeaponSlotsCopy()) {
                    if ((weaponSlot.getWeaponType() == actualData.getType()) && (weaponSlot.getSlotSize() == actualData.getSize())) {
                        matches++;
                    }
                }

                // Now, make sure they match the necessary quantity as well
                retVal = matchesCriteriaQuantity(matches);
            }
            break;

            case SHIP_PARAMETER: {
                CriteriaParameter.ShipParameterCriteriaData actualData = (CriteriaParameter.ShipParameterCriteriaData) criteriaParameter.criteriaData;
                // Somehow I think it's better to use rounding rather than direct casting, but I dont know if there's a real difference
                switch (actualData.getParameter()) {
                    case CARGO:
                        retVal = matchesCriteriaQuantity(Math.round(ship.getCargo()));
                        break;
                    case FUEL:
                        retVal = matchesCriteriaQuantity(Math.round(ship.getFuel()));
                        break;
                    case CREW:
                        retVal = matchesCriteriaQuantity(Math.round(ship.getMaxCrew()));
                        break;
                    case HITPOINTS:
                        retVal = matchesCriteriaQuantity(Math.round(ship.getHitpoints()));
                        break;
                    case ARMOR:
                        retVal = matchesCriteriaQuantity(Math.round(ship.getArmorRating()));
                        break;
                    case FLUX_CAPACITY:
                        retVal = matchesCriteriaQuantity(Math.round(ship.getFluxCapacity()));
                        break;
                    case FLUX_DISSIPATION:
                        retVal = matchesCriteriaQuantity(Math.round(ship.getFluxDissipation()));
                        break;
                    case SIZE:
                        // Kind of a hack, but earlier we replaced the "frigate" with 2 so it'll work out. I think.
                        retVal = matchesCriteriaQuantity(ship.getHullSize().ordinal());
                        break;
                }
            }
            break;

            case WEAPON_PARAMETER: {
                // We can't have these for ships
            }
            break;
        }

        return retVal;
    }

    public boolean matches(WeaponSpecAPI weapon) {
        boolean retVal = false;

        switch (this.criteriaParameter.criteria) {
            case WEAPON: {
                CriteriaParameter.WeaponCriteriaData actualData = (CriteriaParameter.WeaponCriteriaData) criteriaParameter.criteriaData;
                int matches = 0;

                if (weapon.getType() == actualData.getType()) {
                    matches++;
                }

                // Now, make sure they match the necessary quantity as well
                retVal = matchesCriteriaQuantity(matches);
            }
            break;

            case WEAPON_WITH_SIZE: {
                CriteriaParameter.WeaponAndSizeCriteriaData actualData = (CriteriaParameter.WeaponAndSizeCriteriaData) criteriaParameter.criteriaData;
                int matches = 0;

                if ((weapon.getType() == actualData.getType()) && (weapon.getSize() == actualData.getSize())) {
                    matches++;
                }

                // Now, make sure they match the necessary quantity as well
                retVal = matchesCriteriaQuantity(matches);
            }
            break;

            case SHIP_PARAMETER:
                // We can't have these for weapons
                break;

            case WEAPON_PARAMETER: {
                CriteriaParameter.WeaponParameterCriteriaData actualData = (CriteriaParameter.WeaponParameterCriteriaData) criteriaParameter.criteriaData;
                switch (actualData.getParameter()) {
                    case RANGE:
                        retVal = matchesCriteriaQuantity(Math.round(weapon.getMaxRange()));
                        break;
                    case AMMO:
                        retVal = matchesCriteriaQuantity(weapon.getMaxAmmo());
                        break;
                    case TYPE:
                        retVal = matchesCriteriaQuantity(weapon.getType().ordinal());
                        break;
                    case DAMAGE_TYPE:
                        retVal = matchesCriteriaQuantity(weapon.getDamageType().ordinal());
                        break;
                    case TURN_RATE:
                        retVal = matchesCriteriaQuantity(Math.round(weapon.getTurnRate()));
                        break;
                    case SIZE:
                        retVal = matchesCriteriaQuantity(weapon.getSize().ordinal());
                        break;
                    case RARITY:
                        retVal = matchesCriteriaQuantity(Math.round(weapon.getRarity())); //TODO bug smell
                        break;
                    case BEAM:
                        retVal = matchesCriteriaQuantity(weapon.isBeam() ? 1 : 0);
                        break;
                    case MOUNT_TYPE:
                        retVal = matchesCriteriaQuantity(weapon.getMountType().ordinal());
                        break;
                }
            }
            break;
        }

        return retVal;
    }

    public int countMatches(ShipHullSpecAPI ship) {
        int retVal = 0;
        switch (this.criteriaParameter.criteria) {
            case WEAPON: {
                CriteriaParameter.WeaponCriteriaData actualData = (CriteriaParameter.WeaponCriteriaData) criteriaParameter.criteriaData;
                // Iterate through all of ship's weapons and make sure they match the criteria
                for (WeaponSlotAPI weaponSlot : ship.getAllWeaponSlotsCopy()) {
                    if (weaponSlot.getWeaponType() == actualData.getType()) {
                        retVal++;
                    }
                }

                break;
            }
            case WEAPON_WITH_SIZE: {
                CriteriaParameter.WeaponAndSizeCriteriaData actualData = (CriteriaParameter.WeaponAndSizeCriteriaData) criteriaParameter.criteriaData;
                // Iterate through all of ship's weapons and make sure they match the criteria
                for (WeaponSlotAPI weaponSlot : ship.getAllWeaponSlotsCopy()) {
                    if ((weaponSlot.getWeaponType() == actualData.getType()) && (weaponSlot.getSlotSize() == actualData.getSize())) {
                        retVal++;
                    }
                }

                break;
            }
            //TODO check and see how fighter bays behave, and maybe move them to be a parameter rather than a weapon
            case SHIP_PARAMETER: {
                CriteriaParameter.ShipParameterCriteriaData actualData = (CriteriaParameter.ShipParameterCriteriaData) criteriaParameter.criteriaData;
                // Somehow I think it's better to use rounding rather than direct casting, but I dont know if there's a real difference
                switch (actualData.getParameter()) {
                    case CARGO:
                        retVal = Math.round(ship.getCargo());
                        break;
                    case FUEL:
                        retVal = Math.round(ship.getFuel());
                        break;
                    case CREW:
                        retVal = Math.round(ship.getMaxCrew());
                        break;
                    case HITPOINTS:
                        retVal = Math.round(ship.getHitpoints());
                        break;
                    case ARMOR:
                        retVal = Math.round(ship.getArmorRating());
                        break;
                    case FLUX_CAPACITY:
                        retVal = Math.round(ship.getFluxCapacity());
                        break;
                    case FLUX_DISSIPATION:
                        retVal = Math.round(ship.getFluxDissipation());
                        break;
                    case SIZE:
                        // Ugh, not great, but lets also establish sorting order from biggest to smallest
                        retVal = ship.getHullSize().ordinal();
                        break;
                }


                break;
            }
        }

        return retVal;
    }

    public int countMatches(WeaponSpecAPI weapon) {
        int retVal = 0;
        switch (this.criteriaParameter.criteria) {
            case WEAPON: {
                CriteriaParameter.WeaponCriteriaData actualData = (CriteriaParameter.WeaponCriteriaData) criteriaParameter.criteriaData;
                // Iterate through all of ship's weapons and make sure they match the criteria
                if (weapon.getType() == actualData.getType()) {
                    retVal++;
                }

                break;
            }
            case WEAPON_WITH_SIZE: {
                CriteriaParameter.WeaponAndSizeCriteriaData actualData = (CriteriaParameter.WeaponAndSizeCriteriaData) criteriaParameter.criteriaData;
                // Iterate through all of ship's weapons and make sure they match the criteria
                if (weapon.getType() == actualData.getType() && weapon.getSize() == actualData.getSize()) {
                    retVal++;
                }

                break;
            }
            case SHIP_PARAMETER:
                // We simply can't have any of these for weapons
                // Ex: we can't look for a weapon that has X ballistic slots and Y large missile slots
                // Ex: we can't look for weapons that satisfy a 'cargo > 500' condition
                break;

            case WEAPON_PARAMETER: {
                CriteriaParameter.WeaponParameterCriteriaData actualData = (CriteriaParameter.WeaponParameterCriteriaData) criteriaParameter.criteriaData;
                switch (actualData.getParameter()) {
                    case RANGE:
                        retVal = Math.round(weapon.getMaxRange());
                        break;
                    case AMMO:
                        retVal = weapon.getMaxAmmo();
                        break;
                    case TYPE:
                        retVal = weapon.getType().ordinal(); //TODO what to do here?
                        break;
                    case DAMAGE_TYPE:
                        retVal = weapon.getDamageType().ordinal(); //TODO same thing
                        break;
                    case TURN_RATE:
                        retVal = Math.round(weapon.getTurnRate());
                        break;
                    case SIZE:
                        retVal = weapon.getSize().ordinal(); //TODO similar to hullsize
                        break;
                    case RARITY:
                        retVal = Math.round(weapon.getRarity()); //FIXME i think this is a bug
                        break;
                    case BEAM:
                        retVal = weapon.isBeam() ? 1 : 0;
                        break;
                    case MOUNT_TYPE:
                        retVal = weapon.getMountType().ordinal(); //TODO again
                        break;
                }
            }
        }

        return retVal;
    }

    private boolean matchesCriteriaQuantity(int value) {
        boolean retVal = false;
        switch (this.criteriaQuantity.criteria) {
            case AT_LEAST:
                retVal = value >= this.criteriaQuantity.quantity;
                break;
            case EXACTLY:
                retVal = value == this.criteriaQuantity.quantity;
                break;
            case AT_MOST:
                retVal = value <= this.criteriaQuantity.quantity;
                break;
        }

        return retVal;
    }
}