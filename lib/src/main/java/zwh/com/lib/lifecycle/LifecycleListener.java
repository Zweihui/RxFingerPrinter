package zwh.com.lib.lifecycle;

/**
 * Created by Administrator on 2018\2\5 0005.
 */

public interface LifecycleListener {

    /**
     * Callback for when {@link android.app.Fragment#onStart()}} or {@link
     * android.app.Activity#onStart()} is called.
     */
    void onStart();

    /**
     * Callback for when {@link android.app.Fragment#onStop()}} or {@link
     * android.app.Activity#onStop()}} is called.
     */
    void onStop();
    /**
     * Callback for when {@link android.app.Fragment#onResume()}} or {@link
     * android.app.Activity#onResume()} is called.
     */
    void onResume();

    /**
     * Callback for when {@link android.app.Fragment#onPause()}} or {@link
     * android.app.Activity#onPause()}} is called.
     */
    void onPause();

    /**
     * Callback for when {@link android.app.Fragment#onDestroy()}} or {@link
     * android.app.Activity#onDestroy()} is called.
     */
    void onDestroy();
}