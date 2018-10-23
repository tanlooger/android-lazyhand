package lazyhand.com.main.utils;

import lazyhand.com.main.controller.DevicesController;

public class StaticMethod {

    static private DevicesController devicesController;
    static public DevicesController getDevicesController(){
        //DevicesController devicesController = ViewModelProviders.of(Application.ACTIVITY_SERVICE).get(DevicesController.class);
        return devicesController;
    }
}
