package org.cgeo.liveview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This receiver updates the cached destination.<br />
 * When it receives a suitable intent it updates the destination (coordinates, description, ...). There are two
 * possibilities to proceed:
 * <ul>
 * <li>The plugin is in use at the LiveView. Then the new destination is used for the next update cycle.</li>
 * <li>The plugin is not in use. Then the new destination will be used for the next update cycle, but the plugin will
 * not be opened at the LiveView.</li>
 * </ul>
 */
public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

    }

}
