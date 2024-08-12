package com.adv.mixin;

import com.adv.accessors.AdvancementTabAccessor;
import com.adv.accessors.AdvancementWidgetAccessor;
import com.adv.accessors.AdvancementsScreenAccessor;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AdvancementsScreen.class)

public class AdvancementsScreenMixin extends Screen implements AdvancementsScreenAccessor {
    // Modify the WINDOW_WIDTH constant
    private static final Identifier FRAME_TEXTURE = Identifier.of("advancements-mod", "frame.png");
    private static Identifier WINDOW_TEXTURE = Identifier.of("advancements-mod", "window.png");
    private static Text ADVANCEMENTS_TEXT;
    private Identifier BACKGROUND_TEXTURE = Identifier.of("advancements-mod", "background.png");
    private static final Text SAD_LABEL_TEXT = Text.translatable("advancements.sad_label");
    private static final Text EMPTY_TEXT = Text.translatable("advancements.empty");
    @Shadow
    private ClientAdvancementManager advancementHandler;
    private AdvancementTab selectedTab;
    private final Map<AdvancementEntry, AdvancementTab> tabs = Maps.<AdvancementEntry, AdvancementTab>newLinkedHashMap();
    private int xOffset = -50; // The amount to move the screen to the left
    private AdvancementProgress currentProgress=null;
    private int infoScreenScroll =0;
    private int infoScreenScrollMax =0;
    protected AdvancementsScreenMixin() {
        super(null);
    }
    @Override
    public void setInfoScreenScroll(int infoScreenScroll){
        this.infoScreenScroll=infoScreenScroll;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void moveScreenLeft(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        super.render(context, mouseX, mouseY, delta);
        // Modify the x position
        int i = (this.width - 252) / 2 + xOffset;
        int j = (this.height - 240) / 2;

        // Call the original render method with the new x and y positions
        this.drawAdvancementTree(context, mouseX, mouseY, i, j);
        this.drawWindow(context, i, j);
        this.drawWidgetTooltip(context, mouseX, mouseY, i, j);
    }
    private void drawAdvancementTree(DrawContext context, int mouseX, int mouseY, int x, int y) {
        AdvancementTab advancementTab = this.selectedTab;
        if (advancementTab == null) {
            context.fill(x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
            int i = x + 9 + 117;
            context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_TEXT, i, y + 18 + 56 - 9 / 2, Colors.WHITE);
            context.drawCenteredTextWithShadow(this.textRenderer, SAD_LABEL_TEXT, i, y + 18 + 113 - 9, Colors.WHITE);
        } else {
            advancementTab.render(context, x + 9, y + 18);
        }
    }
    private void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
        if (this.selectedTab != null) {
            context.getMatrices().push();
            context.getMatrices().translate((float)(x + 9), (float)(y + 18), 400.0F);
            RenderSystem.enableDepthTest();
            this.selectedTab.drawWidgetTooltip(context, mouseX - x - 9, mouseY - y - 18, x, y);
            RenderSystem.disableDepthTest();
            context.getMatrices().pop();
        }

        if (this.tabs.size() > 1) {
            for (AdvancementTab advancementTab : this.tabs.values()) {
                if (advancementTab.isClickOnTab(x, y, (double)mouseX, (double)mouseY)) {
                    context.drawTooltip(this.textRenderer, advancementTab.getTitle(), mouseX, mouseY);
                }
            }
        }
    }


//    @Inject(method = "drawWindow", at = @At("HEAD"), cancellable = true)
    public void drawWindow(DrawContext context, int x, int y) {
        drawMainWindow(context,x,y);

        RenderSystem.enableBlend();

        context.drawTexture(BACKGROUND_TEXTURE, x+252, y, 0, 0, 100, 240);
        context.drawTexture(FRAME_TEXTURE, x+252, y, 0, 0, 100, 240);
        int requirementY = y+6;
        int requirementX = x+260;

        Text requirementText = Text.literal("Information");
        context.drawText(MinecraftClient.getInstance().textRenderer, requirementText, requirementX, requirementY, 4210752, false);
        if (currentProgress != null) {
            requirementX +=3;
            requirementY += 15+infoScreenScroll;
            int borderTop = y +15;
            int borderBottom = y+240-10;
            for (String requirement : currentProgress.getUnobtainedCriteria()) {
                if(requirementY>borderTop&&requirementY<borderBottom){
                    requirementText = Text.literal(requirement);
                    context.drawText(MinecraftClient.getInstance().textRenderer, requirementText, requirementX, requirementY, 0xb82525,false);
                }
                requirementY += 10;
            }
            for (String requirement : currentProgress.getObtainedCriteria()) {
                if(requirementY>borderTop&&requirementY<borderBottom) {
                    requirementText = Text.literal(requirement);
                    context.drawText(MinecraftClient.getInstance().textRenderer, requirementText, requirementX, requirementY, 0x25b825, false);
                }
                requirementY += 10;
            }
        }
    }
    public void drawMainWindow(DrawContext context, int x, int y) {
        RenderSystem.enableBlend();
        context.drawTexture(WINDOW_TEXTURE, x, y, 0, 0, 252, 240);
        if (this.tabs.size() > 1) {
            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawBackground(context, x, y, advancementTab == this.selectedTab);
            }

            for (AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawIcon(context, x, y);
            }
        }

        context.drawText(this.textRenderer, this.selectedTab != null ? this.selectedTab.getTitle() : ADVANCEMENTS_TEXT, x + 8, y + 6, 4210752, false);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        int x = (this.width - 252) / 2 + xOffset;
        int mx = (int) (mouseX) - x - 9;
        if(mx>242){

            if(verticalAmount>0){
                if(infoScreenScroll<0){
                    infoScreenScroll+=10*verticalAmount;
                }
            } else if (verticalAmount<0){
                if(infoScreenScroll>infoScreenScrollMax){
                    infoScreenScroll+=10*verticalAmount;
                }
            }
        } else if (this.selectedTab != null) {
            this.selectedTab.move(horizontalAmount * 16.0, verticalAmount * 16.0);
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }

    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void widgetClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();

        if (button == 0) {
            int x = (this.width - 252) / 2 + xOffset; // Apply the xOffset here as well
            int y = (this.height - 240) / 2;
            for (AdvancementTab advancementTab : this.tabs.values()) {
                if (advancementTab.isClickOnTab(x, y, mouseX, mouseY)) {
                    this.advancementHandler.selectTab(advancementTab.getRoot().getAdvancementEntry(), true);
                    break;
                }
            }
            if (this.selectedTab != null) {
                int mx = (int) (mouseX) - x - 9;
                int my = (int) (mouseY) - y - 18;
                ((AdvancementTabAccessor) selectedTab).mouseClicked(mx, my);
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    public void drawRequirements(AdvancementProgress progress) {
//        if (progress != null) {
//            int requirementY = 60; // Adjust this value to position the text correctly
//            int requirementX = 150;
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
        this.currentProgress=progress;
        infoScreenScrollMax=0;
        if(progress!=null){
            for (String requirement : currentProgress.getUnobtainedCriteria()) {
                this.infoScreenScrollMax-=10;
            }
            for (String requirement : currentProgress.getObtainedCriteria()) {
                this.infoScreenScrollMax-=10;
            }
        }
        infoScreenScrollMax+=110;

    }
}
