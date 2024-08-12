package com.adv.mixin;

import com.adv.accessors.AdvancementWidgetAccessor;
import com.adv.accessors.AdvancementsScreenAccessor;
import net.minecraft.advancement.AdvancementDisplays;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin implements AdvancementWidgetAccessor {
    private AdvancementProgress progress;
    private boolean req = false;
    private AdvancementTab tab;

    @Override
    public void setReq(boolean val) {
        this.req = val;
        if(req == false){
            drawRequirements(null);
        }
    }
    @Inject(method = "renderWidgets", at = @At("TAIL"))
    private void renderRequirementsInMainRender(DrawContext context, int x, int y, CallbackInfo ci) {
        if (req) {
            drawRequirements(this.progress);
        }
    }


    public void drawRequirements(AdvancementProgress sendProgress) {
//        if (progress != null) {
//            int requirementY = 60; // Adjust this value to position the text correctly
//            int requirementX = 5;
//
//            Text requirementText = Text.literal("Requirements:");
//            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, requirementText, requirementX, requirementY, 0xFFFFFF);
//            requirementY += 10;
//            for (String requirement : progress.getUnobtainedCriteria()) {
//                requirementText = Text.literal(requirement);
//                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, requirementText, requirementX, requirementY, 0xfc5454);
//                requirementY += 10; // Adjust line height as needed
//            }
//            for (String requirement : progress.getObtainedCriteria()) {
//                requirementText = Text.literal(requirement);
//                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, requirementText, requirementX, requirementY, 0x54fc54);
//                requirementY += 10; // Adjust line height as needed
//            }
//        }
        ((AdvancementsScreenAccessor)(this.tab.getScreen())).drawRequirements(sendProgress);
    }


}
