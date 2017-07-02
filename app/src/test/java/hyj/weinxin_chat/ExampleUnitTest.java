package hyj.weinxin_chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        String dateTime = sdf.format(new Date());
        int a = Integer.parseInt("01");
        System.out.println(a);
        System.out.println(dateTime);
    }
}