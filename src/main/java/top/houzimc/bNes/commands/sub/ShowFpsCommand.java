package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;
import top.houzimc.bNes.BNes;

public class ShowFpsCommand {
    public static CommandAPICommand create(BNes bNes){
        return new CommandAPICommand("showfps")
                .executesPlayer((p, args) -> {
                    return ShowFpsCommand.execute(p, args, bNes);
                });
    }
    public static int execute(Player player, CommandArguments arguments, BNes bNes){
        return 0;
    }
}
