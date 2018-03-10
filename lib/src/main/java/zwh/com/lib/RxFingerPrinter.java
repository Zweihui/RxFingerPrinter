package zwh.com.lib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import zwh.com.lib.lifecycle.LifecycleListener;
import zwh.com.lib.lifecycle.SupportFingerPrinterManagerFragment;

import static zwh.com.lib.CodeException.FINGERPRINTERS_FAILED_ERROR;
import static zwh.com.lib.CodeException.HARDWARE_MISSIING_ERROR;
import static zwh.com.lib.CodeException.KEYGUARDSECURE_MISSIING_ERROR;
import static zwh.com.lib.CodeException.NO_FINGERPRINTERS_ENROOLED_ERROR;
import static zwh.com.lib.CodeException.PERMISSION_DENIED_ERROE;
import static zwh.com.lib.CodeException.SYSTEM_API_ERROR;

/**
 * Created by Administrator on 2016/12/31.
 */

public class RxFingerPrinter implements LifecycleListener {
    static final String TAG = "RxFingerPrinter";
    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private Activity context;
    PublishSubject<Boolean> publishSubject;
    SupportFingerPrinterManagerFragment supportFingerPrinterManagerFragment;
    @SuppressLint("NewApi")
    CancellationSignal mCancellationSignal;
    @SuppressLint("NewApi")
    FingerprintManager.AuthenticationCallback authenticationCallback;
    private boolean mLogging;
    private boolean mSelfCompleted;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    public RxFingerPrinter(@NonNull Activity activity) {
        this.context = activity;
        supportFingerPrinterManagerFragment = getRxPermissionsFragment(activity);
    }

    private SupportFingerPrinterManagerFragment getRxPermissionsFragment(Activity activity) {
        SupportFingerPrinterManagerFragment fragment = findRxPermissionsFragment(activity);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new SupportFingerPrinterManagerFragment();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
            fragment.getLifecycle().addListener(this);
        }
        return fragment;
    }

    private SupportFingerPrinterManagerFragment findRxPermissionsFragment(Activity activity) {
        return (SupportFingerPrinterManagerFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

    public PublishSubject<Boolean> begin() {
        dispose();
        publishSubject = PublishSubject.create();
        if (Build.VERSION.SDK_INT < 23) {
            publishSubject.onError(new FPerException(SYSTEM_API_ERROR));
        } else {
            initManager();
            confirmFinger();
            startListening(null);
        }
        return publishSubject;

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            publishSubject.onError(new FPerException(PERMISSION_DENIED_ERROE));
        }
        mCancellationSignal = new CancellationSignal();
        if (manager != null && authenticationCallback != null) {
            mSelfCompleted = false;
            manager.authenticate(cryptoObject, mCancellationSignal, 0, authenticationCallback, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initManager() {
        manager = context.getSystemService(FingerprintManager.class);
        mKeyManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        authenticationCallback = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
                if (mCancellationSignal!=null){
                    publishSubject.onError(new FPerException(FINGERPRINTERS_FAILED_ERROR));
                    mCancellationSignal.cancel();
                    mSelfCompleted = true;
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                publishSubject.onNext(true);
                mSelfCompleted = true;
            }

            @Override
            public void onAuthenticationFailed() {
                publishSubject.onNext(false);
            }
        };
    }

    @SuppressLint("NewApi")
    @TargetApi(23)
    public void confirmFinger() {

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            publishSubject.onError(new FPerException(PERMISSION_DENIED_ERROE));
        }
        //判断硬件是否支持指纹识别
        if (!manager.isHardwareDetected()) {
            publishSubject.onError(new FPerException(HARDWARE_MISSIING_ERROR));
        }
        //判断 是否开启锁屏密码

        if (!mKeyManager.isKeyguardSecure()) {
            publishSubject.onError(new FPerException(KEYGUARDSECURE_MISSIING_ERROR));
        }
        //判断是否有指纹录入
        if (!manager.hasEnrolledFingerprints()) {
            publishSubject.onError(new FPerException(NO_FINGERPRINTERS_ENROOLED_ERROR));
        }

    }

    public void addDispose(Disposable disposable) {
        mDisposables.add(disposable);
    }

    public void dispose() {
        if(mDisposables.isDisposed()){
            mDisposables.dispose();
        }
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onStart() {
        log("LifeCycle--------onStart");
    }

    @Override
    public void onStop() {
        log("LifeCycle--------onStop");
    }

    @Override
    public void onResume() {
        if (!mSelfCompleted){
            startListening(null);
        }
        log("LifeCycle--------onResume");
    }

    @Override
    public void onPause() {
        stopListening();
        log("LifeCycle--------onPause");
    }

    @Override
    public void onDestroy() {
        dispose();
        log("LifeCycle--------onDestroy");
    }

    public void setLogging(boolean logging) {
        mLogging = logging;
    }

    void log(String message) {
        if (mLogging) {
            Log.d(TAG, message);
        }
    }

}
