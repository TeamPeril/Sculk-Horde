package com.github.sculkhorde.mixin.structures;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplate.class)
public interface StructureTemplateAccessor {
    @Accessor("palettes")
    List<StructureTemplate.Palette> getPalettes();

    @Accessor("size")
    Vec3i getSize();

    @Accessor("entityInfoList")
    List<StructureTemplate.StructureEntityInfo> getEntityInfoList();
}
