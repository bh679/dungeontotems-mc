package games.brennan.dungeontotems.platform;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

/** The odd pieces where a loader adds to vanilla. */
public interface LoaderHooks {

    /** The player's step-height bonus, added to vanilla's 0.6 (Forge's {@code step_height_addition}). */
    Attribute stepHeight();

    /** A curse effect (Forge adds the Rage/Lonely timer drawing through its client extension). */
    MobEffect curseEffect(MobEffectCategory category, int color, boolean curable);

    /** Whether another mod lets this entity turn into {@code into} (Forge's {@code LivingConversionEvent.Pre}). */
    boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> into);

    /** Tells other mods an entity turned into another (Forge's {@code LivingConversionEvent.Post}). */
    void onLivingConvert(LivingEntity from, LivingEntity to);

    /** Whether this stack is food for this entity (Forge lets a stack decide). */
    boolean isFood(ItemStack stack, LivingEntity entity);

    /** What drinking milk does to effects: cures every one milk cures (not the uncurable curses). */
    void cureWithMilk(LivingEntity entity);

    /** Client: whether the effect list shows this effect (Forge lets a mod hide one). */
    boolean shouldRenderEffect(MobEffectInstance effect);
}
