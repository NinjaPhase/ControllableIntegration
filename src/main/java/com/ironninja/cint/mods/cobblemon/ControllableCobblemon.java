package com.ironninja.cint.mods.cobblemon;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleBackButton;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGeneralActionSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleSwitchPokemonSelection;
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleOptionTile;
import com.cobblemon.mod.common.client.gui.pasture.PasturePCGUIConfiguration;
import com.cobblemon.mod.common.client.gui.pasture.PasturePokemonScrollList;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblemon.mod.common.client.gui.summary.widgets.EvolutionSelectScreen;
import com.cobblemon.mod.common.client.gui.summary.widgets.PartySlotWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.PartyWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MoveSlotWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MoveSwapScreen;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MovesWidget;
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.ironninja.cint.util.HideNavigationPoint;
import com.ironninja.cint.util.OffsetListEntryNavigationPoint;
import com.ironninja.cint.util.PointAnchor;
import com.mrcrayfish.controllable.client.binding.BindingContext;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.IBindingContext;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.ListEntryNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ControllableCobblemon {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllableCobblemon.class);

    private static final IBindingContext IN_SUMMARY_OR_BOX = new IBindingContext() {
        @Override
        public boolean isActive() {
            return Minecraft.getInstance().screen instanceof Summary || Minecraft.getInstance().screen instanceof PCGUI;
        }

        @Override
        public boolean conflicts(IBindingContext context) {
            return this == context;
        }
    };
    private static final ButtonBinding THROW_POKEMON = new ButtonBinding(-1, "key.cobblemon.throwpartypokemon", "key.categories.cobblemon",
                                                                         BindingContext.IN_GAME);
    private static final ButtonBinding PARTY_SHIFT_UP = new ButtonBinding(-1, "key.cobblemon.upshiftparty", "key.categories.cobblemon",
                                                                          BindingContext.IN_GAME);
    private static final ButtonBinding PARTY_SHIFT_DOWN = new ButtonBinding(-1, "key.cobblemon.downshiftparty", "key.categories.cobblemon",
                                                                          BindingContext.IN_GAME);
    private static final ButtonBinding PREV_SUMMARY_TAB = new ButtonBinding(Buttons.LEFT_BUMPER, "controllableintegration.cobblemon.prev_summary_tab", "key.categories.cobblemon",
                                                                            IN_SUMMARY_OR_BOX);
    private static final ButtonBinding NEXT_SUMMARY_TAB = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllableintegration.cobblemon.next_summary_tab", "key.categories.cobblemon",
                                                                            IN_SUMMARY_OR_BOX);

    public static void handleNavigationPoints(List<NavigationPoint> points) {
        if (!isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof BattleGUI screen) {
            points.clear();
            if (screen.getCurrentActionSelection() instanceof BattleGeneralActionSelection selection) {
                selection.getTiles().parallelStream().forEach(tile -> {
                    points.add(new HideNavigationPoint(tile.getX() + (BattleOptionTile.OPTION_WIDTH / 2.0), tile.getY() + (BattleOptionTile.OPTION_HEIGHT / 2.0)));
                });
            } else if (screen.getCurrentActionSelection() instanceof BattleMoveSelection selection) {
                selection.getBaseTiles().parallelStream().forEach(tile -> {
                    points.add(new HideNavigationPoint(tile.getX() + (BattleMoveSelection.MOVE_WIDTH / 2.0), tile.getY() + (BattleMoveSelection.MOVE_HEIGHT / 2.0)));
                });
                BattleBackButton backBtn = selection.getBackButton();
                points.add(new HideNavigationPoint(backBtn.getX() + ((BattleBackButton.WIDTH * BattleBackButton.SCALE) / 2.), backBtn.getY() + ((BattleBackButton.HEIGHT * BattleBackButton.SCALE) / 2.)));
            } else if (screen.getCurrentActionSelection() instanceof BattleSwitchPokemonSelection selection) {
                selection.getTiles().parallelStream().forEach(tile -> {
                    points.add(new BasicNavigationPoint(tile.getX() + (BattleMoveSelection.MOVE_WIDTH / 2.0), tile.getY() + (BattleMoveSelection.MOVE_HEIGHT / 2.0)));
                });
                BattleBackButton backBtn = selection.getBackButton();
                points.add(new HideNavigationPoint(backBtn.getX() + ((BattleBackButton.WIDTH * BattleBackButton.SCALE) / 2.), backBtn.getY() + ((BattleBackButton.HEIGHT * BattleBackButton.SCALE) / 2.)));
            }
        } else if (mc.screen instanceof Summary screen) {
            points.clear();
            int x = (screen.width - (Summary.BASE_WIDTH)) / 2;
            int y = (screen.height - Summary.BASE_HEIGHT) / 2;
            // Back Button
            points.add(new HideNavigationPoint(x + 302 + (26. / 2.), y + 145 + (13. / 2.)));

            // Tab Buttons
            points.add(new BasicNavigationPoint(x + 78 + (50 / 2.), y - 1 + (13 / 2.)));
            points.add(new BasicNavigationPoint(x + 119 + (50 / 2.), y - 1 + (13 / 2.)));
            points.add(new BasicNavigationPoint(x + 160 + (50 / 2.), y - 1 + (13 / 2.)));

            // Evolve Buttons
            Pokemon p = getSelectedPokemon(screen);
            if (p != null && !p.getEvolutionProxy().client().isEmpty()) {
                points.add(new HideNavigationPoint(x + 12F + (54. / 2.), y + 145 + (15. / 2.)));
            }

            Object mainScreen = getMainScreen(screen);
            if (mainScreen instanceof MovesWidget movesWidget) {
                int wx = movesWidget.getX();
                int wy = movesWidget.getY();
                List<Move> moves = getSelectedPokemon(screen).getMoveSet().getMoves();
                for (int i = 0; i < moves.size(); i++) {
                    Move m = moves.get(i);
                    if (m == null) {
                        continue;
                    }
                    int mx = wx + 13;
                    int my = wy + 6 + (MoveSlotWidget.MOVE_HEIGHT + 3) * i;
                    // Main
                    points.add(new HideNavigationPoint(mx + (MoveSlotWidget.MOVE_WIDTH / 2.), my + (MoveSlotWidget.MOVE_HEIGHT / 2.)));
                    // Re-order
                    points.add(new BasicNavigationPoint((mx - 11.5) + 2, my + 6 + 1.5));
                    points.add(new BasicNavigationPoint((mx - 11.5) + 2, my + 13 + 1.5));
                    // Switch
                    points.add(new HideNavigationPoint((mx + 114.5) + 3, my + 6.5 + 4.5));
                }
            }

            if (screen.getSideScreen() instanceof PartyWidget widget) {
                int wx = widget.getX();
                int wy = widget.getY();
                // Swap Button
                points.add(new HideNavigationPoint(wx + 80., wy - 9.));

                // Pokemon
                ClientParty party = CobblemonClient.INSTANCE.getStorage().getMyParty();
                for (int i = 0; i < 6; i++) {
                    if (widget.getDraggedSlot() == null && party.get(i) == null) {
                        continue;
                    }
                    int px = wx + 6, py = wy + 7;

                    if (i >  0) {
                        boolean isEven = i % 2 == 0;
                        int offsetIdx = (i - (isEven ? 0 : 1)) / 2;
                        int offsetX = isEven ? 0 : 51;
                        int offsetY = isEven ? 0 : 8;
                        px += offsetX;
                        py += (32 * offsetIdx) + offsetY;
                    }
                    points.add(new BasicNavigationPoint(px + (PartySlotWidget.WIDTH / 2.), py + (PartySlotWidget.HEIGHT / 2.)));
                }
            } else if (screen.getSideScreen() instanceof MoveSwapScreen widget) {
                List<MoveSwapScreen.MoveSlot> slots = widget.children();
                for (int i = 0; i < slots.size(); i++) {
                    MoveSwapScreen.MoveSlot slot = slots.get(i);
                    if (slot == null) {
                        continue;
                    }
                    points.add(new ListEntryNavigationPoint(widget, slot, i, 1));
                }
            } else if (screen.getSideScreen() instanceof EvolutionSelectScreen widget) {
                List<EvolutionSelectScreen.EvolveSlot> slots = widget.children();
                for (int i = 0; i < slots.size(); i++) {
                    EvolutionSelectScreen.EvolveSlot slot = slots.get(i);
                    if (slot == null) {
                        continue;
                    }
                    points.add(new ListEntryNavigationPoint(widget, slot, i, 1));
                }
            }
        } else if (mc.screen instanceof PCGUI screen) {
            points.clear();
            int x = (screen.width - PCGUI.BASE_WIDTH) / 2;
            int y = (screen.height - PCGUI.BASE_HEIGHT) / 2;
            StorageWidget storage = getStorageWidget(screen);

            // add exit button
            points.add(new HideNavigationPoint(x + 320 + (26. / 2.), y + 186 + (13. / 2.)));

            // add forward/backwards buttons
            points.add(new HideNavigationPoint(x + 221 + 2, y + 17 + 2.5));
            points.add(new HideNavigationPoint(x + 119 + 2, y + 17 + 2.5));

            // add release button
            if (storage != null && storage.canDeleteSelected()) {
                if (!storage.getDisplayConfirmRelease()) {
                    points.add(new HideNavigationPoint(x + 85 + 194 + (58. / 2.), y + 27 + 124 + (16. / 2.)));
                } else {
                    points.add(new HideNavigationPoint(x + 85 + 190 + (30. / 2.), y + 27 + 131 + (13. / 2.)));
                    points.add(new HideNavigationPoint(x + 85 + 226 + (30. / 2.), y + 27 + 131 + (13. / 2.)));
                }
            }

            // add storage slots
            for (int slot = 0; slot < 30; slot++) {
                int col = slot % 6;
                int row = slot / 6;
                int sx = x + 85 + StorageWidget.BOX_SLOT_START_OFFSET_X + (col * (StorageSlot.SIZE + StorageWidget.BOX_SLOT_PADDING));
                int sy = y + 27 + StorageWidget.BOX_SLOT_START_OFFSET_Y + (row * (StorageSlot.SIZE + StorageWidget.BOX_SLOT_PADDING));
                points.add(new BasicNavigationPoint(sx + (StorageSlot.SIZE / 2.), sy + (StorageSlot.SIZE / 2.)));
            }

            if (screen.getConfiguration().getShowParty()) {
                // add party slots
                ClientParty party = CobblemonClient.INSTANCE.getStorage().getMyParty();
                for (int slot = 0; slot < 6; slot++) {
                    if (storage != null && storage.getGrabbedSlot() == null && party.get(slot) == null) {
                        continue;
                    }
                    int px = x + 85 + StorageWidget.PARTY_SLOT_START_OFFSET_X;
                    int py = y + 27 + StorageWidget.PARTY_SLOT_START_OFFSET_Y;

                    if (slot > 0) {
                        boolean isEven = slot % 2 == 0;
                        int offsetIdx = (slot - (isEven ? 0 : 1)) / 2;
                        int offsetX = isEven ? 0 : (StorageSlot.SIZE + StorageWidget.PARTY_SLOT_PADDING);
                        int offsetY = isEven ? 0 : 8;

                        px += offsetX;
                        py += ((StorageSlot.SIZE + StorageWidget.PARTY_SLOT_PADDING) * offsetIdx) + offsetY;
                    }

                    points.add(new BasicNavigationPoint(px + (StorageSlot.SIZE / 2.), py + (StorageSlot.SIZE / 2.)));
                }
            }

            // Pasture
            if (screen.getConfiguration() instanceof PasturePCGUIConfiguration pasture) {
                PasturePokemonScrollList scrollList = storage.getPastureWidget().getPastureScrollList();
                List<PasturePokemonScrollList.PastureSlot> slots = scrollList.children();
                for (int i = 0; i < slots.size(); i++) {
                    if (slots.get(i) == null || !slots.get(i).canUnpasture()) {
                        continue;
                    }
                    points.add(new OffsetListEntryNavigationPoint(scrollList, slots.get(i), i, 1, PointAnchor.TOP_LEFT, 2, 15, true));
                }

                points.add(new HideNavigationPoint(x + 85 + 182 + 6 + (70. / 2.), y + 27 - 19 + 153 + (17. / 2.)));
            }
        }
    }

    public static void registerButtonBindings() {
        if (!isEnabled()) {
            return;
        }
        BindingRegistry.getInstance().register(PREV_SUMMARY_TAB);
        BindingRegistry.getInstance().register(NEXT_SUMMARY_TAB);
        BindingRegistry.getInstance().register(THROW_POKEMON);
        BindingRegistry.getInstance().register(PARTY_SHIFT_UP);
        BindingRegistry.getInstance().register(PARTY_SHIFT_DOWN);
    }

    public static boolean handleButton(Controller controller) {
        if (!isEnabled()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            if (THROW_POKEMON.isButtonPressed()) {
                PartySendBinding.INSTANCE.onRelease();
                return true;
            } else if (PARTY_SHIFT_UP.isButtonPressed()) {
                CobblemonClient.INSTANCE.getStorage().shiftSelected(false);
                return true;
            } else if (PARTY_SHIFT_DOWN.isButtonPressed()) {
                CobblemonClient.INSTANCE.getStorage().shiftSelected(true);
                return true;
            }
        } else {
            if (PREV_SUMMARY_TAB.isButtonPressed()) {
                if (mc.screen instanceof Summary screen) {
                    int i = getMainScreenIndex(screen);
                    setMainScreen(screen, Math.max(0, i - 1));
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F));
                    return true;
                } else if (mc.screen instanceof PCGUI screen) {
                    StorageWidget storageWidget = getStorageWidget(screen);
                    if (storageWidget != null) {
                        storageWidget.setBox(storageWidget.getBox() - 1);
                    }
                }
                return true;
            } else if (NEXT_SUMMARY_TAB.isButtonPressed()) {
                if (mc.screen instanceof Summary screen) {
                    int i = getMainScreenIndex(screen);
                    setMainScreen(screen, Math.min(2, i + 1));
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F));
                    return true;
                } else if (mc.screen instanceof PCGUI screen) {
                    StorageWidget storageWidget = getStorageWidget(screen);
                    if (storageWidget != null) {
                        storageWidget.setBox(storageWidget.getBox() + 1);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static StorageWidget getStorageWidget(PCGUI gui) {
        try {
            Field f = PCGUI.class.getDeclaredField("storageWidget");
            f.setAccessible(true);
            StorageWidget widget = (StorageWidget) f.get(gui);
            f.setAccessible(false);
            return widget;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public static void setMainScreen(Summary summary, int i) {
        try {
            Method m = summary.getClass().getDeclaredMethod("displayMainScreen", int.class);
            m.setAccessible(true);
            m.invoke(summary, i);
            m.setAccessible(false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static int getMainScreenIndex(Summary summary) {
        try {
            Field f = summary.getClass().getDeclaredField("mainScreenIndex");
            f.setAccessible(true);
            int i = (int) f.get(summary);
            f.setAccessible(false);
            return i;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return -1;
    }

    public static Object getMainScreen(Summary summary) {
        try {
            Field f = Summary.class.getDeclaredField("mainScreen");
            f.setAccessible(true);
            Object o = f.get(summary);
            f.setAccessible(false);
            return o;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public static Pokemon getSelectedPokemon(Summary summary) {
        try {
            Field f = Summary.class.getDeclaredField("selectedPokemon");
            f.setAccessible(true);
            Pokemon p = (Pokemon) f.get(summary);
            f.setAccessible(false);
            return p;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public static boolean isEnabled() {
        return ModList.get().isLoaded("cobblemon");
    }

}
