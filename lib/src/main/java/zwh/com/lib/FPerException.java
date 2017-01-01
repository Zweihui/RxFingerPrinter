package zwh.com.lib;

import static zwh.com.lib.CodeException.FINGERPRINTERS_FAILED_ERROR;
import static zwh.com.lib.CodeException.HARDWARE_MISSIING_ERROR;
import static zwh.com.lib.CodeException.KEYGUARDSECURE_MISSIING_ERROR;
import static zwh.com.lib.CodeException.NO_FINGERPRINTERS_ENROOLED_ERROR;
import static zwh.com.lib.CodeException.PERMISSION_DENIED_ERROE;
import static zwh.com.lib.CodeException.SYSTEM_API_ERROR;

/**
 * Created by Administrator on 2016/12/31.
 */

public class FPerException extends RuntimeException {
    /*错误码*/
    private int code;
    /*显示的信息*/
    private String displayMessage;

    public FPerException() {
        super();
    }

    public FPerException(@CodeException.CodeEp int code) {
        super();
        setCode(code);
    }

    @CodeException.CodeEp
    public int getCode() {
        return code;
    }

    public void setCode(@CodeException.CodeEp int code) {
        this.code = code;
    }

    public String getDisplayMessage() {
        switch (code) {
            case SYSTEM_API_ERROR:
                return "系统API小于23";
            case PERMISSION_DENIED_ERROE:
                return "没有指纹识别权限";
            case HARDWARE_MISSIING_ERROR:
                return "没有指纹识别模块";
            case KEYGUARDSECURE_MISSIING_ERROR:
                return "没有开启锁屏密码";
            case NO_FINGERPRINTERS_ENROOLED_ERROR:
                return "没有指纹录入";
            case FINGERPRINTERS_FAILED_ERROR:
                return "指纹认证失败";
            default:
                return "";
        }
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

}
