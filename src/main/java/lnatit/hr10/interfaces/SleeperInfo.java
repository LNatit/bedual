package lnatit.hr10.interfaces;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import javax.swing.text.html.parser.Entity;
import java.util.function.Consumer;

public class SleeperInfo
{
    public class DuallableSleeper
    {
        private Entity[] sleeper = {null, null};
        private final boolean canDual;

        public DuallableSleeper(Entity sleeper)
        {
            this.sleeper[0] = sleeper;
            this.canDual = false;
        }

        public DuallableSleeper(Entity sleeper, boolean canDual)
        {
            this.sleeper[0] = sleeper;
            this.canDual = canDual;
        }

        public DuallableSleeper(Entity sleeper, @Nonnull SleepSide side)
        {
            if (side == SleepSide.LEFT)
            this.sleeper[0] = sleeper;
            else this.sleeper[1] = sleeper;
            this.canDual = true;
        }

        public boolean dualWith(Entity sleeper)
        {
            if (this.canDual)
            {
                this.sleeper[1] = sleeper;
                return true;
            }
            else return false;
        }

        public void sleeperExecute(Consumer<Entity> function)
        {
            for (Entity sleeperEntity : this.sleeper)
                function.accept(sleeperEntity);
        }
    }

    public enum SleepSide implements IStringSerializable
    {
        LEFT("ss_left"),    //0
        RIGHT("ss_right");  //1

        private final String name;

        SleepSide(String name)
        {
            this.name = name;
        }

        @Override
        public String getString()
        {
            return this.name;
        }
    }
}
