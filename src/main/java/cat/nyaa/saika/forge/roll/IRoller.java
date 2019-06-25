package cat.nyaa.saika.forge.roll;

import java.util.List;

public interface IRoller<T> {
    T roll(List<T> pool);
}
