package com.ironninja.cint.mods;

import com.ironninja.cint.util.CursorUtils;
import com.mrcrayfish.controllable.client.Thumbstick;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.SlotNavigationPoint;
import com.mrcrayfish.controllable.client.input.Controller;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.container.slot.filter.FilterSlot;
import com.refinedmods.refinedstorage.container.slot.filter.FluidFilterSlot;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class ControllableRefinedStorage {

    private static long lastGridScroll = 0;

    public static void handleNavigationPoints(List<NavigationPoint> points) {
        if (!isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof GridScreen screen) {
            int x = screen.getGuiLeft() + 8;
            int y = screen.getGuiTop() + 18;
            if (screen.getGrid().getGridType() == GridType.PATTERN) {
                points.removeIf(point -> {
                    if (!(point instanceof SlotNavigationPoint slotPoint)) {
                        return false;
                    }
                    if (slotPoint.getSlot() instanceof FilterSlot || slotPoint.getSlot() instanceof FluidFilterSlot) {
                        return !slotPoint.getSlot().isActive();
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

    public static void handleGridScroll(Minecraft mc, Controller controller) {
        if (ControllableRefinedStorage.isEnabled()) {
            return;
        }

        double dir = 0;
        float yValue =
                com.mrcrayfish.controllable.Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ?
                controller.getRThumbStickYValue() :
                controller.getLThumbStickYValue();
        if (Math.abs(yValue) >= 0.5f) {
            dir = -yValue;
        } else {
            ControllableRefinedStorage.lastGridScroll = 0;
        }
        long scrollTime = Util.getMillis();

        if (mc.screen instanceof GridScreen screen) {
            if (dir != 0 && scrollTime - ControllableRefinedStorage.lastGridScroll >= 150) {
                screen.mouseScrolled(CursorUtils.getCursorX(), CursorUtils.getCursorY(), dir);
                ControllableRefinedStorage.lastGridScroll = scrollTime;
            }
        }
    }

    public static boolean isEnabled() {
        return ModList.get().isLoaded("refinedstorage");
    }

}
