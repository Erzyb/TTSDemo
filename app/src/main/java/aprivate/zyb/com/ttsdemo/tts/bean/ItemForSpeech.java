package aprivate.zyb.com.ttsdemo.tts.bean;

import aprivate.zyb.com.ttsdemo.tts.Base.BaseBean;

/**
 * Created by zhouyibo on 2017/5/16.
 * bean  for  tts
 */

public class ItemForSpeech extends BaseBean{
    tag tag;
    String content;

    public ItemForSpeech(){}

    public ItemForSpeech.tag getTag() {
        return tag;
    }

    public void setTag(ItemForSpeech.tag tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public static class tag {
        String chapterid;
        int startPos;
        int endPos;

        public String getChapterid() {
            return chapterid;
        }

        public void setChapterid(String chapterid) {
            this.chapterid = chapterid;
        }

        public int getStartPos() {
            return startPos;
        }

        public void setStartPos(int startPos) {
            this.startPos = startPos;
        }

        public int getEndPos() {
            return endPos;
        }

        public void setEndPos(int endPos) {
            this.endPos = endPos;
        }

    }
}
