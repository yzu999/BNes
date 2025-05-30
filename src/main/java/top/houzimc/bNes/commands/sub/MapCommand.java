package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Player;
import top.houzimc.bNes.BNes;

import java.util.Optional;

public class MapCommand {
    public static CommandAPICommand create(BNes bNes){
        return new CommandAPICommand("map")
                .withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.strings(
                        bNes.getRenders()
                )))
                .withOptionalArguments(new PlayerArgument("player"))
                .executesPlayer((p, args) -> {
                    String name = args.getUnchecked("name");
                    if (name == null) return 1;
                    var render = bNes.getRender(name);
                    if (render == null){
                        p.sendMessage(" §b找不到地图");
                        return 1;
                    }
                    Optional<Object> player = args.getOptionalUnchecked("player");
                    Player playerToGive;
                    playerToGive = player.map(o -> (Player) o).orElse(p);
                    playerToGive.getInventory().addItem(render.getMaps());
                    p.sendMessage(" §b已将地图给与§f" + playerToGive.getName());
                    return 0;
                });
    }
}