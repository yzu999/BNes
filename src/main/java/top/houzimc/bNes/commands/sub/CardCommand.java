package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;
import top.houzimc.bNes.BNes;

import java.io.File;

public class CardCommand {
    public static CommandAPICommand create(BNes nes){
        return new CommandAPICommand("card")
                .withArguments(new TextArgument("name").replaceSuggestions(ArgumentSuggestions.strings(
                        nes.getCardFactory().getAllRomList()
                )))
                .executesPlayer((p, args) -> {
                    return CardCommand.execute(p, args, nes);
                });
    }
    public static int execute(Player player, CommandArguments arguments,BNes bnes){
        String name = arguments.getUnchecked("name");
        if (name == null || !new File(bnes.romDir,name).exists()){
            player.sendMessage(" §c文件不存在" + name);
            return 1;
        }else {
            player.getInventory().addItem(bnes.getCardFactory().makeCard(name));
            player.sendMessage("获取卡带: " + name);
        }
        return 0;
    }
}
