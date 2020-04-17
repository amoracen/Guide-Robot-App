package fau.amoracen.guiderobot.service;

import android.content.Context;
import android.view.accessibility.AccessibilityManager;

public class MyAccessibilityEvent {
    private static MyAccessibilityEvent instance;


    public synchronized static MyAccessibilityEvent getInstance() {
        if (instance == null) {
            instance = new MyAccessibilityEvent();
        }
        return instance;
    }

    /**
     * Send an accessibility event
     *
     * @param msg a string
     */
    public void sendAccessibilityEvent(Context context, final String msg) {
        try {
            AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (manager != null && manager.isEnabled()) {
                android.view.accessibility.AccessibilityEvent e = android.view.accessibility.AccessibilityEvent.obtain();
                e.setEventType(android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT);
                e.setClassName(getClass().getName());
                e.setPackageName(context.getPackageName());
                e.getText().add(msg);
                manager.sendAccessibilityEvent(e);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
