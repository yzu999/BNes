package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;
import top.houzimc.bNes.BNes;
import top.houzimc.bNes.render.BukkitRender;

import java.io.File;

public class CloseCommand {
    public static CommandAPICommand create(BNes bNes){
        return new CommandAPICommand("close")
                .withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.strings(
                        bNes.getRenders()
                )))
                .executesPlayer((p, args) -> {
                    return CloseCommand.execute(p, args, bNes);
                });
    }
    public static int execute(Player player, CommandArguments arguments, BNes bNes){
        String name = arguments.getUnchecked("name");
        if (name != null){
            BukkitRender render = bNes.removeRender(name);
            if (render != null){
                render.close();
                player.sendMessage("已关闭游戏机: " + name);
                bNes.getRenderStore().set(name,null);
            } else {
                player.sendMessage("找不到游戏机: " + name);
                return 1;
            }
        }
        return 0;
    }
}
