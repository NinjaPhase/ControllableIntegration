package com.ironninja.cint;

import com.ironninja.cint.mods.cobblemon.ControllableCobblemon;
import com.ironninja.cint.mods.refinedstorage.ControllableRefinedStorage;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(ControllableIntegrationMod.MODID)
public class ControllableIntegrationMod {
    public static final String MODID = "controllableintegration";

    public ControllableIntegrationMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onClientSetup);

        if (ModList.get().isLoaded("refinedstorage")) {
            ControllerEvents.GATHER_NAVIGATION_POINTS.register(ControllableRefinedStorage::handleNavigationPoints);
        }
        if (ModList.get().isLoaded("cobblemon")) {
            ControllerEvents.BUTTON.register(ControllableCobblemon::handleButton);
            ControllerEvents.GATHER_NAVIGATION_POINTS.register(ControllableCobblemon::handleNavigationPoints);
            ControllableCobblemon.registerButtonBindings();
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TickEvents.START_CLIENT.register(this::onClientTick);
        });
    }

    private void onClientTick() {
        Controller controller = Controllable.getController();
        if (controller == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            return;
        }

        if (ControllableRefinedStorage.isEnabled()) {
            ControllableRefinedStorage.handleGridScroll(mc, controller);
        }
    }

}
