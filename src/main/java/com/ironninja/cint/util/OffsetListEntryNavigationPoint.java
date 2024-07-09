package com.ironninja.cint.util;

import com.mrcrayfish.controllable.client.gui.navigation.HideCursor;
import com.mrcrayfish.controllable.client.gui.navigation.ListEntryNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.SkipItem;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

public class OffsetListEntryNavigationPoint extends NavigationPoint {
    private final AbstractSelectionList<?> list;
    private final GuiEventListener listEntry;
    private final int index;
    private final int itemHeight;
    private final int dir;
    private int itemY;
    private boolean hideCursor;
    private PointAnchor anchor;
    private int offsetX, offsetY;
    private boolean shouldHide;

    public OffsetListEntryNavigationPoint(AbstractSelectionList<?> list, GuiEventListener listEntry, int index, int dir, PointAnchor anchor, int offsetX, int offsetY, boolean shouldHide) {
        super(0.0D, 0.0D, Type.BASIC);
        this.list = list;
        this.listEntry = listEntry;
        this.index = index;
        this.itemHeight = ClientServices.CLIENT.getListItemHeight(this.list);
        this.dir = dir;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.anchor = anchor;
        this.itemY = ClientServices.CLIENT.getAbstractListRowTop(this.list, index) + offsetY - 2;
        switch (anchor) {
            case CENTER:
                this.itemY += this.itemHeight / 2;
                break;
        }
        this.shouldHide = shouldHide;
    }

    public double distanceTo(double x, double y) {
        return Math.sqrt(Math.pow(this.getX() - x, 2.0D) + Math.pow(this.getY() - y, 2.0D));
    }

    public double getX() {
        return switch (this.anchor) {
            case CENTER -> (double) (this.list.getRowLeft() + offsetX + this.list.getRowWidth() / 2);
            case TOP_LEFT -> (double) (this.list.getRowLeft() + offsetX);
        };
    }

    public double getY() {
        return (double)this.itemY;
    }

    public void onNavigate() {
        int index = this.index;
        GuiEventListener entry = this.listEntry;
        List<? extends GuiEventListener> children = this.list.children();
        int rowTop;
        if (entry instanceof SkipItem) {
            rowTop = index + this.dir;
            if (rowTop >= 0 && rowTop < children.size()) {
                index = rowTop;
                entry = (GuiEventListener)children.get(rowTop);
            }
        }

        if (index + this.dir == 0 && children.size() > 0 && children.get(0) instanceof SkipItem) {
            entry = (GuiEventListener)children.get(0);
        }

        this.hideCursor = entry instanceof HideCursor;
        rowTop = ClientServices.CLIENT.getAbstractListRowTop(this.list, index);
        int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(this.list, index);
        int listTop = ClientServices.CLIENT.getAbstractListTop(this.list);
        int listBottom = ClientServices.CLIENT.getAbstractListBottom(this.list);
        double scroll;
        if (rowTop < listTop + this.itemHeight / 2) {
            scroll = (double)(this.list.children().indexOf(entry) * this.itemHeight - this.itemHeight / 2);
            this.list.setScrollAmount(scroll);
        }

        if (rowBottom > listBottom - this.itemHeight / 2) {
            scroll = (double)(this.list.children().indexOf(entry) * this.itemHeight + this.itemHeight - (listBottom - listTop) + 4 + this.itemHeight / 2);
            this.list.setScrollAmount(scroll);
        }

        this.itemY = ClientServices.CLIENT.getAbstractListRowTop(this.list, index) + offsetY - 2;
        switch (anchor) {
            case CENTER:
                this.itemY += this.itemHeight / 2;
                break;
        }
    }

    public boolean shouldHide() {
        return this.hideCursor || this.shouldHide;
    }
}
