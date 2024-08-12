package com.adv.mixin;

import com.adv.accessors.AdvancementTabAccessor;
import com.adv.accessors.AdvancementWidgetAccessor;
import com.adv.accessors.AdvancementsScreenAccessor;
import com.google.common.collect.Maps;
import net.minecraft.advancement.AdvancementDisplays;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin implements AdvancementTabAccessor {
    @Shadow
    private Map<AdvancementEntry, AdvancementWidget> widgets = Maps.<AdvancementEntry, AdvancementWidget>newLinkedHashMap();
    @Shadow
    private AdvancementsScreen screen;
    @Shadow @Final
    private double originX;
    @Shadow @Final
    private double originY;
    private boolean clickLock= false;

    @Inject(method = "drawWidgetTooltip", at = @At("TAIL"))
    public void drawRequirements(DrawContext context, int mouseX, int mouseY, int x, int y, CallbackInfo ci) {
        if(!clickLock){
            int i = MathHelper.floor(this.originX);
            int j = MathHelper.floor(this.originY);
            if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
                AdvancementWidget found = null;
                for (AdvancementWidget advancementWidget : this.widgets.values()) {
                    if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
                        ((AdvancementWidgetAccessor)advancementWidget).setReq(true);
                        ((AdvancementsScreenAccessor)screen).setInfoScreenScroll(0);
                        found=advancementWidget;
                        break;
                    }
                }
                if(found!=null){
                    for (AdvancementWidget advancementWidget : this.widgets.values()) {
                        if (!advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
                            ((AdvancementWidgetAccessor)advancementWidget).setReq(false);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY){
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
            AdvancementWidget found = null;
            for (AdvancementWidget advancementWidget : this.widgets.values()) {
                if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
                    ((AdvancementWidgetAccessor)advancementWidget).setReq(true);
                    ((AdvancementsScreenAccessor)screen).setInfoScreenScroll(0);
                    found=advancementWidget;
                    clickLock=true;
                    break;
                }
            }
            if(found!=null){
                for (AdvancementWidget advancementWidget : this.widgets.values()) {
                    if (!advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
                        ((AdvancementWidgetAccessor)advancementWidget).setReq(false);
                    }
                }
            } else{
                for (AdvancementWidget advancementWidget : this.widgets.values()) {
                    ((AdvancementWidgetAccessor)advancementWidget).setReq(false);
                    clickLock=false;
                }
            }
        }
    }
}
