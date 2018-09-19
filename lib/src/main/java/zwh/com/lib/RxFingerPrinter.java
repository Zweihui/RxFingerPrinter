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
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import zwh.com.lib.lifecycle.LifecycleListener;
import zwh.com.lib.lifecycle.SupportFingerPrinterManagerFragment;

import static zwh.com.lib.CodeException.FINGERPRINTERS_FAILED_ERROR;
import static zwh.com.lib.CodeException.FINGERPRINTERS_RECOGNIZE_FAILED;
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
    PublishSubject<IdentificationInfo> publishSubject;
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

    public RxFingerPrinter begin() {
        dispose();
        publishSubject = PublishSubject.create();
        return this;
    }

    public void subscribe(DisposableObserver<IdentificationInfo> observer){
        if (observer == null){
            throw new RuntimeException("Observer can not be null!");
        }
        publishSubject.subscribe(observer);
        addDispose(observer);
        if (Build.VERSION.SDK_INT < 23) {
            publishSubject.onNext(new IdentificationInfo(SYSTEM_API_ERROR));
        } else {
            initManager();
            if (confirmFinger()){
                startListening(null);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            publishSubject.onNext(new IdentificationInfo(PERMISSION_DENIED_ERROE));
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
                    publishSubject.onNext(new IdentificationInfo(FINGERPRINTERS_FAILED_ERROR));
                    mCancellationSignal.cancel();
                    mSelfCompleted = true;
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                publishSubject.onNext(new IdentificationInfo(true));
                mSelfCompleted = true;
            }

            @Override
            public void onAuthenticationFailed() {
                publishSubject.onNext(new IdentificationInfo(FINGERPRINTERS_RECOGNIZE_FAILED));
            }
        };
    }

    @SuppressLint("NewApi")
    @TargetApi(23)
    public boolean confirmFinger() {

        boolean isDeviceSupport = true;

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            publishSubject.onNext(new IdentificationInfo(PERMISSION_DENIED_ERROE));
            isDeviceSupport = false;
        }
        //判断硬件是否支持指纹识别
        if (!manager.isHardwareDetected()) {
            publishSubject.onNext(new IdentificationInfo(HARDWARE_MISSIING_ERROR));
            isDeviceSupport = false;
        }
        //判断 是否开启锁屏密码

        if (!mKeyManager.isKeyguardSecure()) {
            publishSubject.onNext(new IdentificationInfo(KEYGUARDSECURE_MISSIING_ERROR));
            isDeviceSupport = false;
        }
        //判断是否有指纹录入
        if (!manager.hasEnrolledFingerprints()) {
            publishSubject.onNext(new IdentificationInfo(NO_FINGERPRINTERS_ENROOLED_ERROR));
            isDeviceSupport = false;
        }

        return isDeviceSupport;
    }

    public void addDispose(Disposable disposable) {
        if(mDisposables != null){
            mDisposables.add(disposable);
        }
    }

    public void dispose() {
        if(mDisposables != null && mDisposables.isDisposed()){
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
