package games.brennan.dungeontotems.mixin.client;

import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** A book page in 26.x is a {@link MultiLineEditBox}; {@code ZombieWriting} edits its text and cursor. */
@Mixin(MultiLineEditBox.class)
public interface MultiLineEditBoxAccess {

    @Accessor("textField")
    MultilineTextField dungeontotems$textField();
}
