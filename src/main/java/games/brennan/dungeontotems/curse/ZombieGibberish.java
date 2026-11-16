package games.brennan.dungeontotems.curse;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Random;

/**
 * Zombies can't read: what a Zombified player sees in place of an item's name and every tooltip line.
 * Each line is one to three mumbles (now and then a half-word gets through), and once in
 * {@link #RARE_ODDS} lines the zombie has a thought. The
 * same item always reads the same (seeded by the item), so a tooltip never flickers. Mumbles are lang
 * keys ({@code zombie.dungeontotems.mumble.*}). Display only: it replaces text as it is drawn, never what
 * the item carries, so everything reads again the moment the curse ends.
 */
public final class ZombieGibberish {

    static final List<String> MUMBLES = List.of(
        "brains_question", "brains_bang_question", "brains_trailing", "brains", "mmmmm_brains",
        "huuurrr", "hur", "ahhhhurrrr", "ahur", "brai_ns", "hmmmmm", "ahh_trailing", "ah", "words", "wooooords");
    /** The odd half-word that gets through, each {@link #COMMON_WEIGHT} times rarer than a mumble. */
    public static final List<String> RARE_MUMBLES = List.of(
        "me_read_goods", "me_reads", "words_good", "many_words", "read_read", "word_words", "read",
        "food_question", "food_bang", "friend_question", "zomb", "no", "yes", "eat", "eat_it",
        "bite", "bite_it", "it", "this", "wow");
    static final int COMMON_WEIGHT = 5;
    static final String RARE = "zombie.dungeontotems.mumble.enigma";
    static final int RARE_ODDS = 2000;
    private static final int MAX_MUMBLES = 3;

    private ZombieGibberish() {}

    /** Line {@code index} of an item whose name and components hash to {@code itemSeed}. */
    public static MutableComponent line(int itemSeed, int index) {
        Random random = new Random(itemSeed * 31L + index);
        if (random.nextInt(RARE_ODDS) == 0) return Component.translatable(RARE);
        MutableComponent line = Component.empty();
        int count = 1 + random.nextInt(MAX_MUMBLES);
        for (int i = 0; i < count; i++) {
            if (i > 0) line.append(" ");
            line.append(Component.translatable("zombie.dungeontotems.mumble." + mumble(random)));
        }
        return line;
    }

    /** A mumble, the brains and hurrs {@link #COMMON_WEIGHT} times as likely as each rare one. */
    public static String mumble(Random random) {
        int common = MUMBLES.size() * COMMON_WEIGHT;
        int roll = random.nextInt(common + RARE_MUMBLES.size());
        return roll < common ? MUMBLES.get(roll / COMMON_WEIGHT) : RARE_MUMBLES.get(roll - common);
    }
}
