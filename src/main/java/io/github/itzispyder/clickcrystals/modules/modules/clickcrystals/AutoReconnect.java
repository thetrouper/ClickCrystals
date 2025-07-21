package io.github.itzispyder.clickcrystals.modules.modules.clickcrystals;

import io.github.itzispyder.clickcrystals.events.EventHandler;
import io.github.itzispyder.clickcrystals.events.events.networking.GameJoinEvent;
import io.github.itzispyder.clickcrystals.events.events.networking.GameLeaveEvent;
import io.github.itzispyder.clickcrystals.modules.Categories;
import io.github.itzispyder.clickcrystals.modules.modules.ListenerModule;
import io.github.itzispyder.clickcrystals.modules.settings.IntegerSetting;
import io.github.itzispyder.clickcrystals.modules.settings.SettingSection;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class AutoReconnect extends ListenerModule {

    private final SettingSection scGeneral = getGeneralSection();
    private final IntegerSetting delay = scGeneral.add(IntegerSetting.create()
            .name("delay")
            .description("Seconds after disconnecting to reconnect.")
            .min(0)
            .max(60)
            .def(5)
            .build());

    public AutoReconnect() {
        super("auto-reconnect", Categories.CLIENT,"Reconnects you to the server you got disconnected from");
    }

    public ServerInfo info = null;
    public long connectionDebounce;
    public long disconnectDebounce;

    @EventHandler
    public void onJoin(GameJoinEvent e) {
        System.out.println("Joined a game!");
        if (System.currentTimeMillis() - connectionDebounce < 100)
            return;

        connectionDebounce = System.currentTimeMillis();

        info = mc.getCurrentServerEntry();
    }

    @EventHandler
    public void onLeave(GameLeaveEvent e) {
        System.out.println("Reconnecting in " + delay.getVal() + " seconds!");
        if (System.currentTimeMillis() - disconnectDebounce < 100)
            return;

        disconnectDebounce = System.currentTimeMillis();

        system.scheduler.runDelayedTask(() -> {
            if (info == null)
                return;


            connect(info);
        },delay.getVal() * 1000);
    }



    @Override
    protected void onEnable() {
        super.onEnable();
        /*if (registered) return;
        registered = true;
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (!this.isEnabled())
                return;

            if (System.currentTimeMillis() - connectionDebounce < 100)
                return;

            connectionDebounce = System.currentTimeMillis();

            info = mc.getCurrentServerEntry();
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            system.scheduler.runDelayedTask(()->{
                if (!this.isEnabled())
                    return;

                if (System.currentTimeMillis() - disconnectDebounce < 100)
                    return;

                disconnectDebounce = System.currentTimeMillis();

                if (info == null)
                    return;

                System.out.println("Reconnecting!");
                connect(info);
            },delay.getVal() * 1000);
        });*/
    }

    private void connect(ServerInfo connection) {
        String address = connection.address;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            TitleScreen screen = new TitleScreen();
            mc.execute(()->{
                mc.setScreen(screen);
                ConnectScreen.connect(screen,mc,ServerAddress.parse(address),connection,true, null);
            });
        }
    }
}
