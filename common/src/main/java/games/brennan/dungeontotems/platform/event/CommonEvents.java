package games.brennan.dungeontotems.platform.event;

import java.util.List;

/**
 * Every class of {@link SubscribeEvent} listeners. A new {@link EventBusSubscriber} class goes here too
 * ({@code ./gradlew build} fails a test otherwise: {@code CommonEventsTest}). Client classes are named, not
 * referenced, so a dedicated server never loads them.
 */
public final class CommonEvents {

    static final List<String> COMMON = List.of(
        "games.brennan.dungeontotems.TotemTriggerEvents",
        "games.brennan.dungeontotems.advancement.TabComplete",
        "games.brennan.dungeontotems.compat.PlayerMobGifts",
        "games.brennan.dungeontotems.curse.Breathless",
        "games.brennan.dungeontotems.curse.CurseMemory",
        "games.brennan.dungeontotems.curse.CurseTimers",
        "games.brennan.dungeontotems.curse.CurseTrials",
        "games.brennan.dungeontotems.curse.DeadlineCurses",
        "games.brennan.dungeontotems.curse.HeldCurses",
        "games.brennan.dungeontotems.curse.InsomniaPhantoms",
        "games.brennan.dungeontotems.curse.LonelyGifts",
        "games.brennan.dungeontotems.curse.RuleCurses",
        "games.brennan.dungeontotems.curse.SavedCurseShield",
        "games.brennan.dungeontotems.curse.VowOfPeace",
        "games.brennan.dungeontotems.curse.ZombieDiet",
        "games.brennan.dungeontotems.curse.ZombieLimits",
        "games.brennan.dungeontotems.curse.ZombiePack",
        "games.brennan.dungeontotems.curse.ZombieTrade",
        "games.brennan.dungeontotems.gametest.GameTestWorld",
        "games.brennan.dungeontotems.morph.FormAir",
        "games.brennan.dungeontotems.morph.FormAllies",
        "games.brennan.dungeontotems.morph.FormAttacks",
        "games.brennan.dungeontotems.morph.FormTimers",
        "games.brennan.dungeontotems.morph.FormTraits",
        "games.brennan.dungeontotems.morph.FormUse",
        "games.brennan.dungeontotems.morph.MorphCommand",
        "games.brennan.dungeontotems.morph.MorphEvents",
        "games.brennan.dungeontotems.morph.MorphFlight",
        "games.brennan.dungeontotems.morph.WaterLife",
        "games.brennan.dungeontotems.voidtotem.VoidRescue"
    );

    static final List<String> CLIENT = List.of(
        "games.brennan.dungeontotems.client.AdvancementSelfTest",
        "games.brennan.dungeontotems.client.CurseTooltips",
        "games.brennan.dungeontotems.client.FormFlop",
        "games.brennan.dungeontotems.client.FormInput",
        "games.brennan.dungeontotems.client.FormSelfTest",
        "games.brennan.dungeontotems.client.MorphRenderer",
        "games.brennan.dungeontotems.client.StackingSelfTest",
        "games.brennan.dungeontotems.client.ZombiePackOverlay",
        "games.brennan.dungeontotems.client.ZombieReading",
        "games.brennan.dungeontotems.client.ZombieSelfTest",
        "games.brennan.dungeontotems.client.ZombieWriting"
    );

    private CommonEvents() {}

    public static void register(boolean client) {
        for (String name : COMMON) TotemEvents.register(load(name), client);
        if (client) for (String name : CLIENT) TotemEvents.register(load(name), true);
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name, true, CommonEvents.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing listener class " + name, e);
        }
    }
}
