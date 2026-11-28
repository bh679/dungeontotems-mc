package games.brennan.dungeontotems.forge.platform;

import games.brennan.dungeontotems.forge.ForgeCurseEffect;
import games.brennan.dungeontotems.platform.LoaderHooks;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;

public final class ForgeLoaderHooks implements LoaderHooks {

    @Override
    public Attribute stepHeight() {
        return ForgeMod.STEP_HEIGHT_ADDITION.get();
    }

    @Override
    public MobEffect curseEffect(MobEffectCategory category, int color, boolean curable) {
        return new ForgeCurseEffect(category, color, curable);
    }

    @Override
    public boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> into) {
        return ForgeEventFactory.canLivingConvert(entity, into, ticks -> {});
    }

    @Override
    public void onLivingConvert(LivingEntity from, LivingEntity to) {
        ForgeEventFactory.onLivingConvert(from, to);
    }

    @Override
    public boolean isFood(ItemStack stack, LivingEntity entity) {
        return stack.getFoodProperties(entity) != null;
    }

    @Override
    public void cureWithMilk(LivingEntity entity) {
        entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
    }

    @Override
    public boolean shouldRenderEffect(MobEffectInstance effect) {
        return ForgeHooksClient.shouldRenderEffect(effect);
    }
}
