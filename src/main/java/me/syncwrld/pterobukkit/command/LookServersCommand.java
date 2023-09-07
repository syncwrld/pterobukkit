package me.syncwrld.pterobukkit.command;

import me.syncwrld.pterobukkit.PteroBukkitBootstrap;
import me.syncwrld.pterobukkit.view.AllServerView;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LookServersCommand implements CommandExecutor {

    private final PteroBukkitBootstrap bootstrap;

    public LookServersCommand(PteroBukkitBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        AllServerView allServerView = new AllServerView(
                player,
                bootstrap.getPteroApplication(),
                bootstrap.getPteroClient()
        );
        allServerView.renderIt();

        return false;
    }

}
