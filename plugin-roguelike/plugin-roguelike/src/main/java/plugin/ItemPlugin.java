package plugin;

import game.GameContext;
import game.GameEventListener;

public interface ItemPlugin extends GameEventListener {

    String getItemId();

    ItemStats getStats();

    void apply(GameContext context);
}