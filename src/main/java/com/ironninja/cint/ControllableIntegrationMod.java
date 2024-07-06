package com.ironninja.cint;

import com.mojang.logging.LogUtils;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import com.mrcrayfish.controllable.client.Thumbstick;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.SlotNavigationPoint;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.framework.api.event.TickEvents;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.container.slot.filter.FilterSlot;
import com.refinedmods.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

@Mod(ControllableIntegrationMod.MODID)
public class ControllableIntegrationMod {
    public static final String MODID = "controllableintegration";
    private static final Logger LOGGER = LogUtils.getLogger();

    private long lastGridScroll = 0;

    public ControllableIntegrationMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onClientSetup);

        ControllerEvents.GATHER_NAVIGATION_POINTS.register((points) -> {
            Minecraft mc = Minecraft.getInstance();

            if (ModList.get().isLoaded("refinedstorage")) {
                if (mc.screen instanceof GridScreen screen) {
                    int x = screen.getGuiLeft() + 8;
                    int y = screen.getGuiTop() + 18;
                    if (screen.getGrid().getGridType() == GridType.PATTERN) {
                        points.removeIf(point -> {
                            if (!(point instanceof SlotNavigationPoint slotPoint)) {
                                return false;
                            }
                            if (slotPoint.getSlot() instanceof FilterSlot || slotPoint.getSlot() instanceof FluidFilterSlot) {
                                if (!slotPoint.getSlot().isActive()) {
                                    return true;
                                }
                            }
                            return false;
                        });
                    } else if (screen.getGrid().getGridType() == GridType.CRAFTING) {
                        int sy = screen.getTopHeight() + (screen.getVisibleRows() * 18) - 14;
                        points.add(new BasicNavigationPoint(x + 74 + 3.5, y + sy + 3.5));
                    }
                    for (int i = 0; i < 9 * screen.getVisibleRows(); i++) {
                        points.add(new BasicNavigationPoint(x + 9, y + 9));
                        x += 18;
                        if ((i + 1) % 9 == 0) {
                            x = screen.getGuiLeft() + 8;
                            y += 18;
                        }
                    }
                }
            }
        });

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
        ControllerInput input = Controllable.getInput();
        if (controller == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            return;
        }

        double dir = 0;
        float yValue = com.mrcrayfish.controllable.Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();
        if (Math.abs(yValue) >= 0.5f) {
            dir = -yValue;
        } else {
            this.lastGridScroll = 0;
        }
        long scrollTime = Util.getMillis();
        if (mc.screen instanceof GridScreen) {
            GridScreen screen = (GridScreen) mc.screen;
            if (dir != 0 && scrollTime - this.lastGridScroll >= 150) {
                screen.mouseScrolled(this.getCursorX(), this.getCursorY(), dir);
                this.lastGridScroll = scrollTime;
            }
        }
    }

    private double getCursorX() {
        Minecraft mc = Minecraft.getInstance();
        ControllerInput input = Controllable.getInput();
        double cursorX = mc.mouseHandler.xpos();
        if(Controllable.getController() != null && com.mrcrayfish.controllable.Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            cursorX = input.getVirtualCursorX();
        }
        return cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
    }

    private double getCursorY() {
        Minecraft mc = Minecraft.getInstance();
        ControllerInput input = Controllable.getInput();
        double cursorY = mc.mouseHandler.ypos();
        if(Controllable.getController() != null && com.mrcrayfish.controllable.Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            cursorY = input.getVirtualCursorY();
        }
        return cursorY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
    }

}
