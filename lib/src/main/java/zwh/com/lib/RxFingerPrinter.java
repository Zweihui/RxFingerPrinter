package zwh.com.lib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telecom.Call;
import android.widget.Toast;

import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static android.icu.text.RelativeDateTimeFormatter.Direction.THIS;
import static zwh.com.lib.CodeException.FINGERPRINTERS_FAILED_ERROR;
import static zwh.com.lib.CodeException.HARDWARE_MISSIING_ERROR;
import static zwh.com.lib.CodeException.KEYGUARDSECURE_MISSIING_ERROR;
import static zwh.com.lib.CodeException.NO_FINGERPRINTERS_ENROOLED_ERROR;
import static zwh.com.lib.CodeException.PERMISSION_DENIED_ERROE;
import static zwh.com.lib.CodeException.SYSTEM_API_ERROR;

/**
 * Created by Administrator on 2016/12/31.
 */

public class RxFingerPrinter {
    static final String TAG = "RxFingerPrinter";
    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private Context context;
    private HashMap<String, CompositeSubscription> mSubscriptionMap;
    PublishSubject<Boolean> publishSubject;
    @SuppressLint("NewApi")
    CancellationSignal mCancellationSignal;
    @SuppressLint("NewApi")
    FingerprintManager.AuthenticationCallback mSelfCancelled;

    public RxFingerPrinter(@NonNull Context context) {
        this.context = context;
    }

    public PublishSubject<Boolean> begin() {

        if(publishSubject == null){
            publishSubject = PublishSubject.create();
        }
        if (Build.VERSION.SDK_INT < 23){
            publishSubject.onError(new FPerException(SYSTEM_API_ERROR));
        }else {
            initManager();
            confirmFinger();
            startListening(null);
        }
        return publishSubject;

    }
    @TargetApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            throw new FPerException(PERMISSION_DENIED_ERROE);
        }
        manager.authenticate(cryptoObject, null, 0, mSelfCancelled, null);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initManager() {
        mCancellationSignal = new CancellationSignal();
        manager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        mKeyManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
                publishSubject.onError(new FPerException(FINGERPRINTERS_FAILED_ERROR));
                mCancellationSignal.cancel();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                publishSubject.onNext(true);

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

    public Observable.Transformer<Object, Boolean> ensure() {
        return new Observable.Transformer<Object, Boolean>() {

            @Override
            public Observable<Boolean> call(Observable<Object> o) {
                return null;
            }
        };
    }


    /**
     * 保存订阅后的subscription
     * @param o
     * @param subscription
     */
    public void addSubscription(Object o, Subscription subscription) {
        if (mSubscriptionMap == null) {
            mSubscriptionMap = new HashMap<>();
        }
        String key = o.getClass().getName();
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).add(subscription);
        } else {
            CompositeSubscription compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(subscription);
            mSubscriptionMap.put(key, compositeSubscription);
        }
    }


    /**
     * 取消订阅
     * @param o
     */
    public void unSubscribe(Object o) {
        if (mSubscriptionMap == null) {
            return;
        }

        String key = o.getClass().getName();
        if (!mSubscriptionMap.containsKey(key)){
            return;
        }
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).unsubscribe();
        }

        mSubscriptionMap.remove(key);
    }

}
