package aprivate.zyb.com.ttsdemo.tts.Base;

/**
 * Created by zhouyibo on 2017/5/16.
 */

public interface TTSDownloadHelperImpl {
    boolean checkUse();

    /**
     * 下载模型
     */
    void download(boolean auto);

    /**
     * 获取本地文本模型路径
     *
     * @param type 类型：中文，英文
     * @return 模型路径
     */
    String getTextModelPath(String type);

    /**
     * 获取本地语音模型路径
     *
     * @param type 类型：中文男，中文女，英文男，英文女
     * @return 模型路径
     */
    String getSpeechModelPath(String type);

    void stop();
}
