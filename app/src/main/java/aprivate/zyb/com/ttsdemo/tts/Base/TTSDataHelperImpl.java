package aprivate.zyb.com.ttsdemo.tts.Base;

/**
 * Created by zhouyibo on 2017/5/16.
 */

public interface TTSDataHelperImpl {
    void loadData(int position, Object data);

    void addData(Object data);

    void addSpeck(String text, String tag);

    void removeSpeck(String tag);

    void readEnd();
}
