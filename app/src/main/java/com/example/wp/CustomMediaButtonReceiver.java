package com.example.wp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CustomMediaButtonReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("CustomMediaButtonReceiver.onReceive  " + intent);
    }
}