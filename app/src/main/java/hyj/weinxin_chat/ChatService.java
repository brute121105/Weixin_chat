package hyj.weinxin_chat;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import hyj.weinxin_chat.common.WeixinAutoHandler;
import hyj.weinxin_chat.util.LogUtil;

import static hyj.weinxin_chat.GlobalApplication.getContext;


public class ChatService extends AccessibilityService {
    public ChatService() {
        AutoUtil.recordAndLog(record,Constants.CHAT_LISTENING);
        AutoUtil.recordAndLog(loginRecord,Constants.LOGINI_LISTENING);
    }
    Map<String,String> record = new HashMap<String,String>();
    Map<String,String> loginRecord = new HashMap<String,String>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root!=null){
            autoLogin(root);
        }else {
            LogUtil.d("autoLogin","auto login root is null");
        }
        AccessibilityNodeInfo node = AutoUtil.findNodeInfosByText(root,"按住 说话");
        if(node!=null){
            System.out.println("node is not null--->");
            //node.performAction(MotionEvent.ACTION_DOWN);
            this.performGlobalAction(AccessibilityService.GESTURE_SWIPE_DOWN);
            AutoUtil.sleep(3000);
            this.performGlobalAction(AccessibilityService.GESTURE_SWIPE_UP);
            ;
        }else {
            System.out.println("node is null--->"+node);
        }
       /* List<AccessibilityNodeInfo> listView = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bny");
        if (listView != null && listView.size() > 0) {
            listView.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            AutoUtil.sleep(3000);
        }*/
    }
    static int qNum;
    static int intervalTime;
    static int intevalLoginTime;
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        WeixinAutoHandler.IS_START_SERVICE=true;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("url",MODE_PRIVATE);
        accounts = getAccount();
        String strQnum = sharedPreferences.getString("qNum","");
        String strIntervalTime = sharedPreferences.getString("intervalTime","");
        String strIntevalLoginTime = sharedPreferences.getString("intevalLoginTime","");
        qNum = Integer.parseInt("".equals(strQnum)?"0":strQnum);
        intervalTime = Integer.parseInt("".equals(strIntervalTime)?"0":strIntervalTime);
        intevalLoginTime = Integer.parseInt("".equals(strIntevalLoginTime)?"0":strIntevalLoginTime);
        LogUtil.d("chatService","发送个数:"+qNum+" 发送间隔:"+strIntervalTime+" 自动登录间隔:"+intevalLoginTime+" 配置账号个数:"+accounts.size());
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(5);
        //stpe.scheduleWithFixedDelay(new AutoChatThread(),3,intervalTime, TimeUnit.SECONDS);
    }
    static List<String> nickNames = new ArrayList<String>();
    static int nickNameIndes=0;
    static int countSendNum=0;
    static boolean isLogin=false;
    static int lastLoginMinute = -5;
    class AutoChatThread implements Runnable{
        @Override
        public void run() {
            if(WeixinAutoHandler.IS_PAUSE){
                LogUtil.d("autoChat","暂停服务");
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("mm");
            int minute = Integer.parseInt(sdf.format(new Date()));
            LogUtil.d("autoChat","线程"+Thread.currentThread().getName()+" minute:"+minute+" lastLoginMinute:"+lastLoginMinute);
            if(isLogin){
                LogUtil.d("autoChat","等待自动登录完成...");
                return;
            }
            if(intevalLoginTime!=0&&accounts.size()>0&&minute%intevalLoginTime==0&&Math.abs(minute-lastLoginMinute)>1){
                synchronized (this){
                    LogUtil.d("autoLogin","满足自动登录");;
                    AccessibilityNodeInfo loginRoot = getRootInActiveWindow();
                    if(loginRoot==null){
                        LogUtil.d("autoLogin","loginRoot is null");
                        return;
                    }
                    AccessibilityNodeInfo exitCurrentAcountBtn = AutoUtil.findNodeInfosByText(loginRoot,"我");
                    AutoUtil.performClick(exitCurrentAcountBtn,loginRecord,"点击[我]菜单",500);
                    if(exitCurrentAcountBtn==null){
                        LogUtil.d("autoLogin","获取菜单[我] is null");
                        return;
                    }
                    LogUtil.d("autoLogin","开始自动登录");
                    AccessibilityNodeInfo exitCurrentAcountBtn1 = AutoUtil.findNodeInfosByText(loginRoot,"设置");
                    AutoUtil.performClick(exitCurrentAcountBtn1,loginRecord,Constants.LOGINI_LISTENING);
                    lastLoginMinute = minute;
                }
            }

            AccessibilityNodeInfo ssBtn = AutoUtil.findNodeInfosById(getRootInActiveWindow(),"com.tencent.mm:id/g8");
            if(ssBtn!=null){
                AccessibilityNodeInfo ss =  ssBtn.getChild(1);
                AutoUtil.performClick(ss,record,"搜索");
                //输入微信id
                findNodeAndSetText(1);
                AutoUtil.sleep(500);
                //点击搜索结果
                findNodeAndClickId(1,"com.tencent.mm:id/j9");
                //判断是否有键盘按钮
                findNodeAndClickId(1,"com.tencent.mm:id/a5j");
                //填充文本
                findEditAndSetText(1);
                //点击发送
                findNodeAndClickId(1,"com.tencent.mm:id/a5k");
                AutoUtil.sleep(300);
                //返回
                findNodeAndClickId(1,"com.tencent.mm:id/gr");
                //back2List(getRootInActiveWindow());
                //back2List(getRootInActiveWindow());
            }

            if(true) return;
            //while (true){
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if(root!=null){
                    if(nickNames.isEmpty()){
                        getNickNamesList(root);
                    }
                    List<AccessibilityNodeInfo> listView= root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bpl");
                    if(listView!=null&&listView.size()>0) {
                        for (int i = 0, l = listView.get(0).getChildCount(); i < l; i++) {
                            String  nickText = "";
                            AccessibilityNodeInfo childNode = listView.get(0).getChild(i);
                            AccessibilityNodeInfo nickNode = AutoUtil.findNodeInfosById(childNode, "com.tencent.mm:id/aig");
                            if (nickNode != null) {
                                nickText = nickNode.getText().toString();
                                System.out.println("nick-->" + nickText);
                            }
                            if (nickNames.get(nickNameIndes).equals(nickText)) {
                                AutoUtil.performClick(nickNode, record, "点击昵称:"+nickText, 1000);

                                //6、填充第3步已获取消息到输入框
                                findEditAndSetText(0);
                                if(!AutoUtil.checkAction(record,Constants.CHAT_ACTION_05)){
                                    AccessibilityNodeInfo keyBtn = AutoUtil.findNodeInfosById(getRootInActiveWindow(),"com.tencent.mm:id/a5c");
                                    if(keyBtn!=null) {
                                        LogUtil.d("autoChat","键盘按钮已获取");
                                        AutoUtil.performClick(keyBtn, record, "点击键盘按钮",500);
                                        findEditAndSetText(1);
                                    }else{
                                        LogUtil.d("autoChat","键盘按钮 is null");
                                    }
                                }
                                //7、发送
                                if(Constants.CHAT_ACTION_05.equals(record.get("recordAction"))){
                                    root = getRootInActiveWindow();
                                    AccessibilityNodeInfo sendBtn = AutoUtil.findNodeInfosByText(root,"发送");
                                    AutoUtil.performClick(sendBtn,record,Constants.CHAT_ACTION_06,1000);
                                    back2List(root);
                                    //AutoUtil.performBack(ChatService.this,record,"全局返回");
                                    AutoUtil.recordAndLog(record,Constants.CHAT_LISTENING);
                                }
                                if(nickNameIndes==nickNames.size()-1||nickNameIndes==qNum-1){
                                    nickNameIndes=0;
                                    countSendNum = countSendNum+1;
                                }else {
                                    nickNameIndes = nickNameIndes+1;
                                }
                                break;
                            }
                        }
                    }
                }
            //}
        }
    }
    private List<String> getWxids(){
        List<String> ids = new ArrayList<String>();
        ids.add("bh4073");
        ids.add("mm77375");
        ids.add("mt74196");
        ids.add("ok75581");
        ids.add("bbk4872");
        ids.add("cba4871");
        ids.add("cba4870");
        ids.add("nnk4873");
        ids.add("ss978692");
        ids.add("zhq686366");
        ids.add("hyj5690");
        return ids;
    }
    static int wxIdIndex=0;
    private void findNodeAndSetText(int tryCount){
        if(tryCount==10) return;
        AccessibilityNodeInfo ssEdit =  AutoUtil.findNodeInfosById(getRootInActiveWindow(),"com.tencent.mm:id/h2");
        if(ssEdit!=null){
            AutoUtil.performSetText(ssEdit,getWxids().get(wxIdIndex),record,"搜索微信id");
            if(wxIdIndex==getWxids().size()-1){
                wxIdIndex = 0;
            }else {
                wxIdIndex = wxIdIndex+1;
            }
        }else if(tryCount!=0){
            LogUtil.d("autoChat","搜索框 is null "+tryCount);
            AutoUtil.sleep(200);
            findNodeAndSetText(tryCount+1);
        }
    }
    private void findNodeAndClickId(int tryCount,String id){
        if(tryCount==10) return;
        AccessibilityNodeInfo node= AutoUtil.findNodeInfosById(getRootInActiveWindow(),id);;
        if(node!=null){
            AutoUtil.performClick(node,record,"点击"+id);
        }else if(tryCount!=0){
            LogUtil.d("autoChat",id+" is null "+tryCount);
            AutoUtil.sleep(200);
            findNodeAndClickId(tryCount+1,id);
        }
    }
    private void findNodeAndClickText(int tryCount,String text){
        if(tryCount==10) return;
        AccessibilityNodeInfo node= AutoUtil.findNodeInfosById(getRootInActiveWindow(),text);;
        if(node!=null){
            AutoUtil.performClick(node,record,"点击"+text);
        }else if(tryCount!=0){
            LogUtil.d("autoChat",text+" is null "+tryCount);
            AutoUtil.sleep(200);
            findNodeAndClickText(tryCount+1,text);
        }
    }
    private void findEditAndSetText(int tryCount){
        if(tryCount==10) return;
        AccessibilityNodeInfo editText = AutoUtil.findNodeInfosById(getRootInActiveWindow(),"com.tencent.mm:id/a5e");
        if(editText!=null){
            List<String> msgs = AutoUtil.getSomeMsgs();
            int randomNum = (int)(Math.random()*msgs.size());
            editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,AutoUtil.createBuddleText(msgs.get(randomNum)+System.currentTimeMillis()+" "+countSendNum));
            AutoUtil.recordAndLog(record,Constants.CHAT_ACTION_05);
        }else if(tryCount!=0) {
            LogUtil.d("autoChat","输入框 is null "+tryCount);
            AutoUtil.sleep(200);
            findEditAndSetText(tryCount+1);
        }
    }
    private void back2List(AccessibilityNodeInfo root){
        AccessibilityNodeInfo backBtn = AutoUtil.findNodeInfosById(root,"com.tencent.mm:id/gr");
        AutoUtil.performClick(backBtn,record,"全局返回");
    }
    private void getNickNamesList(AccessibilityNodeInfo root){
        if(root!=null) {
            List<AccessibilityNodeInfo> listView = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bpl");
            if (listView != null && listView.size() > 0) {
                for (int i = 0, l = listView.get(0).getChildCount(); i < l; i++) {
                    String nickText = "";
                    AccessibilityNodeInfo childNode = listView.get(0).getChild(i);
                    AccessibilityNodeInfo nickNode = AutoUtil.findNodeInfosById(childNode, "com.tencent.mm:id/aig");
                    if (nickNode != null) {
                        nickText = nickNode.getText().toString();
                        System.out.println("getNickNamesList nick-->" + nickText);
                        nickNames.add(nickText);
                    }
                }
            }
        }
    }
    @Override
    public void onInterrupt() {
        System.out.println("--->onInterrupt");
    }

    @Override
    public void onDestroy() {
        System.out.println("--->onDestroy");
    }

    static  int accountIndex = 0;
    static  List<String[]> accounts;
    private void autoLogin(AccessibilityNodeInfo nodeInfo){
        //0、退出
        if(AutoUtil.checkAction(loginRecord,Constants.LOGINI_LISTENING)){
            AccessibilityNodeInfo exitCurrentAcountBtn = AutoUtil.findNodeInfosByText(nodeInfo,"退出");
            if(exitCurrentAcountBtn!=null){
                isLogin=true;
            }
            AutoUtil.performClick(exitCurrentAcountBtn,loginRecord,Constants.LOGIN_ACTION_00);
            return;
        }
        //1、退出当前账号
        if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_00)){
            AccessibilityNodeInfo exitCurrentAcountBtn = AutoUtil.findNodeInfosByText(nodeInfo,"退出当前帐号");
            System.out.println("exitCurrentAcountBtn--->"+exitCurrentAcountBtn);
            AutoUtil.performClick(exitCurrentAcountBtn,loginRecord,Constants.LOGIN_ACTION_01);
            return;
        }

        //2、确认退出
        if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_01)){
            AccessibilityNodeInfo quiteConfirmBtn = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bvw").get(0).getChild(1);
            AutoUtil.performClick(quiteConfirmBtn,loginRecord,Constants.LOGIN_ACTION_02);
            return;
        }
        //3、更多
        if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_02)){
            AccessibilityNodeInfo moreBtn = AutoUtil.findNodeInfosByText(nodeInfo,"更多");
            AutoUtil.performClick(moreBtn,loginRecord,Constants.LOGIN_ACTION_03);
            return;
        }
        //4、切换账号
        if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_03)){
            AccessibilityNodeInfo changeAccount = AutoUtil.findNodeInfosById(nodeInfo,"com.tencent.mm:id/fl");
            AutoUtil.performClick(changeAccount,loginRecord,Constants.LOGIN_ACTION_04);
            return;
        }
        //5、使用其他方式登录
        if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_04)){
            AccessibilityNodeInfo changeLoginWayBtn = AutoUtil.findNodeInfosByText(nodeInfo,"用微信号/QQ号/邮箱登录");
            AutoUtil.performClick(changeLoginWayBtn,loginRecord,Constants.LOGIN_ACTION_05);
            return;
        }
        //6、输入账号、密码、点击登录
        if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_05)||AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_06)||AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_07)){
            List<AccessibilityNodeInfo> userNode =  nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bhg");
            if(userNode!=null&&userNode.size()==1){
                AutoUtil.performSetText(userNode.get(0).getChild(1),accounts.get(accountIndex)[0],loginRecord,Constants.LOGIN_ACTION_06);
            }
            List<AccessibilityNodeInfo> pwdNode =  nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bhh");
            if(pwdNode!=null&&pwdNode.size()==1){
                AutoUtil.performSetText(pwdNode.get(0).getChild(1),accounts.get(accountIndex)[1],loginRecord,Constants.LOGIN_ACTION_07);
            }
            AutoUtil.sleep(500);
            if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_07)){
                AccessibilityNodeInfo loginBtn = AutoUtil.findNodeInfosById(nodeInfo,"com.tencent.mm:id/bhj");
                AutoUtil.performClick(loginBtn,loginRecord,Constants.LOGIN_ACTION_08);
            }
            if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_08)){
                if(accountIndex==accounts.size()-1){
                    accountIndex = 0;
                }else {
                    accountIndex = accountIndex+1;
                }
                AutoUtil.recordAndLog(loginRecord,Constants.LOGINI_LISTENING);
                nickNames = new ArrayList<String>();
                nickNameIndes=0;
                countSendNum=0;
                isLogin=false;
            }
        }
    }
    private void showToastByRunnable(final Context context, final CharSequence text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<String[]> getAccount(){
        List<String[]> accounts = new ArrayList<String[]>();
        SharedPreferences shPref = getContext().getSharedPreferences("url",MODE_PRIVATE);
        String u1 = shPref.getString("username1","");
        String p1 = shPref.getString("pwd1","");
        String u2 = shPref.getString("username2","");
        String p2 = shPref.getString("pwd2","");
        String u3 = shPref.getString("username3","");
        String p3 = shPref.getString("pwd3","");
        String u4 = shPref.getString("username4","");
        String p4 = shPref.getString("pwd4","");
        System.out.println("u1-->"+u1);
        System.out.println("p1-->"+p1);
        System.out.println("u2-->"+u2);
        System.out.println("p2-->"+p2);
        if(u1!=null&&!"".equals(u1))
            accounts.add(new String[]{u1,p1});
        if(u2!=null&&!"".equals(u2))
            accounts.add(new String[]{u2,p2});
        if(u3!=null&&!"".equals(u3))
            accounts.add(new String[]{u3,p3});
        if(u4!=null&&!"".equals(u4))
            accounts.add(new String[]{u4,p4});
        return accounts;

    }
    public  static void getChild(AccessibilityNodeInfo node){
        System.out.println("-----------start---------");
        if(node!=null){
            int count = node.getChildCount();
            System.out.println("child count"+count+"node text-->"+node.getText()+"  node clsName-->"+node.getClassName()+" desc"+node.getContentDescription());
            if(count>0){
                for(int i=0,l=count;i<l;i++){
                    if(i==0){
                        System.out.println("第"+i+"层 第0个子节点，兄弟节点数:"+count);
                    }
                    AccessibilityNodeInfo child = node.getChild(i);
                    getChild(child);
                    if(child!=null){
                        System.out.println(i+" child text-->"+child.getText()+" child clsName-->"+child.getClassName()+" desc"+node.getContentDescription());
                    }
                }
            }
        }
        System.out.println("-----------end---------");
    }

}
