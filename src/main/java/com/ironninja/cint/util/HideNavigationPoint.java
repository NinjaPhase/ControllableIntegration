package com.ironninja.cint.util;

import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;

public class HideNavigationPoint extends BasicNavigationPoint {

    public HideNavigationPoint(double x, double y) {
        super(x, y);
    }

    @Override
    public boolean shouldHide() {
        return true;
    }
}
