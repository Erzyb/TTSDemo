package aprivate.zyb.com.ttsdemo.tts.Base;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;

/**
 * Created by zhouyibo on 2017/5/16.
 *
 */

public class SpeechSynListener implements SpeechSynthesizerListener {
    private TTSManager mTTSManager;

    public SpeechSynListener(TTSManager TTSManager) {
        mTTSManager = TTSManager;
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {
        mTTSManager.speakStart(s);
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        mTTSManager.speakEnd(s);
    }

    @Override
    public void onError(String s, SpeechError speechError) {

    }
}
