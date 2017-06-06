package aprivate.zyb.com.ttsdemo.tts;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aprivate.zyb.com.ttsdemo.tts.Base.TTSDataHelper;
import aprivate.zyb.com.ttsdemo.tts.Base.TTSManagerImpl;
import aprivate.zyb.com.ttsdemo.tts.bean.ChapterForReader;
import aprivate.zyb.com.ttsdemo.tts.bean.ItemForSpeech;

/**
 * Created by zhouyibo on 2017/6/6.
 *
 */

public class TestTTSDataHelper extends TTSDataHelper<ChapterForReader, ItemForSpeech> {
    public TestTTSDataHelper(TTSManagerImpl manager) {
        super(manager);
    }

    /**
     * 功能：将外部数据源处理为内部数据源
     * @param item  传入的数据源
     * @param position  开始语音解析的位置
     * @return  返回处理后需要添加到语音通道的item
     * 注意设置sign   sign是语音播放标签，将你需要的信息设置进去，播放过程中根据返回的sign控制你的页面
     */
    @Override
    public List<ItemForSpeech> transform2ItemSpeechs(ChapterForReader item, int position) {
        Vector<ItemForSpeech> list = new Vector<>();
        String str = item.getContent();
        /*正则表达式：句子结束符*/
        String regEx = ":|。|！|；|\\？";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        /*按照句子结束符分割句子*/
        String[] words = p.split(str);

        /*将句子结束符连接到相应的句子后*/
        if (words.length > 0) {
            int count = 0;
            while (count < words.length) {
                if (m.find()) {
                    words[count] += m.group();
                }
                count++;
            }
        }
        /*输出结果*/
        int startPos = 0;
        for (int index = 0; index < words.length; index++) {
            String word = words[index];
            ItemForSpeech speech = new ItemForSpeech();
            speech.setContent(word);
            ItemForSpeech.tag tag = new ItemForSpeech.tag();
            tag.setChapterid(item.getChapterId());
            tag.setStartPos(startPos);
            startPos = startPos + word.length();
            tag.setEndPos(startPos - 1);
            speech.setTag(tag);
            speech.setSign(tag.toString());
            if (speech.getTag().getEndPos() >= position && speech.getTag().getStartPos() < position) {
                speech.setContent(word.substring(position - speech.getTag().getStartPos()));
            }
            if (speech.getTag().getEndPos() >= position) {
                list.add(speech);
            }
        }
        return list;
    }
}
