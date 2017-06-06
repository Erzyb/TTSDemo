package aprivate.zyb.com.ttsdemo.tts.util;

import android.content.Context;
import android.media.AudioManager;

import static android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED;

/**
 * Created by zhouyibo on 2017/5/16.
 * 音频焦点管理
 */

public class AudioFocusManager {
    private static AudioManager mAudioFocusManager;

    //获取音频焦点
    public static int requestAudioFocus(Context context,AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener) {
        if (mAudioFocusManager == null)
            mAudioFocusManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
        if (mAudioFocusManager != null) {
            int ret = mAudioFocusManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            return ret;
        }
        return AUDIOFOCUS_REQUEST_FAILED;
    }

    //释放音频焦点
    public static void abandonAudioFocus(AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener) {
        if (mAudioFocusManager != null) {
            mAudioFocusManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            mAudioFocusManager = null;
        }
    }
}
