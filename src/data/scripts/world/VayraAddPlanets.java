package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;

import java.util.List;

public class VayraAddPlanets {

    public static Logger log = Global.getLogger(VayraAddPlanets.class);

    public void generate(SectorAPI sector) {
        List<StarSystemAPI> systems = sector.getStarSystems();
        for (StarSystemAPI system : systems) {
            int planets = system.getPlanets().size();
            float dist = 300f;
            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.isStar()) {
                    planets--;
                }
                float rad2x = planet.getCircularOrbitRadius() + (planet.getRadius() * 2);
                if (rad2x > dist) {
                    dist = rad2x;
                }
            }
            PlanetAPI star = system.getStar();
            log.info("counted " + planets + " planets for system " + system.getNameWithTypeIfNebula());
            if (planets < 1 && star != null) {
                String name = ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, star.getName(), null).nameWithRomanSuffixIfAny;
                WeightedRandomPicker<String> types = new WeightedRandomPicker<>();
                types.add("barren");
                types.add("barren2");
                types.add("barren3");
                types.add("barren-bombarded");
                types.add("barren-desert");
                types.add("irradiated");
                String type = types.pick();
                //(String id, SectorEntityToken focus, String name, String type, float angle, float radius, float orbitRadius, float orbitDays)
                PlanetAPI planet = system.addPlanet(system.getId() + "_" + name, system.getCenter(), name, type,
                        (float) (Math.random() * 360f), 50f + ((float) Math.random() * 100f), dist, 360f);
                Misc.initConditionMarket(planet);
                planet.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                planet.getMarket().addCondition(Conditions.IRRADIATED);
                planet.getMarket().addCondition(Conditions.METEOR_IMPACTS);
                if (Math.random() < 0.69f) planet.getMarket().addCondition(Conditions.ORE_SPARSE);
                if (Math.random() < 0.1312f) planet.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
                log.info("added planet " + planet + " to system " + system.getNameWithTypeIfNebula());
            }
        }
    }
}
