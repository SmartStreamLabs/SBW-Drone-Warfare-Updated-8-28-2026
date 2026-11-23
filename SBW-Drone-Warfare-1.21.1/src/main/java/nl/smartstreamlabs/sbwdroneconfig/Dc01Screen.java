package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Each bay has its own ordered load, service and launch controls. */
public class Dc01Screen extends AbstractContainerScreen<Dc01Menu> {
    private final Button[][] actions = new Button[3][4];

    public Dc01Screen(Dc01Menu menu, Inventory inv, Component name) {
        super(menu, inv, name);
        imageWidth = 380;
        imageHeight = 226;
    }

    @Override protected void init() {
        imageWidth = Math.min(420, width - 12);
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Fleet / Return"),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,40);}).bounds(leftPos+imageWidth-108,topPos+5,100,14).build());
        int buttonWidth = (imageWidth - 30) / 4;
        for (int slot = 0; slot < 3; slot++) {
            final int bay = slot;
            String[] labels = {"1 Load", "2 Inventory", "3 Launch & Fly", "Unload"};
            int[] ids = {slot, 30 + slot, 3 + slot, 6 + slot};
            for (int action = 0; action < 4; action++) {
                final int id = ids[action];
                actions[bay][action] = addRenderableWidget(Button.builder(Component.literal(labels[action]), b -> {
                    if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                }).bounds(leftPos + 9 + action * (buttonWidth + 4), topPos + 60 + bay * 38, buttonWidth, 18).build());
            }
        }
        updateButtons();
    }

    private boolean equipped() {
        return minecraft.player != null && SbwCompat.isMonitor(minecraft.player.getMainHandItem())
                && SbwCompat.hasEquippedFpvGoggles(minecraft.player);
    }

    private String launchHint(int bay) {
        if (menu.type(bay) == 0) return "Load a drone into this bay first.";
        if (!menu.stopped()) return "Stop the carrier before launching.";
        if (!equipped()) return "Hold an SBW monitor and wear FPV goggles. Close this menu, equip them, then Shift-right-click the carrier (or open its launch menu while driving).";
        if (!menu.ready(bay)) return "Check the drone battery in Inventory and clear the space above this bay. Launch reports the exact server rejection.";
        return "Launch this drone and immediately switch to its camera for manual flight. No separate link or C press needed.";
    }

    private void updateButtons() {
        for (int s = 0; s < 3; s++) {
            boolean loaded = menu.type(s) != 0;
            actions[s][0].active = menu.stopped() && !loaded;
            actions[s][0].setTooltip(Tooltip.create(Component.literal("Load one FPV from your inventory or a compatible placed drone within 6 blocks. HB-01 uses the center bay and the whole deck.")));
            actions[s][1].active = menu.stopped() && loaded;
            actions[s][1].setTooltip(Tooltip.create(Component.literal(loaded ? "Edit the real drone inventory and install a charged battery before launch." : "Load a drone to edit its inventory.")));
            actions[s][2].active = menu.stopped() && loaded && equipped();
            actions[s][2].setTooltip(Tooltip.create(Component.literal(launchHint(s))));
            actions[s][3].active = menu.stopped() && loaded;
            actions[s][3].setTooltip(Tooltip.create(Component.literal("Place this drone on free ground beside the carrier. This does not launch it.")));
        }
    }

    @Override public void containerTick() { super.containerTick(); updateButtons(); }

    @Override protected void renderBg(GuiGraphics g, float partial, int x, int y) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xf51a2525);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xff9db887);
    }

    @Override protected void renderLabels(GuiGraphics g, int x, int y) {
        g.drawString(font, "DC-01 · Drone Launch Bays", 9, 8, 0xe1eadd, false);
        g.drawString(font, menu.stopped() ? "Stopped · Load, service, then Launch & Fly" : "Stop the carrier to use the launch bays", 9, 21, menu.stopped() ? 0x8fdb9a : 0xffb96b, false);
        g.drawString(font, "Capacity: 3 FPVs OR 1 Heavy Bomber (center)", 9, 34, 0xb5c4c4, false);
        for (int s = 0; s < 3; s++) {
            int yy = 49 + s * 38;
            boolean loaded = menu.type(s) != 0;
            String label = Component.translatable("dc01.slot." + s).getString() + ": ";
            if (!loaded) label += "Empty";
            else {
                label += (menu.type(s) == 3 ? "HB-01" : "FPV") + " · " + menu.droneEnergy(s) + "/" + menu.droneCapacity(s) + " FE";
                label += menu.ready(s) ? " · Ready" : " · Check battery / clearance";
            }
            g.drawString(font, font.plainSubstrByWidth(label, imageWidth - 18), 9, yy, loaded && menu.ready(s) ? 0x83df95 : 0xd2decf, false);
        }
        if (menu.reason() != 0) g.drawWordWrap(font, Component.translatable("dc01.reason." + menu.reason()), 9, 166, imageWidth - 18, 0xf3d9a1);
        else g.drawWordWrap(font, Component.literal(equipped() ? "Launch & Fly releases the selected drone and connects your camera automatically." : "To fly: hold an SBW monitor and wear FPV goggles. Then choose Launch & Fly."), 9, 166, imageWidth - 18, 0xf3d9a1);
        String key = Dc01Client.DOCKS.getTranslatedKeyMessage().getString();
        g.drawWordWrap(font, Component.literal("Open: Shift-right-click with monitor, or " + key + " while driving. Inventory keeps your loaded battery and modules."), 9, 198, imageWidth - 18, 0xb5c4c4);
    }

    @Override public void render(GuiGraphics g, int x, int y, float partial) {
        
        super.render(g, x, y, partial);
        renderTooltip(g, x, y);
    }
}
