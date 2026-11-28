package games.brennan.dungeontotems.morph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;

/**
 * One morph form's place in the pick: which pool it is drawn from, how often, and how many seconds
 * the morph lasts once it lands on this form. Read from {@code data/<namespace>/morph_forms/*.json}
 * by {@link MorphFormTable} (main uses a NeoForge data map), so a datapack can change it; the shipped values come from Brennan's table. A weight
 * of 0 leaves the form out; a form without {@code seconds} lasts the curse's usual 30.
 */
public record MorphForm(Pool pool, int weight, int seconds) {

    /** {@code Curse.MORPHING}'s after-save time, for forms that don't set their own. */
    public static final int DEFAULT_SECONDS = 30;
    /** Longest a form may set: an hour. */
    public static final int MAX_SECONDS = 3600;

    /** Where you died picks the pool: in lava, underwater (eyes in water), or anywhere else. */
    public enum Pool implements StringRepresentable {
        LAND, WATER, LAVA;

        public static final Codec<Pool> CODEC = StringRepresentable.fromEnum(Pool::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private static final Codec<MorphForm> UNCHECKED = RecordCodecBuilder.create(instance -> instance.group(
        Pool.CODEC.fieldOf("pool").forGetter(MorphForm::pool),
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(MorphForm::weight),
        Codec.INT.optionalFieldOf("seconds", DEFAULT_SECONDS).forGetter(MorphForm::seconds)
    ).apply(instance, MorphForm::new));

    /**
     * 1.20.1's DataFixerUpper turns a bad optional field into its default rather than an error, so
     * {@code seconds} is checked here: out of 1..{@value #MAX_SECONDS} is refused, as on main.
     */
    public static final Codec<MorphForm> CODEC = UNCHECKED.flatXmap(MorphForm::checked, MorphForm::checked);

    private static com.mojang.serialization.DataResult<MorphForm> checked(MorphForm form) {
        return form.seconds >= 1 && form.seconds <= MAX_SECONDS
            ? com.mojang.serialization.DataResult.success(form)
            : com.mojang.serialization.DataResult.error(() -> "seconds out of range [1, " + MAX_SECONDS + "]: " + form.seconds);
    }

    /** How long a morph into this form lasts, in ticks. */
    public int ticks() {
        return seconds * 20;
    }
}
