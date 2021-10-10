package net.gyroinc.dhbwhexacopter.models

class DroneNavStatus(
    val gps_mode: Int,
    val nav_state: Int,
    val action: Int,
    val wp_number: Int,
    val nav_error: Int,
    val target_bearing: Int
) {
    companion object {
        const val GPS_MODE_NONE = 0
        const val GPS_MODE_HOLD = 1
        const val GPS_MODE_RTH = 2
        const val GPS_MODE_NAV = 3
        const val GPS_MODE_EMERG = 15

        const val NAV_STATE_NONE = 0
        const val NAV_STATE_RTH_START = 1
        const val NAV_STATE_RTH_ENROUTE = 2
        const val NAV_STATE_HOLD_INFINIT = 3
        const val NAV_STATE_HOLD_TIMED = 4
        const val NAV_STATE_WP_ENROUTE = 5
        const val NAV_STATE_PROCESS_NEXT = 6
        const val NAV_STATE_DO_JUMP = 7
        const val NAV_STATE_LAND_START = 8
        const val NAV_STATE_LAND_IN_PROGRESS = 9
        const val NAV_STATE_LANDED = 10
        const val NAV_STATE_LAND_SETTLE = 11
        const val NAV_STATE_LAND_START_DESCENT = 12
        const val NAV_STATE_HOVER_ABOVE_HOME = 13
        const val NAV_STATE_EMERGENCY_LANDING = 14
        const val NAV_STATE_RTH_CLIMB = 15

        const val NAV_ERROR_NONE = 0                //All systems clear
        const val NAV_ERROR_TOOFAR = 1              //Next waypoint distance is more than safety distance
        const val NAV_ERROR_SPOILED_GPS = 2         //GPS reception is compromised - Nav paused - copter is adrift !
        const val NAV_ERROR_WP_CRC = 3              //CRC error reading WP data from EEPROM - Nav stopped
        const val NAV_ERROR_FINISH = 4              //End flag detected, navigation finished
        const val NAV_ERROR_TIMEWAIT = 5            //Waiting for poshold timer
        const val NAV_ERROR_INVALID_JUMP = 6        //Invalid jump target detected, aborting
        const val NAV_ERROR_INVALID_DATA = 7        //Invalid mission step action code, aborting, copter is adrift
        const val NAV_ERROR_WAIT_FOR_RTH_ALT = 8    //Waiting to reach RTH Altitude
        const val NAV_ERROR_GPS_FIX_LOST = 9        //Gps fix lost, aborting mission
        const val NAV_ERROR_DISARMED = 10           //NAV engine disabled due disarm
        const val NAV_ERROR_LANDING = 11            //Landing
    }
}