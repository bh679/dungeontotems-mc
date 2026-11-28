package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** An animal about to be tamed by a player. Cancel to refuse. */
public final class AnimalTameEvent extends LivingEvent {
    private final Mob animal;
    private final Player tamer;

    public AnimalTameEvent(Mob animal, Player tamer) {
        super(animal);
        this.animal = animal;
        this.tamer = tamer;
    }

    public Mob getAnimal() { return animal; }
    public Player getTamer() { return tamer; }
}
