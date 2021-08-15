package com.welleasern.online.Model;

/**
 * Author: Junbong Jang
 * Date: 5/2/2019
 *
 * Contains all the global state used to trigger functionality related to speaking activity.
 */
public class SpeakingState {
    public static boolean recognition_start_trigger = false;
    public static boolean recognition_stop_trigger = false;
    public static boolean recognition_on = false;
    public static boolean record_myvoice_trigger = false;
    public static boolean stop_record_myvoice_trigger = false;
    public static boolean play_myvoice_trigger = false;
    public static boolean stop_play_myvoice_trigger = false;
    public static boolean respond_to_javascript = false;
    public static boolean send_myvoice_trigger = false;

    public static String save_filename = "";
}
