package hyj.weinxin_chat;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import hyj.weinxin_chat.common.WeixinAutoHandler;
import hyj.weinxin_chat.util.LogUtil;


public class ChatService extends AccessibilityService {
    public ChatService() {
        System.out.println("eventType-->null");
        AutoUtil.recordAndLog(record,Constants.CHAT_LISTENING);
        AutoUtil.recordAndLog(loginRecord,Constants.LOGINI_LISTENING);
    }
    Map<String,String> record = new HashMap<String,String>();
    Map<String,String> loginRecord = new HashMap<String,String>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }
    static int qNum;
    static int intervalTime;
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        WeixinAutoHandler.IS_START_SERVICE=true;
        SharedPreferences sharedPreferences = GlobalApplication.getContext().getSharedPreferences("url",MODE_PRIVATE);
        accounts = getAccount();
        String strQnum = sharedPreferences.getString("qNum","");
        String strIntervalTime = sharedPreferences.getString("intervalTime","");
        qNum = Integer.parseInt("".equals(strQnum)?"0":strQnum);
        intervalTime = Integer.parseInt("".equals(strIntervalTime)?"0":strIntervalTime);
        System.out.println("qnum--->"+qNum);
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(5);
        stpe.scheduleWithFixedDelay(new AutoChatThread(),3,intervalTime, TimeUnit.SECONDS);

        ScheduledThreadPoolExecutor stpe1 = new ScheduledThreadPoolExecutor(5);
        stpe1.scheduleWithFixedDelay(new WxLoginService(),3,2,TimeUnit.SECONDS);
    }
    static List<String> nickNames = new ArrayList<String>();
    static int nickNameIndes=0;
    static int countSendNum=0;
    class AutoChatThread implements Runnable{
        @Override
        public void run() {
            System.out.println("is_pause--->"+WeixinAutoHandler.IS_PAUSE);
            if(WeixinAutoHandler.IS_PAUSE) return;
            //while (true){
                System.out.println("---开启线程--"+Thread.currentThread().getName());
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if(root!=null){
                    if(nickNames.isEmpty()){
                        getNickNamesList(root);
                    }
                    List<AccessibilityNodeInfo> listView= root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bny");
                    if(listView!=null&&listView.size()>0) {
                        for (int i = 0, l = listView.get(0).getChildCount(); i < l; i++) {
                            String  nickText = "";
                            AccessibilityNodeInfo childNode = listView.get(0).getChild(i);
                            AccessibilityNodeInfo nickNode = AutoUtil.findNodeInfosById(childNode, "com.tencent.mm:id/agw");
                            if (nickNode != null) {
                                nickText = nickNode.getText().toString();
                                System.out.println("nick-->" + nickText);
                            }
                            if (nickNames.get(nickNameIndes).equals(nickText)) {
                                System.out.println("---进入 so----");
                                AutoUtil.performClick(nickNode, record, "点击昵称", 1000);

                                //6、填充第3步已获取消息到输入框
                                AccessibilityNodeInfo editText = AutoUtil.findNodeInfosById(getRootInActiveWindow(),"com.tencent.mm:id/a49");
                                if(editText!=null){
                                    editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,AutoUtil.createBuddleText("测试内容"+System.currentTimeMillis()+" "+countSendNum));
                                    AutoUtil.recordAndLog(record,Constants.CHAT_ACTION_05);
                                }
                                //7、发送
                                if(Constants.CHAT_ACTION_05.equals(record.get("recordAction"))){
                                    root = getRootInActiveWindow();
                                    AccessibilityNodeInfo sendBtn = AutoUtil.findNodeInfosByText(root,"发送");
                                    AutoUtil.performClick(sendBtn,record,Constants.CHAT_ACTION_06,1000);
                                    AutoUtil.performBack(ChatService.this,record,"全局返回");
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
    private void getNickNamesList(AccessibilityNodeInfo root){
        if(root!=null) {
            List<AccessibilityNodeInfo> listView = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bny");
            if (listView != null && listView.size() > 0) {
                for (int i = 0, l = listView.get(0).getChildCount(); i < l; i++) {
                    String nickText = "";
                    AccessibilityNodeInfo childNode = listView.get(0).getChild(i);
                    AccessibilityNodeInfo nickNode = AutoUtil.findNodeInfosById(childNode, "com.tencent.mm:id/agw");
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
    class WxLoginService implements Runnable{
        @Override
        public void run() {
            System.out.println("---开启自动登录线程--"+Thread.currentThread().getName()+" "+loginRecord.get("recordAction"));
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            if(nodeInfo==null){
                LogUtil.d("自动登录","node 为空；");
                return;
            }
            //1、退出当前账号
            if(AutoUtil.checkAction(loginRecord,Constants.LOGINI_LISTENING)){
                AccessibilityNodeInfo exitCurrentAcountBtn = AutoUtil.findNodeInfosByText(nodeInfo,"退出当前帐号");
                AutoUtil.performClick(exitCurrentAcountBtn,loginRecord,Constants.LOGIN_ACTION_01);
                return;
            }

            //2、确认退出
            if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_01)){
                AccessibilityNodeInfo quiteConfirmBtn = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bu8").get(0).getChild(1);
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
                List<AccessibilityNodeInfo> changeAccount =  nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/amv");
                if(changeAccount!=null&&changeAccount.size()>0){
                    AutoUtil.performClick(changeAccount.get(0).getChild(0),loginRecord,Constants.LOGIN_ACTION_04);
                }
                return;
            }
            //5、使用其他方式登录
            if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_04)){
                AccessibilityNodeInfo changeLoginWayBtn = AutoUtil.findNodeInfosByText(nodeInfo,"使用其他方式登录");
                AutoUtil.performClick(changeLoginWayBtn,loginRecord,Constants.LOGIN_ACTION_05);
                return;
            }
            //6、输入账号、密码、点击登录
            if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_05)||AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_06)||AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_07)){
                List<AccessibilityNodeInfo> userNode =  nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bfm");
                if(userNode!=null&&userNode.size()==1){
                    AutoUtil.performSetText(userNode.get(0).getChild(1),accounts.get(accountIndex)[0],loginRecord,Constants.LOGIN_ACTION_06);
                }
                List<AccessibilityNodeInfo> pwdNode =  nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bfn");
                if(pwdNode!=null&&pwdNode.size()==1){
                    AutoUtil.performSetText(pwdNode.get(0).getChild(1),accounts.get(accountIndex)[1],loginRecord,Constants.LOGIN_ACTION_07);
                }
                AutoUtil.sleep(500);
                if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_07)){
                    AccessibilityNodeInfo loginBtn = AutoUtil.findNodeInfosById(nodeInfo,"com.tencent.mm:id/bfo");
                    AutoUtil.performClick(loginBtn,loginRecord,Constants.LOGIN_ACTION_08);
                }
                if(AutoUtil.checkAction(loginRecord,Constants.LOGIN_ACTION_08)){
                    if(accountIndex==accounts.size()-1){
                        accountIndex = 0;
                    }else {
                        accountIndex = accountIndex+1;
                    }
                    AutoUtil.recordAndLog(loginRecord,Constants.LOGINI_LISTENING);
                }
            }
        }
    }
    public List<String[]> getAccount(){
        List<String[]> accounts = new ArrayList<String[]>();
        SharedPreferences shPref = GlobalApplication.getContext().getSharedPreferences("url",MODE_PRIVATE);
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
}
