package aprivate.zyb.com.ttsdemo.tts.Base;

/**
 * Created by zhouyibo on 2017/5/16.
 */

public interface TTSManagerImpl {
    void checkUse();

    void setHost(TTSHost host);

    void download();

    void init();

    boolean isDoing();

    void onStart();

    void onRestrat();

    void onPause();

    void onResume();

    void onStop();

    void onDestory();

    void receiveData(int position, Object data);

    void addNewData(Object data);

    void receive(String type, String value);

    void addSpeck(String content, String tag);

    void speakStart(String tag);

    void speakEnd(String tag);
}
