package aprivate.zyb.com.ttsdemo.tts.Base;

import android.content.Context;
import android.media.AudioManager;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

import aprivate.zyb.com.ttsdemo.tts.util.AudioFocusManager;
import aprivate.zyb.com.ttsdemo.tts.util.NetWorkUtil;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.TTS_EVENT_DOWNLOAD_PROGRESS;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.TTS_EVENT_TIMER_END;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.TTS_EVENT_TIMER_PROGRESS;
import static aprivate.zyb.com.ttsdemo.tts.Base.TTSDataManager.TTS_EVENT_TOAST;

/**
 * Created by zhouyibo on 2017/5/16.
 * 语音听书模块管理类
 * 功能：音频播放控制，数据处理，插件检测下载，宿主类反馈，音频焦点控制
 * 持有 数据处理帮助类，插件下载帮助类，播放回调接口，宿主对象，计时器，是整个听书模块的中枢。
 */

public abstract class TTSManager implements TTSManagerImpl {

    private static boolean isDoing = false;//是否正在运行
    private Context mContext;
    private boolean isMale = false;//语音合成参数，性别，默认true为男性
    private String speed = "5";//语音合成参数，速度，默认速度为5  【0，9】
    private boolean paramsChange = false;//参数是否有改变，控制继续播放时流程，继续or重新合成
    private long millisInFuture = 0;//计时器总计时
    private SpeechSynthesizer mSpeechSynthesizer;//语音合成播放进度回调接口
    private TTSHost host;//宿主对象
    private TTSDownloadHelper mTTSDownloadHelper;//插件下载帮助类
    private TTSDataHelper mTTSDataHelper;//数据处理帮助类
    private SpeechSynListener mSpeechSynListener;

    private TTSTimer mTTSTimer = new TTSTimer() {//计时器势力
        @Override
        public void onTick(long millisUntilFinished) {//剩余时间回调
            receive(TTS_EVENT_TIMER_PROGRESS, String.valueOf(millisUntilFinished));
        }

        @Override
        public void onFinish() {//计时器结束回调
            receive(TTS_EVENT_TIMER_END, "");
        }
    };
    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//音频焦点控制
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AUDIOFOCUS_GAIN://获取音频焦点
                    onResume();
                    break;
                case AUDIOFOCUS_LOSS://失去音频焦点
                    onPause();
                    break;
                case AUDIOFOCUS_LOSS_TRANSIENT://暂时失去音频焦点
                    onPause();
                    break;
                default:
                    break;
            }
        }
    };

//    private static TTSManager mTTSManager;//单例实现

