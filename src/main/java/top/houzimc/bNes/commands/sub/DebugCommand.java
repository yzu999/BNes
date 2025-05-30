package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;
import top.houzimc.bNes.BNes;

public class DebugCommand {
    public static CommandAPICommand create(BNes bNes){
        return new CommandAPICommand("debug")
                .executesPlayer((p, args) -> {
                    for (var render : bNes.getRenderMap().values()) {
                        for (Integer id : render.getIds()) {
                            bNes.getPlayerNms().sendMap(p, id, new byte[16384]);
                        }
                    }
                    p.sendMessage("已清空地图画面");
                    return 1;
                });
    }
}
