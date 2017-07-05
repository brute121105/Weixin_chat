package hyj.weinxin_chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static hyj.weinxin_chat.GlobalApplication.getContext;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        getMsgs("#11#22");
    }

    private List<String> getMsgs(String str){
        List<String> strList = new ArrayList<String>();
        if(str==null||"".equals(str.trim())){
            return strList;
        }
        str = str.replaceAll("\n","");
        String[] strs = str.split("#");
        for(String s :strs){
            if(!"".equals(s)){
                System.out.println(s);
                strList.add(s);
            }
        }
        return strList;
    }
}