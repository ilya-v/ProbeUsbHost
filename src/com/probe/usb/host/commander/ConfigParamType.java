package com.probe.usb.host.commander;

public enum ConfigParamType
{  
    acc_odr                              ( 1,  1000,      true,  "Hz"),
    selective_recording_mode             ( 4,  1,         true,  "boolean"),
    streaming_mode                       ( 5,  1,         true,  "boolean"),
    use_bt_with_usb                      ( 6,  1,         true,  "boolean"),
    snooze_detection_time                ( 7,  0xFFFF,    true,  "sec"),
    snooze_threshold_acceleration        ( 8,  3200,      true,  "0.098 m/sec2"),
    activation_threshold_acceleration    ( 9,  3200,      true,  "0.098 m/sec2"),
    sleep_detection_time                 (10,  0xFFFF,    true,  "msec"),
    sleep_threshold_acceleration         (11,  3200,      true,  "0.098 m/sec2"),
    shock_threshold_acceleration         (12,  3200,      true,  "0.098 m/sec2"),
    rest_threshold_acceleration          (13,  3200,      true,  "0.098 m/sec2"),
    rest_detection_time                  (14,  0xFFFF,    true,  "msec"),
    accelerometer_status_reg             (128, 0xFF,      false, "bit field"),
    vcc_voltage                          (129, 0xFFFF,    false, "a.u."),
    vcc_nominal_voltage                  (130, 0xFFFF,    false, "a.u."),
    charging_status                      (131, 1,         false, "boolean");

    public int index;
    public int max;
    public boolean writable;
    public String unit;
    
    ConfigParamType(final int index, final int max, final boolean writable, final String unit) {
       this.index = index;
       this.max = max;
       this.writable = writable;
       this.unit = unit;
    }
}