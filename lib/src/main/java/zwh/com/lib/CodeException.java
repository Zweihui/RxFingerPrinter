package zwh.com.lib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 错误异常码
 */

public class CodeException {
    /*系统API小于23*/
    public static final int SYSTEM_API_ERROR = 0x1;
    /*没有指纹识别权限*/
    public static final int PERMISSION_DENIED_ERROE = 0x2;
    /*没有指纹识别模块*/
    public static final int HARDWARE_MISSIING_ERROR = 0x3;
    /*没有开启锁屏密码*/
    public static final int KEYGUARDSECURE_MISSIING_ERROR = 0x4;
    /*没有指纹录入*/
    public static final int NO_FINGERPRINTERS_ENROOLED_ERROR = 0x5;
    /*连续多次指纹识别失败*/
    public static final int FINGERPRINTERS_FAILED_ERROR = 0x6;


    @IntDef({SYSTEM_API_ERROR, PERMISSION_DENIED_ERROE, HARDWARE_MISSIING_ERROR, KEYGUARDSECURE_MISSIING_ERROR, NO_FINGERPRINTERS_ENROOLED_ERROR,FINGERPRINTERS_FAILED_ERROR})
    @Retention(RetentionPolicy.SOURCE)

    public @interface CodeEp {
    }
}
