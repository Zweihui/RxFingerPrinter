package zwh.com.lib;

/**
 * Created by Zhangwh on 2018/9/7.
 * 指纹识别结束返回信息
 */

public class IdentificationInfo {
    private FPerException mException;
    private boolean isSuccessful;

    public IdentificationInfo(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public IdentificationInfo(int exceptionCode) {
        mException = new FPerException(exceptionCode);
        this.isSuccessful = false;
    }

    public FPerException getException() {
        return mException;
    }

    public void setException(FPerException exception) {
        mException = exception;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }
}
