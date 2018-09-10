package com.zwh.rxfingerprinter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.reactivex.observers.DisposableObserver;
import zwh.com.lib.FPerException;
import zwh.com.lib.IdentificationInfo;
import zwh.com.lib.RxFingerPrinter;

public class MainActivity extends AppCompatActivity {

    private FingerPrinterView fingerPrinterView;
    private RxFingerPrinter rxfingerPrinter;
    private DisposableObserver<IdentificationInfo> observer;
    private Button mBtnOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fingerPrinterView = (FingerPrinterView) findViewById(R.id.fpv);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        fingerPrinterView.setOnStateChangedListener(new FingerPrinterView.OnStateChangedListener() {
            @Override public void onChange(int state) {
                if (state == FingerPrinterView.STATE_WRONG_PWD) {
                    fingerPrinterView.setState(FingerPrinterView.STATE_NO_SCANING);
                }
            }
        });
        rxfingerPrinter = new RxFingerPrinter(this);
        rxfingerPrinter.setLogging(BuildConfig.DEBUG);
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                createObserver();
                rxfingerPrinter.begin().subscribe(observer);
            }
        });
    }

    private void createObserver(){
        observer = new DisposableObserver<IdentificationInfo>() {
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
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onNext(IdentificationInfo info) {
                        if(info.isSuccessful()){
                            fingerPrinterView.setState(FingerPrinterView.STATE_CORRECT_PWD);
                            Toast.makeText(MainActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
                            enableButton(true);
                        }else{
                            FPerException exception = info.getException();
                            if (exception != null){
                                Toast.makeText(MainActivity.this,exception.getDisplayMessage(),Toast.LENGTH_SHORT).show();
                            }
                            fingerPrinterView.setState(FingerPrinterView.STATE_WRONG_PWD);
                        }
                    }
                };
    }

    private void enableButton(boolean enable){
        mBtnOpen.setClickable(enable);
        mBtnOpen.setEnabled(enable);
    }
}
