package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.houzimc.bNes.BNes;

import java.util.List;
import java.util.Optional;

public class MenuCommand {
    public static CommandAPICommand create(BNes bNes){
        return new CommandAPICommand("menu")
                .withOptionalArguments(new IntegerArgument("page"))
                .executesPlayer((p, args) -> {
                    return MenuCommand.execute(p, args, bNes);
                });
    }
    public static int execute(Player player, CommandArguments arguments, BNes bNes){
        Optional<Object> page = arguments.getOptionalUnchecked("page");
        int pageToShow = 0;
        if (page.isPresent()){
            if((int)page.get() < 0){
                player.sendMessage(" §b无效页面");
                return 1;
            }else {
                pageToShow = (int)page.get();
            }
        }
        List<ItemStack> list = bNes.getCardFactory().getCardItems(pageToShow,54);
        if (list.isEmpty()){
            player.sendMessage(" §b空页面");
            return 1;
        }
        Inventory inv = Bukkit.createInventory(null, 54, "模型列表");
        inv.setContents(list.toArray(new ItemStack[0]));
        player.openInventory(inv);
        return 0;
    }
}
