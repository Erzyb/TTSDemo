package aprivate.zyb.com.ttsdemo.tts.Base;

import android.content.Context;

import com.baidu.tts.client.model.ModelManager;
import com.baidu.tts.client.model.OnDownloadListener;

import java.util.HashMap;
import java.util.Map;

import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.MODEL_ENG_FEMALE;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.MODEL_ENG_MALE;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.MODEL_ZH_FEMALE;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.MODEL_ZH_MALE;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.TTS_EVENT_DOWNLOAD_PROGRESS;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.TTS_EVENT_TOAST;

/**
 * Created by zhouyibo on 2017/5/16.
 * 语音插件下载帮助类
 */

public class TTSDownloadHelper implements TTSDownloadHelperImpl {

    private static Map<String, Integer> data;//下载进度保存
    private static int curProgress = 0;//下载进度
    private static int downloadCount = 4;//总下载任务数
    private TTSManagerImpl mTTSManager;
    private static TTSDownloadHelper mTTSDownloadHelper;
    private static boolean auto = false;//是否下载结束后自动开始语音
    private ModelManager mModelManager;

    public static TTSDownloadHelper getInstance(TTSManagerImpl manager, Context context) {
        if (mTTSDownloadHelper == null) {
            synchronized (TTSDownloadHelper.class){
                if (mTTSDownloadHelper == null){
                    mTTSDownloadHelper = new TTSDownloadHelper(manager,context);
                }
            }
        }
        return mTTSDownloadHelper;
    }

    private TTSDownloadHelper(TTSManagerImpl manager,Context context) {
        mTTSManager = manager;
        mModelManager = new ModelManager(context);
    }

    public static void setAuto(boolean auto) {
        TTSDownloadHelper.auto = auto;//设置是否在下载完成后直接开始运行
    }

    public static Map<String, Integer> getData() {
        return data;//通过data 判断是否已经开始下载
    }

    @Override
    public boolean checkUse() {
        //使用判断模型文件是否可用接口
        return mModelManager.isModelValid(MODEL_ZH_MALE)
                && mModelManager.isModelValid(MODEL_ZH_FEMALE)
                && mModelManager.isModelValid(MODEL_ENG_MALE)
                && mModelManager.isModelValid(MODEL_ENG_FEMALE);
    }

    @Override
    public void download(boolean auto) {
        this.auto = auto;
        if (data == null) {
            data = new HashMap<>();
            data.put(MODEL_ZH_MALE, 0);
            data.put(MODEL_ZH_FEMALE, 0);
            data.put(MODEL_ENG_MALE, 0);
            data.put(MODEL_ENG_FEMALE, 0);
            downloadSingmodel(MODEL_ZH_MALE, MODEL_ZH_FEMALE);
            downloadSingmodel(MODEL_ENG_MALE, MODEL_ENG_FEMALE);
        } else {
            callback(TTS_EVENT_TOAST, "正在下载语音插件");//事件回调，正在下载，下载重复调用
        }
    }

    /**
     * 下载模型，因为中文男声和中文女生有一样的前端模块，所以没有同时下载
     * ，在中文女下载结束时开启中文男的下载，英文模块同理，测试能省去一次前端模块下载
     *
     * @param model1 前置模块(中文女声)
     * @param model2 后置模块(中文男声)
     */
    private void downloadSingmodel(String model1, final String model2) {
        mModelManager.download(model1, new OnDownloadListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onProgress(String s, long l, long l1) {
                mathProgress(s, (int) (l * 100 / l1));
            }

            @Override
            public void onFinish(String s, int i) {
                if (i == -1005 || i == 0) {
                    downloadCount--;
                    mModelManager.download(model2, new OnDownloadListener() {
                        @Override
                        public void onStart(String s) {

                        }

                        @Override
                        public void onProgress(String s, long l, long l1) {
                            mathProgress(s, (int) (l * 100 / l1));
                        }

                        @Override
                        public void onFinish(String s, int i) {
                            if (i == -1005 || i == 0) {
                                downloadCount--;
                                if (downloadCount == 0) {
                                    if (auto) {
                                        callback(TTS_EVENT_DOWNLOAD_PROGRESS, String.valueOf(0));//回调下载进度
                                    }
                                    callback(TTS_EVENT_TOAST, "听书插件下载完成");
                                    if (auto) {
                                        mTTSManager.init();
                                    }
                                    data = null;
                                }
                            } else {
                                stop();
                                callback(TTS_EVENT_TOAST, "听书插件下载失败，请检查网络");
                            }
                        }
                    });
                } else {
                    stop();
                    callback(TTS_EVENT_TOAST, "听书插件下载失败，请检查网络");
                }
            }
        });
    }

    /**
     * 计算总的下载进度，没有具体根据已下载字节数和总字节数做进度，根据文件的大概大小分为权重做进度处理
     * 总共四个模块，前置模块权重为0.4，后置模块权重为0.1，中英文合起来为1
     * map中只保存每个模块的进度，通过权重计算出一个大概进度。
     *
     * @param s        模块id
     * @param progress 当前模块进度
     * @return 通过权重计算出的总的进度
     */
    public void mathProgress(String s, int progress) {
        if (data != null) {
            data.put(s, progress);
            int sum = (int) (data.get(MODEL_ZH_MALE) * 0.4 + data.get(MODEL_ZH_FEMALE) * 0.1
                    + data.get(MODEL_ENG_MALE) * 0.4 + data.get(MODEL_ENG_FEMALE) * 0.1);
            if (curProgress != sum) {
                curProgress = sum;
                if (auto) {
                    callback(TTS_EVENT_DOWNLOAD_PROGRESS, String.valueOf(curProgress));//回调下载进度
                }
            }
        }
    }


    @Override
    public String getTextModelPath(String type) {
        return mModelManager.getTextModelFileAbsPath(type);//获取模块前端路径
    }

    /**
     * 二次封装，根据zh判断是获取中文前端路径还是英文前端路径
     */
    public String getTextModelPath(boolean zh) {
        if (zh) {
            return mModelManager.getTextModelFileAbsPath(MODEL_ZH_MALE);
        } else {
            return mModelManager.getTextModelFileAbsPath(MODEL_ENG_FEMALE);
        }
    }

    @Override
    public String getSpeechModelPath(String type) {
        //使用获取本地后端模型接口
        return mModelManager.getSpeechModelFileAbsPath(type);
    }

    @Override
    public void stop() {
        mModelManager.stop();
        if (auto) {
            callback(TTS_EVENT_DOWNLOAD_PROGRESS, "end");//回调下载进度
        }
        data = null;
    }

    /**
     * 二次封装，根据参数判断获取后端路径  男女  中英
     */
    public String getSpeechModelPath(boolean male, boolean zh) {
        if (zh) {
            if (male) {
                return mModelManager.getSpeechModelFileAbsPath(MODEL_ZH_MALE);
            } else {
                return mModelManager.getSpeechModelFileAbsPath(MODEL_ZH_FEMALE);
            }
        } else {
            if (male) {
                return mModelManager.getSpeechModelFileAbsPath(MODEL_ENG_MALE);
            } else {
                return mModelManager.getSpeechModelFileAbsPath(MODEL_ENG_FEMALE);
            }
        }
    }

    /**
     * mTTSManager回调事件
     */
    private void callback(String type, String value) {
        mTTSManager.receive(type, value);
    }
}
