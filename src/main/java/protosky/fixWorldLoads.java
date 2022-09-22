package protosky;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import protosky.stuctures.StrongHoldHelper;
import protosky.stuctures.endCityHelper;

public class fixWorldLoads implements ModInitializer {
    @Override
    public void onInitialize() {
        //System.out.println("\nhello!\n");
        CommonLifecycleEvents.TAGS_LOADED.register((arg1, arg2) -> {
            //System.out.println("\nran!\n");
            StrongHoldHelper.ran = false;
            endCityHelper.ran = false;
        });
    }
}
