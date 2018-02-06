package com.zwh.rxfingerprinter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import io.reactivex.observers.DisposableObserver;
import zwh.com.lib.FPerException;
import zwh.com.lib.RxFingerPrinter;

public class MainActivity extends AppCompatActivity {

    private FingerPrinterView fingerPrinterView;
    RxFingerPrinter rxfingerPrinter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fingerPrinterView = (FingerPrinterView) findViewById(R.id.fpv);
        fingerPrinterView.setOnStateChangedListener(new FingerPrinterView.OnStateChangedListener() {
            @Override public void onChange(int state) {
                if (state == FingerPrinterView.STATE_CORRECT_PWD) {
                    Toast.makeText(MainActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
                }
                if (state == FingerPrinterView.STATE_WRONG_PWD) {
                    Toast.makeText(MainActivity.this, "指纹识别失败，请重试",
                            Toast.LENGTH_SHORT).show();
                    fingerPrinterView.setState(FingerPrinterView.STATE_NO_SCANING);
                }
            }
        });
        rxfingerPrinter = new RxFingerPrinter(this);
        rxfingerPrinter.setLogging(BuildConfig.DEBUG);
        findViewById(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisposableObserver<Boolean> observer = new DisposableObserver<Boolean>() {

                    @Override
                    protected void onStart() {
                        if (fingerPrinterView.getState() == FingerPrinterView.STATE_SCANING) {
                            return;
                        } else if (fingerPrinterView.getState() == FingerPrinterView.STATE_CORRECT_PWD
                                || fingerPrinterView.getState() == FingerPrinterView.STATE_WRONG_PWD) {
                            fingerPrinterView.setState(FingerPrinterView.STATE_NO_SCANING);
                        } else {
                            fingerPrinterView.setState(FingerPrinterView.STATE_SCANING);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e instanceof FPerException){
                            Toast.makeText(MainActivity.this,((FPerException) e).getDisplayMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(aBoolean){
                            fingerPrinterView.setState(FingerPrinterView.STATE_CORRECT_PWD);
                        }else{
                            fingerPrinterView.setState(FingerPrinterView.STATE_WRONG_PWD);
                        }
                    }
                };
                rxfingerPrinter.dispose();
                rxfingerPrinter.begin().subscribe(observer);
                rxfingerPrinter.addDispose(observer);
            }
        });
    }
}
