package de.danoeh.antennapod.ui.screen.subscriptions;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.danoeh.antennapod.model.feed.FeedOrder;
import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.event.UnreadItemsUpdateEvent;
import de.danoeh.antennapod.storage.preferences.UserPreferences;

public class FeedSortDialog {
    public static void showDialog(Context context) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
        dialog.setTitle(context.getString(R.string.pref_nav_drawer_feed_order_title));
        dialog.setNegativeButton(android.R.string.cancel, (d, listener) -> d.dismiss());

        int selected = UserPreferences.getFeedOrder().id;
        List<String> entryValues =
                Arrays.asList(context.getResources().getStringArray(R.array.nav_drawer_feed_order_values));
        final int selectedIndex = entryValues.indexOf("" + selected);

        String[] items = context.getResources().getStringArray(R.array.nav_drawer_feed_order_options);
        dialog.setSingleChoiceItems(items, selectedIndex, (d, which) -> {
            if (selectedIndex != which) {
                UserPreferences.setFeedOrder(FeedOrder.fromOrdinal(Integer.parseInt(entryValues.get(which))));
                //Update subscriptions
                EventBus.getDefault().post(new UnreadItemsUpdateEvent());
            }
            d.dismiss();
        });
        dialog.show();
    }
}
