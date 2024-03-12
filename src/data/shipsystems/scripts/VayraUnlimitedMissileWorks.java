package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

/**
 * Why did I think this was a worthwhile use of my time?
 * What is wrong with me?
 * @author Histidine
 */
public class VayraUnlimitedMissileWorks extends BaseShipSystemScript {
	
	public static final String[] LINES = new String[] {
		"I am the bone of my sword",
		"Steel is my body, and fire is my blood",
		"I have created over a thousand blades",
		"Unknown to death, nor known to life",
		"Have withstood pain to create many weapons",
		"Yet, those hands will never hold anything",
		"So as I pray, Unlimited Missile Works"
	};
	
	public int numUses = -1;
	
	public String getString() {
		if (numUses >= LINES.length)
			numUses = 0;
		try {
			return LINES[numUses];
		} catch (Exception ex) {
			return "";
		}
	}
	
	@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		numUses++;
	}
	
	@Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData(getString(), false);
		}
		return null;
	}
}


