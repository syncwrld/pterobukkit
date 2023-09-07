package me.syncwrld.pterobukkit;

import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import lombok.AccessLevel;
import lombok.Getter;
import me.syncwrld.pterobukkit.command.LookServersCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

@Getter(AccessLevel.PUBLIC)
public final class PteroBukkitBootstrap extends JavaPlugin {

    private PteroApplication pteroApplication;
    private PteroClient pteroClient;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            this.pteroApplication = PteroBuilder.createApplication(
                    this.getConfig().getString("panel-url"),
                    this.getConfig().getString("application-token")
            );

            this.pteroClient = PteroBuilder.createClient(
                    this.getConfig().getString("panel-url"),
                    this.getConfig().getString("client-token")
            );
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§b[PteroBukkit] §cCan't login with current defined credentials.");
            this.getServer().getPluginManager().disablePlugin(this);
        }

        this.getCommand("servers").setExecutor(new LookServersCommand(this));
        Bukkit.getConsoleSender().sendMessage("§b[PteroBukkit] §aFinally complete loading phase.");
    }

}
