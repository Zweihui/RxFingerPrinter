package com.zwh.rxfingerprinter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import rx.Subscriber;
import rx.Subscription;
import zwh.com.lib.FPerException;
import zwh.com.lib.RxFingerPrinter;

public class MainActivity extends AppCompatActivity {

    private FingerPrinterView fingerPrinterView;
    private int fingerErrorNum = 0; // 指纹错误次数
    RxFingerPrinter rxfingerPrinter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fingerPrinterView = (FingerPrinterView) findViewById(R.id.fpv);
        fingerPrinterView.setOnStateChangedListener(new FingerPrinterView.OnStateChangedListener() {
            @Override public void onChange(int state) {
                if (state == FingerPrinterView.STATE_CORRECT_PWD) {
                    fingerErrorNum = 0;
                    Toast.makeText(MainActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
                }
                if (state == FingerPrinterView.STATE_WRONG_PWD) {
                    Toast.makeText(MainActivity.this, "指纹识别失败，还剩" + (5-fingerErrorNum) + "次机会",
                            Toast.LENGTH_SHORT).show();
                    fingerPrinterView.setState(FingerPrinterView.STATE_NO_SCANING);
                }
            }
        });
        rxfingerPrinter = new RxFingerPrinter(this);
        findViewById(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fingerErrorNum = 0;
                rxfingerPrinter.unSubscribe(this);
                Subscription subscription = rxfingerPrinter.begin().subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onStart() {
                        super.onStart();
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
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e instanceof FPerException){
                            Toast.makeText(MainActivity.this,((FPerException) e).getDisplayMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(aBoolean){
                            fingerPrinterView.setState(FingerPrinterView.STATE_CORRECT_PWD);
                        }else{
                            fingerErrorNum++;
                            fingerPrinterView.setState(FingerPrinterView.STATE_WRONG_PWD);
                        }
                    }
                });
                rxfingerPrinter.addSubscription(this,subscription);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rxfingerPrinter.unSubscribe(this);
    }
}
