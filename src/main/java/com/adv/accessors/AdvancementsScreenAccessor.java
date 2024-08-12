package com.adv.accessors;

import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.gui.DrawContext;

public interface AdvancementsScreenAccessor {
    void drawRequirements(AdvancementProgress progress);
    void setInfoScreenScroll(int infoScreenScroll);
}
