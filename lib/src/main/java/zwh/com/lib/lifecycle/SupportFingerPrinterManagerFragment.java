package zwh.com.lib.lifecycle;

import android.annotation.SuppressLint;
import android.app.Fragment;

/**
 * Created by Administrator on 2018\2\5 0005.
 */

public class SupportFingerPrinterManagerFragment extends Fragment {
    private static final String TAG = "SupportFingerPrinterManagerFragment";
    private final ActivityFragmentLifecycle lifecycle;
    public SupportFingerPrinterManagerFragment() {
        this(new ActivityFragmentLifecycle());
    }

    @SuppressLint("ValidFragment")
    public SupportFingerPrinterManagerFragment(ActivityFragmentLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public ActivityFragmentLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public void onStart() {
        super.onStart();
        lifecycle.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        lifecycle.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        lifecycle.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        lifecycle.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycle.onDestroy();
    }
}
