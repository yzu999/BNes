package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;
import top.houzimc.bNes.BNes;
import top.houzimc.bNes.render.BukkitRender;

public class RenameCommand {
    public static CommandAPICommand create(BNes bNes){
        return new CommandAPICommand("rename")
                .withArguments(new StringArgument("old").replaceSuggestions(ArgumentSuggestions.strings(
                        bNes.getRenders()
                )))
                .withArguments(new StringArgument("new"))
                .executesPlayer((p, args) -> {
                    return RenameCommand.execute(p, args, bNes);
                });
    }
    public static int execute(Player player, CommandArguments arguments, BNes bNes){
        String oldName = arguments.getUnchecked("old");
        String newName = arguments.getUnchecked("new");
        if (oldName == null || newName == null) return 1;
        if (bNes.getRender(newName) != null){
            player.sendMessage(newName + "§b已存在");
            return 1;
        }
        BukkitRender render = bNes.removeRender(oldName);
        if (render == null){
            player.sendMessage("找不到地图");
            return 1;
        }
        render.setName(newName);
        bNes.putRender(render);
        var cs = bNes.getRenderStore();
        cs.set(oldName,null);
        try{
            render.saveTo(cs.createSection(newName));
        }catch (Exception exception){
            exception.printStackTrace();
        }
        player.sendMessage("重命名完成");
        return 0;
    }
}
