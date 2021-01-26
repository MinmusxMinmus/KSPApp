package other;

import java.util.Collection;

// TODO add this in persistencelib?
public interface Storable {
    Collection<String> toStorableCollection();
}
