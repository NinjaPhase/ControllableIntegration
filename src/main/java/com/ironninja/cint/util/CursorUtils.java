package com.ironninja.cint.util;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerInput;
import net.minecraft.client.Minecraft;

public class CursorUtils {

    public static double getCursorX() {
        Minecraft mc = Minecraft.getInstance();
        ControllerInput input = Controllable.getInput();
        double cursorX = mc.mouseHandler.xpos();
        if(Controllable.getController() != null && com.mrcrayfish.controllable.Config.CLIENT.client.options.virtualCursor.get() && input.getLastUse() > 0)
        {
            cursorX = input.getVirtualCursorX();
        }
        return cursorX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
    }

    public static double getCursorY() {
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
