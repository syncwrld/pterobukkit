package me.syncwrld.pterobukkit.view;

import com.google.common.base.Strings;
import com.mattmalec.pterodactyl4j.DataType;
import com.mattmalec.pterodactyl4j.ServerStatus;
import com.mattmalec.pterodactyl4j.application.entities.ApplicationServer;
import com.mattmalec.pterodactyl4j.application.entities.PteroApplication;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.syncwrld.pterobukkit.util.Heads;
import me.syncwrld.pterobukkit.util.ItemBuilderCustom;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

public class AllServerView {

    private final Player player;
    private final PaginatedGui menu;
    private final PteroApplication pteroApplication;
    private final PteroClient pteroClient;

    public AllServerView(Player player, PteroApplication pteroApplication, PteroClient pteroClient) {
        this.player = player;
        this.pteroApplication = pteroApplication;
        this.pteroClient = pteroClient;
        this.menu = Gui.paginated()
                .disableAllInteractions()
                .title(Component.text("§eViewing all servers"))
                .rows(5)
                .create();
    }

    public void renderIt() {
        menu.setItem(36, createNavigationItem("§7Go back", menu::previous));
        menu.setItem(44, createNavigationItem("§7Next page", menu::next));

        for (ApplicationServer server : this.pteroApplication.retrieveServers()) {
            ClientServer clientServer = this.pteroClient.retrieveServerByIdentifier(server.getIdentifier()).execute();
            if (clientServer.isSuspended() || clientServer.isTransferring() || clientServer.isInstalling()) {
                continue;
            }

            Utilization utilization = clientServer.retrieveUtilization().execute();

            String serverAddress = Strings.nullToEmpty(clientServer.getAllocations().get(0).getFullAddress());
            String serverName = Strings.nullToEmpty(server.getName());
            String serverImage = Strings.nullToEmpty(server.getContainer().getImage());
            String serverUptime = Strings.nullToEmpty(utilization.getUptimeFormatted());
            // String serverStatus = handleStatus(server);
            String memoryUsage = Strings.nullToEmpty(utilization.getMemoryFormatted(DataType.GB) + "/" + clientServer.getLimits().getMemory());
            String cpuUsage = Double.valueOf(utilization.getCPU()).intValue() + "/" + Double.valueOf(server.getLimits().getCPULong()).intValue() + "%";

            serverImage = sanitizeImageName(serverImage);

            GuiItem item = createServerGuiItem(serverName, memoryUsage, cpuUsage, serverImage, serverUptime, serverAddress, clientServer);
            menu.addItem(item);
        }

        menu.open(player);
    }

    private String sanitizeImageName(String serverImage) {
        return serverImage
                .replace("pterodactyl", "")
                .replace("ghcr.io", "")
                .replace("yolks", "")
                .replace("parkervcp", "")
                .replace("games", "")
                .replace(":", "")
                .replace("/", "");
    }

    private String handleStatus(ApplicationServer applicationServer) {
        ServerStatus serverStatus = (applicationServer.getStatus());
        switch (serverStatus) {
            case UNKNOWN:
                return "§7Unknown (?)";
            case SUSPENDED:
                return "§cSuspended";
            case INSTALLING:
                return "§7Installing egg...";
            case INSTALL_FAILED:
                return "§cFailed to install";
            case RESTORING_BACKUP:
                return "§eBackuping...";
            default:
                return "§4ERROR";
        }
    }

    private GuiItem createServerGuiItem(String serverName, String memoryUsage, String cpuUsage, String serverImage, String serverUptime, String serverAddress, ClientServer clientServer) {
        return ItemBuilder.from(new ItemBuilderCustom(Heads.buildSkull("http://textures.minecraft.net/texture/e82086d1545ae888aa766f8ed9c66e4755b42ed3a7be4e0cfa068d7f676d6df"))
                .setName("§dServer: " + serverName)
                .setLore(
                        "§8 ▶ §7" + serverAddress,
                        "§8 ▶ §7Memory: " + memoryUsage + " MB",
                        "§8 ▶ §7CPU: " + cpuUsage,
                        "§8 ▶ §7Image: " + serverImage,
                        "§8 ▶ §7Uptime: " + serverUptime,
                        "",
                        "§7Left Click: Power On",
                        "§7Right Click: Power Off",
                        "§7Press 'Q': Restart")
                .toItemStack()
        ).asGuiItem(event -> {
            if (event.isLeftClick()) {
                player.getOpenInventory().close();
                player.sendMessage("§bTrying to power on §7#" + serverName);
                clientServer.start().executeAsync();
            } else if (event.isRightClick()) {
                player.getOpenInventory().close();
                player.sendMessage("§eTrying to power off §7#" + serverName);
                clientServer.stop().executeAsync();
            } else if (event.getAction() == InventoryAction.DROP_ONE_SLOT) {
                player.getOpenInventory().close();
                player.sendMessage("§aTrying to restart §7#" + serverName);
                clientServer.restart().executeAsync();
            }
        });
    }

    private GuiItem createNavigationItem(String name, Runnable onClick) {
        return ItemBuilder.from(new ItemBuilderCustom(Material.ARROW).setName(name).toItemStack()).asGuiItem(event -> {
            onClick.run();
        });
    }
}
