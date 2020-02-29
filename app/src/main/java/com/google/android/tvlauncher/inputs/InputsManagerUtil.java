package com.google.android.tvlauncher.inputs;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.AppUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InputsManagerUtil {
    private static final String ACTION_VIEW_INPUTS = "com.android.tv.action.VIEW_INPUTS";
    static final String BUNDLED_TUNER_ID = "com.google.android.tvlauncher.input.bundled_tuner";
    static final boolean HIDE_HOME_INPUT = true;
    static final String HOME_INPUT_ID = "com.google.android.tvlauncher.input.home";
    private static final String INPUT_TYPE_BUNDLED_TUNER = "input_type_combined_tuners";
    private static final String INPUT_TYPE_CEC_AUDIO_SYSTEM = "input_type_cec_audio_system";
    private static final String INPUT_TYPE_CEC_DEVICE_TV = "input_type_cec_device_tv";
    private static final String INPUT_TYPE_CEC_LOGICAL = "input_type_cec_logical";
    private static final String INPUT_TYPE_CEC_PLAYBACK = "input_type_cec_playback";
    private static final String INPUT_TYPE_CEC_RECORDER = "input_type_cec_recorder";
    private static final String INPUT_TYPE_CEC_TUNER = "input_type_cec_tuner";
    private static final String INPUT_TYPE_COMPONENT = "input_type_component";
    private static final String INPUT_TYPE_COMPOSITE = "input_type_composite";
    private static final String INPUT_TYPE_DISPLAY_PORT = "input_type_displayport";
    private static final String INPUT_TYPE_DVI = "input_type_dvi";
    private static final String INPUT_TYPE_HDMI = "input_type_hdmi";
    private static final String INPUT_TYPE_MHL_MOBILE = "input_type_mhl_mobile";
    private static final String INPUT_TYPE_OTHER = "input_type_other";
    private static final String INPUT_TYPE_SCART = "input_type_scart";
    private static final String INPUT_TYPE_SVIDEO = "input_type_svideo";
    private static final String INPUT_TYPE_TUNER = "input_type_tuner";
    private static final String INPUT_TYPE_VGA = "input_type_vga";
    private static final String TAG = "InputsManagerUtil";
    public static final int TYPE_BUNDLED_TUNER = -3;
    public static final int TYPE_CEC_AUDIO_SYSTEM = -9;
    public static final int TYPE_CEC_DEVICE = -2;
    public static final int TYPE_CEC_DEVICE_PLAYBACK = -5;
    public static final int TYPE_CEC_DEVICE_RECORDER = -4;
    public static final int TYPE_CEC_DEVICE_TV = -8;
    public static final int TYPE_CEC_TUNER = -10;
    public static final int TYPE_HOME = -7;
    public static final int TYPE_MHL_MOBILE = -6;
    private static final Map<String, Integer> descriptionTypeMap = new LinkedHashMap(18);
    private static final Set<Integer> hdmiDeviceTypes = new HashSet(7);
    private static final Map<Integer, Integer> typeIconResourceIdMap = new HashMap(18);

    static {
        descriptionTypeMap.put(INPUT_TYPE_BUNDLED_TUNER, -3);
        descriptionTypeMap.put(INPUT_TYPE_TUNER, 0);
        descriptionTypeMap.put(INPUT_TYPE_CEC_DEVICE_TV, -8);
        descriptionTypeMap.put(INPUT_TYPE_CEC_RECORDER, -4);
        descriptionTypeMap.put(INPUT_TYPE_CEC_TUNER, -10);
        descriptionTypeMap.put(INPUT_TYPE_CEC_PLAYBACK, -5);
        descriptionTypeMap.put(INPUT_TYPE_CEC_AUDIO_SYSTEM, -9);
        descriptionTypeMap.put(INPUT_TYPE_CEC_LOGICAL, -2);
        descriptionTypeMap.put(INPUT_TYPE_MHL_MOBILE, -6);
        descriptionTypeMap.put(INPUT_TYPE_HDMI, 1007);
        descriptionTypeMap.put(INPUT_TYPE_DVI, 1006);
        descriptionTypeMap.put(INPUT_TYPE_COMPONENT, 1004);
        descriptionTypeMap.put(INPUT_TYPE_SVIDEO, 1002);
        descriptionTypeMap.put(INPUT_TYPE_COMPOSITE, 1001);
        descriptionTypeMap.put(INPUT_TYPE_DISPLAY_PORT, 1008);
        descriptionTypeMap.put(INPUT_TYPE_VGA, 1005);
        descriptionTypeMap.put(INPUT_TYPE_SCART, 1003);
        descriptionTypeMap.put(INPUT_TYPE_OTHER, 1000);
        hdmiDeviceTypes.add(-8);
        hdmiDeviceTypes.add(-4);
        hdmiDeviceTypes.add(-10);
        hdmiDeviceTypes.add(-5);
        hdmiDeviceTypes.add(-9);
        hdmiDeviceTypes.add(-2);
        hdmiDeviceTypes.add(-6);
        typeIconResourceIdMap.put(-4, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_recording));
        typeIconResourceIdMap.put(-5, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_playback));
        typeIconResourceIdMap.put(-2, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_tuner));
        typeIconResourceIdMap.put(-8, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_livetv));
        typeIconResourceIdMap.put(-9, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_audio));
        typeIconResourceIdMap.put(-10, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_tuner));
        typeIconResourceIdMap.put(-6, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_mhl));
        typeIconResourceIdMap.put(1007, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_hdmi));
        typeIconResourceIdMap.put(-7, Integer.valueOf(C1167R.C1168drawable.ic_home_input_black_24dp));
        typeIconResourceIdMap.put(-3, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_tuner));
        typeIconResourceIdMap.put(0, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_tuner));
        typeIconResourceIdMap.put(1006, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_dvi));
        typeIconResourceIdMap.put(1004, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_component));
        typeIconResourceIdMap.put(1002, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_svideo));
        typeIconResourceIdMap.put(1001, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_composite));
        typeIconResourceIdMap.put(1008, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_display_port));
        typeIconResourceIdMap.put(1005, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_vga));
        typeIconResourceIdMap.put(1003, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_scart));
        typeIconResourceIdMap.put(1000, Integer.valueOf(C1167R.C1168drawable.ic_icon_32dp_hdmi));
    }

    public static InputsManager getInputsManager(Context context) {
        if (OemConfiguration.get(context).shouldUseCustomInputList()) {
            return CustomInputsManager.getInstance(context.getApplicationContext());
        }
        return TifInputsManager.getInstance(context.getApplicationContext());
    }

    public static void launchInputsActivity(Context context) {
        Intent intent = new Intent(ACTION_VIEW_INPUTS);
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(intent, 131072);
        intent.setPackage(context.getPackageName());
        if (activities.size() > 1) {
            Iterator<ResolveInfo> it = activities.iterator();
            while (true) {
                if (it.hasNext()) {
                    ResolveInfo activity = it.next();
                    if (activity.activityInfo != null && !context.getPackageName().equals(activity.activityInfo.packageName) && AppUtil.isSystemApp(activity)) {
                        intent.setPackage(activity.activityInfo.packageName);
                        break;
                    }
                }
            }
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Inputs activity not found", e);
        }
    }

    static Map<Integer, Integer> getInputsOrderMap(List<String> topPriorityInputs) {
        Map<Integer, Integer> map = new HashMap<>(descriptionTypeMap.size());
        map.put(-7, 0);
        int priority = 1;
        for (String input : topPriorityInputs) {
            Integer type = descriptionTypeMap.get(input);
            if (type != null && !hdmiDeviceTypes.contains(type)) {
                map.put(type, Integer.valueOf(priority));
                priority++;
            }
        }
        for (Map.Entry<String, Integer> entry : descriptionTypeMap.entrySet()) {
            Integer inputType = (Integer) entry.getValue();
            if (!map.containsKey(inputType) && !hdmiDeviceTypes.contains(inputType)) {
                map.put(inputType, Integer.valueOf(priority));
                priority++;
            }
        }
        int hdmiPriority = ((Integer) map.get(1007)).intValue();
        for (Integer intValue : hdmiDeviceTypes) {
            map.put(Integer.valueOf(intValue.intValue()), Integer.valueOf(hdmiPriority));
        }
        return map;
    }

    static Integer getIconResourceId(int type) {
        return typeIconResourceIdMap.get(Integer.valueOf(type));
    }

    static Integer getType(String inputDescription) {
        return descriptionTypeMap.get(inputDescription);
    }
}
