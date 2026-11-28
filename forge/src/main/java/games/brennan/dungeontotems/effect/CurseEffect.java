package games.brennan.dungeontotems.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A curse effect. {@code curable == false} gives it no curative items: milk leaves it, and so does
 * another totem's pop ({@code mixin/LivingEntityTotemMixin} keeps uncurable curses through it).
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

    @Override
    public List<ItemStack> getCurativeItems() {
        return curable ? super.getCurativeItems() : new ArrayList<>();
    }

    /** Client only (Forge calls it on the client): Rage and Lonely draw their timer on the effect box. */
    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(games.brennan.dungeontotems.client.TimerCurseIcons.EXTENSION);
    }
}
