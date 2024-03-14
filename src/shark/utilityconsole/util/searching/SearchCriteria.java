package shark.utilityconsole.util.searching;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;

import java.util.List;

public class SearchCriteria {
    final List<ParameterCriterion> criteria;

    public SearchCriteria(List<ParameterCriterion> listOfCriteria) {
        this.criteria = listOfCriteria;
    }

    public List<ParameterCriterion> getCriteria() {
        return criteria;
    }

    /**
     * Tests whether a given {@link com.fs.starfarer.api.combat.ShipHullSpecAPI} matches all {@link ParameterCriterion}
     * this SearchCriteria includes
     * @return whether all criteria are met, as boolean
     */
    public boolean matches(ShipHullSpecAPI ship) {
        // Assume true until failed
        boolean retVal = true;
        for (ParameterCriterion pc : criteria) {
            retVal = retVal && pc.matches(ship);
        }
        return retVal;
    }

    public int compareResults(ShipHullSpecAPI ship1, ShipHullSpecAPI ship2) {
        // The idea is, depending on the criteria used, to sort from highest to lowest.
        // We can establish ordering based on how many matches each of these results have, one criteria at the time.
        int comparison = 0;
        for (ParameterCriterion param : criteria) {
            comparison += param.compareResults(ship1, ship2);
            if (comparison != 0) return comparison;
        }

        return comparison;
    }
}