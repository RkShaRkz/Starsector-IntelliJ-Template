package author.modname;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class TemplateModPlugin extends BaseModPlugin {
    
    public static final int BOUNTY_DURATION = 365;
    public static final int PLAYER_BOUNTY_DURATION = 36500;
    
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
    }
    
    @Override
    public void onNewGame() {
        super.onNewGame();
    }
    
    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);
    }
}