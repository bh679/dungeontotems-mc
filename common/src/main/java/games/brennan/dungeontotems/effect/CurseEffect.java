package games.brennan.dungeontotems.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * A curse effect. {@code curable == false} gives it no curative items: milk leaves it, and so does
 * another totem's pop ({@code mixin/LivingEntityTotemMixin} keeps uncurable curses through it). Made through
 * {@code Services.HOOKS.curseEffect}: Forge's subclass adds the Rage/Lonely timer drawing.
 */
public class CurseEffect extends MobEffect {

    private final boolean curable;

    public CurseEffect(MobEffectCategory category, int color, boolean curable) {
        super(category, color);
        this.curable = curable;
    }

    public boolean curable() {
        return curable;
    }

    /**
     * Forge's {@code IForgeMobEffect#getCurativeItems} (milk, by default), overridden by name: Forge's milk
     * cures what lists it. Fabric's milk asks {@link #curable()} instead ({@code MilkBucketItemMixin}).
     */
    public List<ItemStack> getCurativeItems() {
        List<ItemStack> items = new ArrayList<>();
        if (curable) items.add(new ItemStack(Items.MILK_BUCKET));
        return items;
    }
}
