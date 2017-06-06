package aprivate.zyb.com.ttsdemo.tts.Base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyibo on 2017/5/16.
 *
 */

public abstract class TTSDataHelper<K,V extends BaseBean> implements TTSDataHelperImpl {
    private TTSManagerImpl manager;
    private List<K> datas = new ArrayList<>();//数据源1 保存原始章节信息
    private List<V> speechs = new ArrayList<>();//数据源2 保存未进入语音通道的信息
    private List<V> speechQenen = new ArrayList<>();//数据源3 已经进入语音通道的信息
    static int position = 0;
    private boolean doing = true;

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (doing) {
                if (speechQenen.size() < 15 && speechs.size() != 0) {
                    V item = speechs.get(0);
                    speechQenen.add(item);
                    speechs.remove(0);
                    addSpeck(item.getContent(), item.getSign());
                }
            }
        }
    });

    public TTSDataHelper(TTSManagerImpl manager) {
        this.manager = manager;
    }

    /**
     * @param position position一般情况下为开始语音合成的当前页第一个字在当前章的位置，position为-1时是改变语音属性后继续播放，将未播放的语音重新进队
     * @param data     text数据，position为-1时 text数据为空
     */
    @Override
    public void loadData(int position, Object data) {
        doing = true;
        if (position == -1) {
            speechQenen.addAll(speechs);
            speechs.clear();
            speechs.addAll(speechQenen);
            speechQenen.clear();
        } else {
            datas.clear();
            speechs.clear();
            speechQenen.clear();
            datas.addAll((List<K>) data);
            this.position = position;
            for (int i = 0; i < datas.size(); i++) {
                if (i == 0) {
                    speechs.addAll(transform2ItemSpeechs(datas.get(i), position));
                } else {
                    speechs.addAll(transform2ItemSpeechs(datas.get(i), 0));
                }
            }
            thread.start();
        }
    }

    @Override
    public void addData(Object data) {
        if (!datas.contains((K) data)) {
            datas.add((K) data);
            if (datas.size() > 4) {
                datas.remove(0);
            }
            speechs.addAll(transform2ItemSpeechs((K) data, 0));
        }
    }

    @Override
    public void addSpeck(String text, String tag) {
        try {
            if (text.getBytes("GBK").length <= 1024) {
                manager.addSpeck(text, tag);
            } else {
                String newstring = "";
                for (int i = 0; i < text.length(); i++) {
                    newstring = text.substring(0, i);
                    if (newstring.getBytes("GBK").length > 1024) {
                        manager.addSpeck(text.substring(0, i - 1), tag);
                        addSpeck(text.substring(i), tag);
                        return;
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void removeSpeck(String tag) {
        speechQenen.remove(0);
        if (speechQenen.size() == 0){
            readEnd();
        }
    }

    @Override
    public void readEnd() {
        doing = false;
        datas.clear();
        speechs.clear();
        speechQenen.clear();
    }

    /**
     * 将文本按照段落分割，并生成播放的指定格式
     *
     * @param item
     * @param position
     * @return
     */
    public abstract List<V> transform2ItemSpeechs(K item, int position);
}
