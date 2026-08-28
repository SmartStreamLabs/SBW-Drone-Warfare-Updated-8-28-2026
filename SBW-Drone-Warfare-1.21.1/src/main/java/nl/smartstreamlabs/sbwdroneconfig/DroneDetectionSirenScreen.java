package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class DroneDetectionSirenScreen extends Screen {
    private final net.minecraft.core.BlockPos blockPos;
    private final List<String> blacklistedGamertags;
    private EditBox input;

    protected DroneDetectionSirenScreen(net.minecraft.core.BlockPos blockPos, List<String> blacklistedGamertags) {
        super(Component.translatable("screen.sbwdroneconfig.siren.title"));
        this.blockPos = blockPos;
        this.blacklistedGamertags = new ArrayList<>(blacklistedGamertags);
    }

    public static void open(OpenSirenConfigMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new DroneDetectionSirenScreen(message.blockPos(), message.blacklistedGamertags()));
    }

    @Override
    protected void init() {
        super.init();
        int panelWidth = 220;
        int left = (width - panelWidth) / 2;
        int top = 42;

        input = new EditBox(font, left + 10, top + 24, panelWidth - 20, 20, Component.translatable("screen.sbwdroneconfig.siren.input"));
        input.setMaxLength(32);
        addRenderableWidget(input);

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.siren.add"), button -> submit(false))
                .pos(left + 10, top + 50)
                .size(96, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.siren.remove"), button -> submit(true))
                .pos(left + 114, top + 50)
                .size(96, 20)
                .build());
    }

    private void submit(boolean remove) {
        String value = input.getValue().trim();
        if (value.isEmpty()) {
            return;
        }
        AddonNetwork.updateSirenBlacklist(new UpdateSirenBlacklistMessage(blockPos, value, remove));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (input != null && input.isFocused() && keyCode == 257) {
            submit(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int panelWidth = 220;
        int panelHeight = 150;
        int left = (width - panelWidth) / 2;
        int top = 32;
        guiGraphics.fill(left, top, left + panelWidth, top + panelHeight, 0xD0101010);
        guiGraphics.drawCenteredString(font, title, width / 2, top + 8, 0xFFFFFF);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.siren.input"), left + 10, top + 14, 0xC0C0C0, false);
        guiGraphics.drawString(font, Component.translatable("screen.sbwdroneconfig.siren.blacklist"), left + 10, top + 80, 0xC0C0C0, false);

        int y = top + 92;
        for (int i = 0; i < Math.min(5, blacklistedGamertags.size()); i++) {
            guiGraphics.drawString(font, "- " + blacklistedGamertags.get(i), left + 14, y + i * 11, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
