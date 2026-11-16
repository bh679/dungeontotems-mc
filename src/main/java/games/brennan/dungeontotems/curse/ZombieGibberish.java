package games.brennan.dungeontotems.curse;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

/**
 * Zombies can't read: what a Zombified player sees in place of an item's name and every tooltip line.
 * Each line is one to three mumbles (now and then a half-word gets through). One item in
 * {@link #THOUGHT_ODDS} gives the zombie a lucid moment: its name reads as one of the {@link #THOUGHTS}.
 * The same item always reads the same (seeded by the item), so a tooltip never flickers. Mumbles are lang
 * keys ({@code zombie.dungeontotems.mumble.*}). Display only: it replaces text as it is drawn, never what
 * the item carries, so everything reads again the moment the curse ends.
 */
public final class ZombieGibberish {

    static final List<String> MUMBLES = List.of(
        "brains_question", "brains_bang_question", "brains_trailing", "brains", "mmmmm_brains",
        "huuurrr", "hur", "ahhhhurrrr", "ahur", "brai_ns", "hmmmmm", "ahh_trailing", "ah", "words", "wooooords",
        "mmmm", "mmm_trailing", "mmmmmmmm");
    /** The odd half-word that gets through, each {@link #COMMON_WEIGHT} times rarer than a mumble. */
    public static final List<String> RARE_MUMBLES = List.of(
        "me_read_goods", "me_reads", "words_good", "many_words", "read_read", "word_words", "read",
        "food_question", "food_bang", "friend_question", "zomb", "no", "yes", "eat", "eat_it",
        "bite", "bite_it", "it", "this", "wow", "shrimp");
    static final int COMMON_WEIGHT = 5;
    /** Lang keys {@code zombie.dungeontotems.thought.*}. */
    public static final List<String> THOUGHTS = List.of(
        "enigma", "sorry_jest", "rotted_vessel", "cruel_fortune", "hunger_riddle", "mind_entombed",
        "letters_swim", "piece_of_work", "ruin_philosopher", "to_read", "horror_by_candlelight");
    public static final int THOUGHT_ODDS = 500;
    /** The line index whose seed rolls the item's thought, apart from every real line's. */
    private static final int THOUGHT_ROLL = -1;
    private static final int MAX_MUMBLES = 3;
    private static final int MIN_PAGE_MUMBLES = 3;
    private static final int PARAGRAPH_ODDS = 12;

    private ZombieGibberish() {}

    /** Line {@code index} of an item whose name and components hash to {@code itemSeed}. */
    public static MutableComponent line(int itemSeed, int index) {
        if (index == 0) {
            String thought = thought(itemSeed);
            if (thought != null) return Component.translatable("zombie.dungeontotems.thought." + thought);
        }
        Random random = new Random(itemSeed * 31L + index);
        MutableComponent line = Component.empty();
        int count = 1 + random.nextInt(MAX_MUMBLES);
        for (int i = 0; i < count; i++) {
            if (i > 0) line.append(" ");
            line.append(Component.translatable("zombie.dungeontotems.mumble." + mumble(random)));
        }
        return line;
    }

    /**
     * A book page as a zombie reads it: about as many mumbles as the page has words, now and then a new
     * paragraph, the same every time for the same page.
     */
    public static MutableComponent page(int pageSeed, int words) {
        Random random = new Random(pageSeed);
        MutableComponent page = Component.empty();
        for (int i = 0; i < Math.max(MIN_PAGE_MUMBLES, words); i++) {
            if (i > 0) page.append(random.nextInt(PARAGRAPH_ODDS) == 0 ? "\n\n" : " ");
            page.append(Component.translatable("zombie.dungeontotems.mumble." + mumble(random)));
        }
        return page;
    }

    /** Text after a zombie's edit, with where the cursor now sits. */
    public record Edit(String text, int cursor) {}

    /** One word in {@code WORD_THROUGH_ODDS} a zombie writes comes through as typed. */
    public static final int WORD_THROUGH_ODDS = 6;

    /**
     * The word just before {@code cursor} as a zombie writes it: a mumble ({@code mumbleText} turns a
     * mumble's lang key into text), or, one time in {@link #WORD_THROUGH_ODDS}, the word itself.
     */
    public static Edit writeWord(String text, int cursor, Random random, UnaryOperator<String> mumbleText) {
        int start = cursor;
        while (start > 0 && !Character.isWhitespace(text.charAt(start - 1))) start--;
        if (start == cursor || random.nextInt(WORD_THROUGH_ODDS) == 0) return new Edit(text, cursor);
        String mumble = mumbleText.apply("zombie.dungeontotems.mumble." + mumble(random));
        return new Edit(text.substring(0, start) + mumble + text.substring(cursor), start + mumble.length());
    }

    /** The thought this item stirs, or null for the {@code THOUGHT_ODDS - 1} items in {@code THOUGHT_ODDS} that stir none. */
    @Nullable
    public static String thought(int itemSeed) {
        Random random = new Random(itemSeed * 31L + THOUGHT_ROLL);
        return random.nextInt(THOUGHT_ODDS) == 0 ? THOUGHTS.get(random.nextInt(THOUGHTS.size())) : null;
    }

    /** A mumble, the brains and hurrs {@link #COMMON_WEIGHT} times as likely as each rare one. */
    public static String mumble(Random random) {
        int common = MUMBLES.size() * COMMON_WEIGHT;
        int roll = random.nextInt(common + RARE_MUMBLES.size());
        return roll < common ? MUMBLES.get(roll / COMMON_WEIGHT) : RARE_MUMBLES.get(roll - common);
    }
}
