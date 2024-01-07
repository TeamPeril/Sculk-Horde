package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;

import static com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry.StrategicValues.Infector;
import static com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry.StrategicValues.Melee;
import static com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry.StrategicValues.Ranged;

public class DefaultRaidWavePatterns {

    public final static EntityFactoryEntry.StrategicValues[] FIVE_RANGED_FIVE_MELEE = {Melee, Ranged, Melee, Ranged, Melee, Ranged, Melee, Ranged, Melee, Ranged};

    public final static EntityFactoryEntry.StrategicValues[] FIVE_RANGED_FIVE_INFECTOR = {Infector, Ranged, Infector, Ranged, Infector, Ranged, Infector, Ranged, Infector, Ranged};

    public final static EntityFactoryEntry.StrategicValues[] FIVE_INFECTOR_FIVE_MELEE = {Melee, Infector, Melee, Infector, Melee, Infector, Melee, Infector, Melee, Infector};

    public final static EntityFactoryEntry.StrategicValues[] TEN_MELEE = {Melee, Melee, Melee, Melee, Melee, Melee, Melee, Melee, Melee, Melee};

    public final static EntityFactoryEntry.StrategicValues[] TEN_RANGED = {Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged};
}
