package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

public class VayraShipPackModPlugin extends BaseModPlugin {

    public static Logger log = Global.getLogger(VayraShipPackModPlugin.class);

    public static final String MOD_ID = "vayrashippack";

    @Override
    public void onApplicationLoad() throws Exception {
        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.lazywizard.lazylib.ModUtils");
        } catch (ClassNotFoundException lazy) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "LazyLib is required to run Vayra's Ship Pack."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download LazyLib at http://fractalsoftworks.com/forum/index.php?topic=5444"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }
        //VayraTags.checkSpecial();
    }
    
     @Override
    public void onNewGameAfterTimePass() {

        log.info("new game started, adding scripts");
        Global.getSector().addScript(new VayraInventoryStripper());
    }

    public static float randomRange(float min, float max) {
        return (float) (random() * (max - min) + min);
    }

    public static String[] JSONArrayToStringArray(JSONArray jsonArray) {
        try {
            String[] ret = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                ret[i] = jsonArray.getString(i);
            }
            return ret;
        } catch (JSONException e) {
            log.warn(e);
            return new String[]{};
        }
    }

    public static String aOrAn(String input) {

        ArrayList<String> vowels = new ArrayList<>(Arrays.asList(
                "a",
                "e",
                "i",
                "o",
                "u"));

        String firstLetter = input.substring(0, 1).toLowerCase();

        if (vowels.contains(firstLetter)) {
            return "an";
        } else {
            return "a";
        }
    }

    public static List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }
}
