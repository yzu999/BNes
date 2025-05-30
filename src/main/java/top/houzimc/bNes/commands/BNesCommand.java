package top.houzimc.bNes.commands;

import dev.jorel.commandapi.CommandAPICommand;
import top.houzimc.bNes.BNes;
import top.houzimc.bNes.commands.sub.*;

public class BNesCommand {
    public static void register(BNes plugin){
        CommandAPICommand command = new CommandAPICommand("bnes")
                .withSubcommand(CardCommand.create(plugin))
                .withSubcommand(CloseCommand.create(plugin))
                .withSubcommand(CreateCommand.create(plugin))
                .withSubcommand(MapCommand.create(plugin))
                .withSubcommand(MenuCommand.create(plugin))
                .withSubcommand(ReloadCommand.create(plugin))
                .withSubcommand(RenameCommand.create(plugin))
                .withSubcommand(ShowFpsCommand.create(plugin));
        if (plugin.setting.DEBUG){
            command = command.withSubcommand(DebugCommand.create(plugin));
        }
        command.register();
    }
}
