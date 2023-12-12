package com.github.sculkhorde.core.gravemind;

import static com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory.StrategicValues.Infector;
import static com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory.StrategicValues.Melee;
import static com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory.StrategicValues.Ranged;

import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;

public class DefaultRaidWavePatterns {

    public final static EntityFactory.StrategicValues[] FIVE_RANGED_FIVE_MELEE = {Melee, Ranged, Melee, Ranged, Melee, Ranged, Melee, Ranged, Melee, Ranged};

    public final static EntityFactory.StrategicValues[] FIVE_RANGED_FIVE_INFECTOR = {Infector, Ranged, Infector, Ranged, Infector, Ranged, Infector, Ranged, Infector, Ranged};

    public final static EntityFactory.StrategicValues[] FIVE_INFECTOR_FIVE_MELEE = {Melee, Infector, Melee, Infector, Melee, Infector, Melee, Infector, Melee, Infector};

    public final static EntityFactory.StrategicValues[] TEN_MELEE = {Melee, Melee, Melee, Melee, Melee, Melee, Melee, Melee, Melee, Melee};

    public final static EntityFactory.StrategicValues[] TEN_RANGED = {Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged, Ranged};
}
