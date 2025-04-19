package forge;

import forge.game.Game;
import forge.game.player.IGameEntitiesFactory;
import forge.game.player.Player;
import forge.game.player.PlayerController;

public class ApiLobbyPlayer extends LobbyPlayer implements IGameEntitiesFactory {

    private final String name;

    public ApiLobbyPlayer(String name) {
        super(name); // or call any parent constructor if required
        this.name = name;
    }

    @Override
    public void hear(LobbyPlayer player, String message) {
        // TODO
    }

    @Override
    public PlayerController createMindSlaveController(Player master, Player slave) {
        return null;
    }

    @Override
    public Player createIngamePlayer(Game game, int id) {
        return new Player(name, game, id);
    }
}
