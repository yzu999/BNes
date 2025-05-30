package top.houzimc.bNes;

import io.netty.util.collection.IntObjectHashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import top.houzimc.bNes.commands.BNesCommand;
import top.houzimc.bNes.listener.PlayerListener;
import top.houzimc.bNes.nms.PlayerNms;
import top.houzimc.bNes.render.BukkitRender;
import top.houzimc.bNes.render.BukkitRender1x;
import top.houzimc.bNes.utils.CardFactory;
import top.houzimc.bNes.utils.CommonUtils;
import top.houzimc.bNes.voicechat.VoiceChatPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class BNes extends PluginBase {
    public static BNes plugin;
    public final File NON_CARD;
    public File romDir;
    public Setting setting;
    PlayerNms playerNms;
    Map<String, BukkitRender> nameRenderMap = new HashMap<>();
    IntObjectHashMap<BukkitRender> idRenderMap = new IntObjectHashMap<>();
    CardFactory cardFactory;
    private Economy economy;
    private VoiceChatPlugin voiceChatPlugin;

    public BNes() {
        NON_CARD = new File(getDataFolder(), "non_card.nes");
    }

    public static BNes getPlugin() {
        return plugin;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onLoad() {
        saveDefaultConfig();
        plugin = this;
        setting = new Setting(this);
        romDir = new File(getDataFolder(), "roms");
        if (!romDir.exists()) romDir.mkdir();
        if (!NON_CARD.exists()) {
            try {
                NON_CARD.createNewFile();
                try (var input = getResource("non_card.nes"); var outPut = new FileOutputStream(NON_CARD)) {
                    if (input == null) return;
                    outPut.write(input.readAllBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playerNms = PlayerNms.getInstance();
    }

    @Override
    public void onEnable() {
        cardFactory = new CardFactory(romDir, setting.cardItem);
        regListener(new PlayerListener(this));
        loadRenders();
        final Logger logger = getLogger();
        logger.info("Current Nms: " + playerNms.getClass().getSimpleName());
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            Plugin p;
            p = Bukkit.getPluginManager().getPlugin("Vault");
            if (p != null) {
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
                if (economyProvider != null) {
                    this.economy = economyProvider.getProvider();
                    logger.info("hooked: " + p.getDescription().getFullName());
                }
            }
            p = Bukkit.getPluginManager().getPlugin("voicechat");
            if (p != null) {
                voiceChatPlugin = new VoiceChatPlugin(this);
                logger.info("hooked: " + p.getDescription().getFullName());
//            regListener(new TestListener());
            }
        }, 15);
        BNesCommand.register(plugin);
    }

    @Override
    public void onDisable() {
        unregListener();
        saveRenders();
        for (BukkitRender value : nameRenderMap.values()) {
            try {
                value.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        nameRenderMap.clear();
        idRenderMap.clear();
    }

    public void loadRenders() {
        ConfigurationSection renderStore = getRenderStore();
        var keys = renderStore.getKeys(false);
        for (String key : keys) {
            try {
                var r_config = renderStore.getConfigurationSection(key);
                //noinspection ConstantConditions
                BukkitRender render = r_config.getList("ids", Collections.emptyList()).size() > 1 ? new BukkitRender(key, this) : new BukkitRender1x(key, this);
                render.form(r_config);
                long time = System.currentTimeMillis() - render.lastRender();
                if (time > setting.letheTime) {
                    if (setting.letheClearup) {
                        plugin.getLogger().warning("§b实例 §f" + render.getName() + " §b已有§f " + CommonUtils.tanMintoh(time) + " §b未更新，已关闭实例");
                        render.close();
                        continue;
                    } else {
                        plugin.getLogger().warning("§b实例 §f" + render.getName() + " §b已有§f " + CommonUtils.tanMintoh(time) + " §b未更新,这可能是个被抛弃的游戏机，你可以使用§a/ns close§b来关闭这个实例");
                    }
                }
                render.initRender();
                putRender(render);
            } catch (Exception exception) {
                getLogger().warning("加载实例" + key + "异常");
                exception.printStackTrace();
            }
        }
    }

    public ConfigurationSection getRenderStore() {
        var renderStore = setting.getStorage().getConfigurationSection("renders");
        if (renderStore == null) {
            renderStore = setting.getStorage().createSection("renders");
        }
        return renderStore;
    }

    public void saveRenders() {
        ConfigurationSection renders = getRenderStore();
        for (Map.Entry<String, BukkitRender> entry : nameRenderMap.entrySet()) {
            try {
                entry.getValue().saveTo(renders.createSection(entry.getKey()));
            } catch (Exception exception) {
                getLogger().warning("储存" + entry.getKey() + "异常");
                exception.printStackTrace();
            }
        }
        setting.saveStorage();
    }

    public Map<String, BukkitRender> getRenderMap() {
        return nameRenderMap;
    }

    public void putRender(BukkitRender render) {
        nameRenderMap.put(render.getName(), render);
        for (Integer id : render.getIds()) {
            idRenderMap.put(id, render);
        }
    }

    public PlayerNms getPlayerNms() {
        return playerNms;
    }

    public Set<String> getRenders() {
        return nameRenderMap.keySet();
    }

    public BukkitRender getRender(String name) {
        return nameRenderMap.get(name);
    }

    public BukkitRender getRender(int id) {
        return idRenderMap.get(id);
    }

    public BukkitRender removeRender(String name) {
        BukkitRender render = nameRenderMap.remove(name);
        if (render != null) for (Integer id : render.getIds()) {
            idRenderMap.remove(id);
        }
        return render;
    }

    public CardFactory getCardFactory() {
        return cardFactory;
    }

    public Economy getEconomy() {
        return economy;
    }

    public VoiceChatPlugin getVoiceChatPlugin() {
        return voiceChatPlugin;
    }
}