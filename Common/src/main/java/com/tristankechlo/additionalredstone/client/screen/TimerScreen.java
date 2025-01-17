package com.tristankechlo.additionalredstone.client.screen;

import com.tristankechlo.additionalredstone.Constants;
import com.tristankechlo.additionalredstone.blockentity.TimerBlockEntity;
import com.tristankechlo.additionalredstone.network.IPacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.time.LocalTime;

public class TimerScreen extends Screen {

    private static final Component TITLE = Component.translatable("screen.additionalredstone.oscillator");
    private static final ResourceLocation ERROR = new ResourceLocation(Constants.MOD_ID, "textures/gui/icons.png");
    private EditBox powerUpWidget;
    private EditBox powerDownWidget;
    private EditBox intervalWidget;
    private Button saveButton;
    private Button cancelButton;
    private BlockPos pos;
    private int powerUpTime;
    private int powerDownTime;
    private int interval;
    private boolean powerUpError = false;
    private boolean powerDownError = false;
    private boolean intervalError = false;

    public TimerScreen(int powerUp, int powerDown, int interval, BlockPos pos) {
        super(TITLE);
        this.powerUpTime = powerUp;
        this.powerDownTime = powerDown;
        this.interval = interval;
        this.pos = pos;
    }

    @Override
    public void tick() {
        this.powerUpWidget.tick();
        this.powerDownWidget.tick();
        this.intervalWidget.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        this.powerUpWidget = new EditBox(this.font, this.width / 2 + 32, 60, 98, 20, Component.translatable("screen.additionalredstone.timer.power.on"));
        this.powerDownWidget = new EditBox(this.font, this.width / 2 + 32, 90, 98, 20, Component.translatable("screen.additionalredstone.timer.power.off"));
        this.intervalWidget = new EditBox(this.font, this.width / 2 + 32, 120, 98, 20, Component.translatable("screen.additionalredstone.timer.interval"));
        this.addWidget(this.powerUpWidget);
        this.addWidget(this.powerDownWidget);
        this.addWidget(this.intervalWidget);
        this.powerUpWidget.setMaxLength(5);
        this.powerDownWidget.setMaxLength(5);
        this.intervalWidget.setMaxLength(5);
        this.setInitialFocus(this.powerUpWidget);
        this.powerUpWidget.setFocused(true);
        this.powerUpWidget.setValue(String.valueOf(this.powerUpTime));
        this.powerDownWidget.setValue(String.valueOf(this.powerDownTime));
        this.intervalWidget.setValue(String.valueOf(this.interval));

        this.saveButton = new Button.Builder(Component.translatable("screen.additionalredstone.save"), (b) -> this.save())
                .pos(this.width / 2 - 110, 160).size(100, 20).build();
        this.cancelButton = new Button.Builder(Component.translatable("screen.additionalredstone.cancel"), (b) -> this.cancel())
                .pos(this.width / 2 + 10, 160).size(100, 20).build();
        this.addRenderableWidget(saveButton);
        this.addRenderableWidget(cancelButton);
    }

    private void save() {
        int powerUp = this.getPowerUpTime();
        int powerDown = this.getPowerDownTime();
        int interval = this.getInterval();
        if (this.powerUpError || this.powerDownError || this.intervalError) {
            return;
        }
        IPacketHandler.INSTANCE.sendPacketSetTimerValues(powerUp, powerDown, interval, pos);
        this.onClose();
    }

    private void cancel() {
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.powerUpWidget.render(graphics, mouseX, mouseY, partialTicks);
        this.powerDownWidget.render(graphics, mouseX, mouseY, partialTicks);
        this.intervalWidget.render(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, Component.translatable("screen.additionalredstone.timer.description"),
                this.width / 2, 30, Constants.TEXT_COLOR_SCREEN);

        graphics.drawString(this.font, Component.translatable("screen.additionalredstone.timer.power.on"),
                this.width / 2 - 130, 65, Constants.TEXT_COLOR_SCREEN);
        graphics.drawString(this.font, Component.translatable("screen.additionalredstone.timer.power.off"),
                this.width / 2 - 130, 95, Constants.TEXT_COLOR_SCREEN);
        graphics.drawString(this.font, Component.translatable("screen.additionalredstone.timer.interval"),
                this.width / 2 - 130, 125, Constants.TEXT_COLOR_SCREEN);

        if (this.powerUpError) {
            graphics.blit(ERROR, this.width / 2 + 140, 61, 0, 0, 18, 18, 18, 18);
        }
        if (this.powerDownError) {
            graphics.blit(ERROR, this.width / 2 + 140, 91, 0, 0, 18, 18, 18, 18);
        }
        if (this.intervalError) {
            graphics.blit(ERROR, this.width / 2 + 140, 121, 0, 0, 18, 18, 18, 18);
        }
    }

    private int getPowerUpTime() {
        int powerUp = 0;
        String text = this.powerUpWidget.getValue();
        if (text.equalsIgnoreCase("24:00")) {
            text = "00:00";
        }
        try {
            LocalTime time = LocalTime.parse(text);
            powerUp = (time.getHour() * 1000) + (int) (time.getMinute() * 16.67) - 6000;
            if (powerUp < 0) {
                powerUp = 24000 + powerUp;
            }
            this.powerUpError = false;
        } catch (Exception e) {
            try {
                powerUp = Integer.parseInt(text);
            } catch (Exception ex) {
                this.powerUpError = true;
            }
        }
        return Mth.clamp(powerUp, TimerBlockEntity.minTime, TimerBlockEntity.maxTime);
    }

    private int getPowerDownTime() {
        int powerDown = 0;
        String text = this.powerDownWidget.getValue();
        if (text.equalsIgnoreCase("24:00")) {
            text = "00:00";
        }
        try {
            LocalTime time = LocalTime.parse(text);
            powerDown = (time.getHour() * 1000) + (int) (time.getMinute() * 16.67) - 6000;
            if (powerDown < 0) {
                powerDown = 24000 + powerDown;
            }
            this.powerDownError = false;
        } catch (Exception e) {
            try {
                powerDown = Integer.parseInt(text);
            } catch (Exception ex) {
                this.powerDownError = true;
            }
        }
        return Mth.clamp(powerDown, TimerBlockEntity.minTime, TimerBlockEntity.maxTime);
    }

    private int getInterval() {
        int interval = 0;
        try {
            interval = Integer.parseInt(this.intervalWidget.getValue());
            this.intervalError = false;
        } catch (Exception e) {
            this.intervalError = true;
        }
        return Mth.clamp(interval, 1, 1000);
    }

}
