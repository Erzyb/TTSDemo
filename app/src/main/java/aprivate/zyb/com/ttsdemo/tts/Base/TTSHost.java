package aprivate.zyb.com.ttsdemo.tts.Base;

/**
 * Created by zhouyibo on 2017/5/16.
 */

public interface TTSHost {
    void download(String progress);

    void toast(String toast);

    void loading(boolean show);

    void ttsStart();

    void ttsPause();

    void ttsResume();

    void ttsEnd();

    void getTag(String sign);

    void remainTime(long millisUntilFinished);
}
