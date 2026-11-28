package games.brennan.dungeontotems.fabric.platform;

import games.brennan.dungeontotems.effect.CurseEffect;
import games.brennan.dungeontotems.fabric.FabricAttributes;
import games.brennan.dungeontotems.platform.LoaderHooks;
import games.brennan.dungeontotems.platform.event.MobEffectEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class FabricLoaderHooks implements LoaderHooks {

    @Override
    public Attribute stepHeight() {
        return FabricAttributes.stepHeight();
    }

    @Override
    public MobEffect curseEffect(MobEffectCategory category, int color, boolean curable) {
        return new CurseEffect(category, color, curable);
    }

    /** Fabric has no conversion event: nothing to ask. */
    @Override
    public boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> into) {
        return true;
    }

    @Override
    public void onLivingConvert(LivingEntity from, LivingEntity to) {}

    @Override
    public boolean isFood(ItemStack stack, LivingEntity entity) {
        return stack.getItem().getFoodProperties() != null;
    }

    /**
     * As Forge's milk ({@code curePotionEffects}): every effect milk cures, each asked first
     * ({@link MobEffectEvent.Remove}, which can keep it). A {@link CurseEffect} that isn't curable stays.
     */
    @Override
    public void cureWithMilk(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        List<MobEffectInstance> effects = List.copyOf(entity.getActiveEffects());
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect() instanceof CurseEffect curse && !curse.curable()) continue;
            entity.removeEffect(effect.getEffect()); // fires MobEffectEvent.Remove (LivingEntityEffectsMixin)
        }
    }

    /** Fabric lets no mod hide an effect from the list. */
    @Override
    public boolean shouldRenderEffect(MobEffectInstance effect) {
        return true;
    }
}