//    public static TTSManager getInstance(Context context) {
//        if (mTTSManager == null) {
//            synchronized (TTSManager.class) {
//                if (mTTSManager == null) {
//                    mTTSManager = new TTSManager(context);
//                }
//            }
//        }
//        return mTTSManager;
//    }

    protected TTSManager(Context context) {
        this.mContext = context;
        mTTSDownloadHelper = TTSDownloadHelper.getInstance(this, context);
        mTTSDataHelper = getDataHelper(this);
        mSpeechSynListener = new SpeechSynListener(this);
    }

    protected abstract TTSDataHelper getDataHelper(TTSManager manager);

    /**
     * 检测是否可用
     * 在阅读页初始化时调用
     * 可用直接返回，不可用则判断网络类型决定是否下载
     */
    @Override
    public void checkUse() {
        if (mTTSDownloadHelper.checkUse()) {
            return;
        } else {
            if (NetWorkUtil.isWifiConnected(mContext)) {
                mTTSDownloadHelper.download(false);//参数：决定下载进度是否对用户可感知
            }
        }
    }

    /**
     * 设置宿主对象，宿主对象为被驱动对象
     */
    @Override
    public void setHost(TTSHost host) {
        this.host = host;
    }

    /**
     * 下载插件方法
     */
    @Override
    public void download() {
        mTTSDownloadHelper.download(true);//true表示下载进度对用户可感知
    }

    /**
     * 初始化方法
     * 前提：具有音频焦点，宿主不为null，插件可用
     * 初始化后直接进入播放流程
     * paramsChange默认为false,执行播放，onstart 驱动host提供数据  在一次语音播放流程中，onstart只会执行一次
     * 当语音属性改变时需要重新初始化，paramsChange为true，onRestart会重新处理数据进行语音播放
     */
    @Override
    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                host.loading(true);
                //获取语音焦点
                if (AudioFocusManager.requestAudioFocus(mContext, mOnAudioFocusChangeListener) != AUDIOFOCUS_REQUEST_GRANTED) {
                    receive(TTS_EVENT_TOAST, "语音焦点获取失败，请先关闭其他语音输出");
                    host.loading(false);
                    return;
                }
                //判断宿主是否存在
                if (host == null) {
                    receive(TTS_EVENT_TOAST, "听书设置错误，请重试");
                    host.loading(false);
                    return;
                }
                //判断插件是否可用
                if (!mTTSDownloadHelper.checkUse()) {
                    if (TTSDownloadHelper.getData() != null) {
                        receive(TTS_EVENT_TOAST, "正在下载语音插件");
                        TTSDownloadHelper.setAuto(true);
                        return;
                    }
                    if (NetWorkUtil.isNetworkAvailable(mContext)) {
                        if (NetWorkUtil.isWifiConnected(mContext)) {
                            host.loading(false);
                            receive(TTS_EVENT_DOWNLOAD_PROGRESS, "start_wifi");
                        } else {
                            host.loading(false);
                            receive(TTS_EVENT_DOWNLOAD_PROGRESS, "start_net");
                        }
                    } else {
                        host.loading(false);
                        receive(TTS_EVENT_TOAST, "听书插件请求失败，请检查网络状态");
                    }
                    return;
                }
                mSpeechSynthesizer = SpeechSynthesizer.getInstance();
                mSpeechSynthesizer.setContext(mContext);
                mSpeechSynthesizer.setSpeechSynthesizerListener(mSpeechSynListener);
                // 文本模型文件路径 (离线引擎使用)
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mTTSDownloadHelper.getTextModelPath(true));
                // 声学模型文件路径 (离线引擎使用)
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mTTSDownloadHelper.getSpeechModelPath(isMale, true));
                // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
                mSpeechSynthesizer.setAppId(getAppid()/*这里只是为了让Demo运行使用的APPID,请替换成自己的id。*/);
                // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
                mSpeechSynthesizer.setApiKey(getAppkey(), getAppSocket());
                // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, isMale ? "1" : "0");
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, speed);
                // 设置Mix模式的合成策略
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
                // 初始化tts
                mSpeechSynthesizer.initTts(TtsMode.MIX);
                // 加载离线英文资源（提供离线中文合成功能）
                mSpeechSynthesizer.loadModel(mTTSDownloadHelper.getSpeechModelPath(isMale, true), mTTSDownloadHelper.getTextModelPath(true));
                // 加载离线英文资源（提供离线英文合成功能）
                mSpeechSynthesizer.loadEnglishModel(mTTSDownloadHelper.getTextModelPath(false), mTTSDownloadHelper.getSpeechModelPath(isMale, false));
                mSpeechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (!paramsChange) {
                    onStart();
                } else {
                    onRestrat();
                    paramsChange = false;
                }
            }
        }).start();
    }

    protected abstract String getAppSocket();

    protected abstract String getAppkey();

    protected abstract String getAppid();

    @Override
    public boolean isDoing() {//是否正在运行
        return isDoing;
    }

    public void setSex(boolean male) {//设置性别
        this.isMale = male;
        paramsChange = true;
    }

    public void setSpeed(String speed) {//设置语速
        this.speed = speed;
        paramsChange = true;
    }

    public void setTimer(long millisInFuture) {//设置计时器
        this.millisInFuture = millisInFuture;
        mTTSTimer.setMillisInFuture(millisInFuture).start();
    }

    @Override
    public void onStart() {
        isDoing = true;
        paramsChange = false;
        host.ttsStart();//获取数据，最终会调用receiveData（）
    }

    @Override
    public void onRestrat() {
        mTTSDataHelper.loadData(-1, null);//重新设置语音队列
    }

    @Override
    public void onPause() {//暂停
        host.ttsPause();
        mSpeechSynthesizer.pause();
    }

    @Override
    public void onResume() {//继续  判断有没有属性改变，有属性改变的时候  停止当前语音队列，重新init
        if (paramsChange) {
            onStop();
            init();
        } else {
            mSpeechSynthesizer.resume();
        }
        host.ttsResume();
    }

    @Override
    public void onStop() {
        mSpeechSynthesizer.stop();
    }

    @Override
    public void onDestory() {//播放结束方法，理论上执行一次  释放所有资源
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.release();
        }
        mTTSDataHelper.readEnd();
        mTTSDownloadHelper.stop();
        millisInFuture = 0;
        mTTSTimer.stop();
        if (host != null) {
            host.remainTime(0);
            host.ttsEnd();
            host = null;//置为空，否则会造成内存泄漏
        }
//        mTTSManager.setHost(null);
//        mTTSManager = null;
        isDoing = false;
        AudioFocusManager.abandonAudioFocus(mOnAudioFocusChangeListener);
    }

    @Override
    public void receiveData(int position, Object data) {
        mTTSDataHelper.loadData(position, data);
    }

    @Override
    public void addNewData(Object data) {
        mTTSDataHelper.addData(data);
    }

    @Override
    public void receive(String type, String value) {
        switch (type) {
            case TTS_EVENT_TOAST:
                if (host != null) {
                    host.toast(value);
                }
                break;
            case TTS_EVENT_DOWNLOAD_PROGRESS:
                if (host != null) {
                    host.download(value);
                }
                break;
            case TTS_EVENT_TIMER_PROGRESS:
                if (host != null) {
                    host.remainTime(Long.valueOf(value));
                }
                break;
            case TTS_EVENT_TIMER_END:
                onDestory();
                break;
            default:
                break;
        }
    }

    @Override
    public void addSpeck(String content, String tag) {
        mSpeechSynthesizer.speak(content, tag);
    }

    @Override
    public void speakStart(String tag) {
        host.getTag(tag);
    }

    @Override
    public void speakEnd(String tag) {
        mTTSDataHelper.removeSpeck(tag);
    }

    public boolean isMale() {
        return isMale;
    }

    public String getSpeed() {
        return speed;
    }

    public long getMillisInFuture() {
        return millisInFuture;
    }
}
