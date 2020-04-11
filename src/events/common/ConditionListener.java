package events.common;

import game.GameException;
import game.GameInstance;

public interface ConditionListener {

    Event check(Event event, GameInstance game) throws Exception;
}
