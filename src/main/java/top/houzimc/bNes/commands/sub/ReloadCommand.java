package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import top.houzimc.bNes.BNes;

public class ReloadCommand {
    public static CommandAPICommand create(BNes plugin){
        return new CommandAPICommand("reload")
                .executesPlayer((p, args) -> {
                    plugin.setting.reload();
                    p.sendMessage("重载已完成");
                });
    }
}
