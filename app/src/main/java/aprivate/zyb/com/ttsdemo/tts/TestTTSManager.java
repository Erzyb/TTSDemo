package aprivate.zyb.com.ttsdemo.tts;

import android.content.Context;

import aprivate.zyb.com.ttsdemo.tts.Base.TTSDataHelper;
import aprivate.zyb.com.ttsdemo.tts.Base.TTSManager;

/**
 * Created by zhouyibo on 2017/6/6.
 */

public class TestTTSManager extends TTSManager {
    private static TestTTSManager sTestTTSManager;
    public TestTTSManager getInstance(Context context) {
        if (sTestTTSManager == null) {
            synchronized (TestTTSManager.class) {
                if (sTestTTSManager == null) {
                    sTestTTSManager = new TestTTSManager(context);
                }
            }
        }
        return sTestTTSManager;
    }

    private TestTTSManager(Context context) {
        super(context);
    }

    @Override
    protected TTSDataHelper getDataHelper(TTSManager manager) {
        return new TestTTSDataHelper(manager);
    }

    @Override
    protected String getAppSocket() {
        return "9542006";
    }

    @Override
    protected String getAppkey() {
        return "dlXpHoTaEA8igYO8wZc8OBQS";
    }

    @Override
    protected String getAppid() {
        return "4f9087c90c53961a0fde2efabd0ac5ac";
    }
}
