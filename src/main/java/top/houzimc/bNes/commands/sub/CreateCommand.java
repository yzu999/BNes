package top.houzimc.bNes.commands.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import top.houzimc.bNes.BNes;
import top.houzimc.bNes.render.BukkitRender;
import top.houzimc.bNes.render.BukkitRender1x;

import java.util.Optional;

public class CreateCommand {
    public static CommandAPICommand create(BNes bNes){
        return new CommandAPICommand("create")
                .withArguments(new StringArgument("name"))
                .withOptionalArguments(new BooleanArgument("one"))
                .executesPlayer((p, args) -> {
                    return CreateCommand.execute(p, args, bNes);
                });
    }
    public static int execute(Player player, CommandArguments arguments, BNes bNes){
        String name = arguments.getUnchecked("name");
        BukkitRender render = bNes.getRender(name);
        if (render != null){
            player.sendMessage(" §b实例已存在");
            return 1;
        }

        if (bNes.setting.createPrice > 0 && bNes.getEconomy() != null){
            var r = bNes.getEconomy().withdrawPlayer(player,bNes.setting.createPrice);
            if (r.type == EconomyResponse.ResponseType.SUCCESS){
                player.sendMessage(" §b消费§f" + r.amount + "§b创建游戏机");
            } else {
                player.sendMessage(" §b无法创建地图:§f" + r.errorMessage);
                return 1;
            }
        }
        Optional<Object> one = arguments.getOptionalUnchecked("one");
        if (one.isPresent() && (Boolean) one.get()) {
            render = new BukkitRender1x(name, bNes);
        }else {
            render = new BukkitRender(name, bNes);
        }
        bNes.putRender(render);
        render.open(bNes.NON_CARD.toString());
        render.start();
        player.sendMessage(" §b创建游戏机" + name);
        bNes.getPlayerNms().addInvOrDrop(player.getInventory(),render.getMaps());
        String errorMsg = render.getErrorMsg();
        if (errorMsg != null){
            player.sendMessage(" §c出现错误:§f" + errorMsg);
            return 1;
        }
        return 0;
    }
}
