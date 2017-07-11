package hust.com.jsp.bean;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael-Lee on 2017/7/8.
 */

public class BZPlan_TimeSchemaOrder {

    private BZPlan bzPlan;
    private List<BZPlan> bzPlanList;
    Map<JZJ,ZWNode> jzjZWNodeListNoConflictMap;
    Map<String,List<JZJ>> zwFJListMap;//zwName--该zw下冲突的所有JZJ列表
    Map<String,List<ZWNode>> zwNodeListMap;//zwName--该zw下冲突的所有JZJ的全排列链表
    List<BZPlanSchemaItem> schemaList;//存储所有的方案，从而找出最优解

    public BZPlan_TimeSchemaOrder(List<BZPlan> bzPlanList){
        this.bzPlanList=bzPlanList;
        this.jzjZWNodeListNoConflictMap=new HashMap<>();
        this.zwFJListMap =new HashMap<>();
        this.zwNodeListMap =new HashMap<>();
        this.schemaList=new ArrayList<>();
    }

    //得到每个zw下需要bz任务的jzj
    private void getZWListPlan(){
        for(BZPlan bzplan:bzPlanList){
            List<BZPlanItem> bzPlanItems=bzplan.getBzPlanItemList();
            JZJ jzj=bzplan.getJzj();
            for(BZPlanItem bzItem:bzPlanItems){
                String zwName=bzItem.getStation().getDisplayName();
                if(!zwFJListMap.containsKey(zwName)){
                    List<JZJ> jzjList=new ArrayList<>();
                    jzjList.add(jzj);
                    zwFJListMap.put(zwName,jzjList);
                }else {
                    if(!zwFJListMap.get(zwName).contains(jzj))
                        zwFJListMap.get(zwName).add(jzj);
                }
            }
        }
    }

    //获取ZW上JZJ列表的全排列链表,返回可能的排序方案总数
    private int getZWNodeList(){
        for(Map.Entry<String,List<JZJ>> entry : zwFJListMap.entrySet()){
            List<JZJ> jzjList=entry.getValue();
            String zwName=entry.getKey();
            Station station=new Station();
            station.setDisplayName(zwName);
            JZJ[] jzjs=new JZJ[jzjList.size()];
            for(int i=0;i<jzjList.size();i++){
                jzjs[i]=jzjList.get(i);
            }
            List<ZWNode> listZWNode=new ArrayList<>();//保存所有的全排列
            arrange(jzjs,0,listZWNode,station);
            zwNodeListMap.put(entry.getKey(),listZWNode);
        }
        int num=1;
        for(Map.Entry<String,List<ZWNode>> entry : zwNodeListMap.entrySet()){
            num*=entry.getValue().size();
        }

        return num;
    }
    private void swap(JZJ[] jzjArray,int i,int j) {  //将jzj数组中的第i个jzj和第j个jzj交换
        if(i!=j) {
            JZJ tmp ;
            tmp = jzjArray[i];
            jzjArray[i] = jzjArray[j];
            jzjArray[j] = tmp;
        }
    }
    private void arrange(JZJ[] jzjArray, int st,List<ZWNode> listZWNode,Station station) {  //递归实现jzjArray[st]到jzjArray[len-1]的全排列
        if(st==jzjArray.length-1)  {
            ZWNode head=new ZWNode(jzjArray[0],station);
            ZWNode h=head;
            for(int j=1; j<jzjArray.length; j++)
            {
                ZWNode node=new ZWNode(jzjArray[j],station);
                h.next=node;
                h=h.next;
            }
            listZWNode.add(head);
        } else{           //否则，依次递归调用。
            for(int i=st; i<jzjArray.length; i++) {
                swap(jzjArray,st,i);
                arrange(jzjArray,st+1,listZWNode,station);
                swap(jzjArray,st,i);//复位
            }
        }
    }

    private Map<Integer,List<ZWNode>> getSchemaOrderCombine(){
        Map<Integer,List<ZWNode>> schemaCombineMap=new HashMap<>();

        int count=0;
        for(Map.Entry<String,List<ZWNode>> entry : zwNodeListMap.entrySet()) {
            String zwName = entry.getKey();
            List<ZWNode> zwNodeList = entry.getValue();
            List<JZJ> jzjList = zwFJListMap.get(zwName);
            if (jzjList.size() > 1) {
                schemaCombineMap.put(count,zwNodeList);
                count++;
            }
        }
        List<List<ZWNode>> arr=new ArrayList<>();
        for(int j=0;j<count;j++){
            arr.add(schemaCombineMap.get(j));
        }
        List<List<ZWNode>> arr1=new ArrayList<>();
        List<List<ZWNode>> arr2=new ArrayList<>();
        int len=1;
        for(int i=0;i<arr.size();i++){
            len=len*arr.get(i).size();
            arr2=new ArrayList<>();
            for(int j=0;j<len;j++){
                List<ZWNode> list=new ArrayList<>();
                ZWNode head=arr.get(i).get(j%arr.get(i).size());
                ZWNode newH=cloneNodeList(head);
                list.add(newH);
                while(newH.next!=null){
                    newH=newH.next;
                }
                JZJ jzj=new JZJ(0).setDisplayName("flag");
                ZWNode flag1=new ZWNode(jzj);
                newH.next=flag1;
                if(i==0)
                    arr2.add(list);
                else {
                    List<ZWNode> list2=new ArrayList<>();
                    ZWNode h=arr1.get(j / arr.get(i).size()).get(0);
                    ZWNode cloneH=cloneNodeList(h);
                    list2.add(cloneH);
                    arr2.add(list2);
                    while(cloneH.next!=null){
                        cloneH=cloneH.next;
                    }
                    cloneH.next=list.get(0);

                }
            }
            arr1=arr2;
        }
        Map<Integer,List<ZWNode>> schemaMap=new HashMap<>();
        for(int i=0;i<arr2.size();++i){
            List<ZWNode> list=new ArrayList<>();
            ZWNode oldHead,pNode,qNode;
            oldHead=arr2.get(i).get(0);
            pNode=oldHead;
            qNode=oldHead;
            String flag;
            while(qNode.next!=null){
                pNode=qNode;
                qNode=qNode.next;
                flag=qNode.jzj.getDisplayName();
                if("flag".equals(flag)){
                    pNode.next=null;
                    list.add(oldHead);
                    qNode=qNode.next;
                    pNode=qNode;
                    oldHead=qNode;
                }
                if(qNode==null) break;
            }

            schemaMap.put(i,list);
        }

        return schemaMap;
    }

    private ZWNode cloneNodeList(ZWNode head){
        ZWNode pNode=head;
        ZWNode cloneNode=null,cloneHead=null;
        if(pNode!=null){
            cloneHead=new ZWNode(new JZJ(1));
            cloneHead.jzj=pNode.jzj;
            cloneHead.actionStartTime=pNode.actionStartTime;
            cloneHead.actionEndTime=pNode.actionEndTime;
            cloneHead.spendTime=pNode.spendTime;
            cloneHead.station=pNode.station;
            cloneHead.next=null;
            cloneNode=cloneHead;
            pNode=pNode.next;
        }
        while (pNode!=null){
            ZWNode temp=new ZWNode(new JZJ(1));
            temp.jzj=pNode.jzj;
            temp.actionStartTime=pNode.actionStartTime;
            temp.actionEndTime=pNode.actionEndTime;
            temp.spendTime=pNode.spendTime;
            temp.station=pNode.station;
            temp.next=null;
            cloneNode.next=temp;
            cloneNode=cloneNode.next;
            pNode=pNode.next;
        }
        return cloneHead;
    }

    public void initSchemaItem(){
        getZWListPlan();
        int num=getZWNodeList();
        getBZPlanNoConflictNodeList();
        for(int i=0;i<num;i++){
            BZPlanSchemaItem schemaItem=new BZPlanSchemaItem(i);
            for(Map.Entry<JZJ,ZWNode> entry : jzjZWNodeListNoConflictMap.entrySet()) {
                schemaItem.jzjZWNodeMap.put(entry.getKey(),cloneNodeList(entry.getValue()));
            }
            schemaList.add(schemaItem);
        }

        Map<Integer,List<ZWNode>> schemaMap=getSchemaOrderCombine();
        for(Map.Entry<Integer,List<ZWNode>> entry:schemaMap.entrySet()){
            int id=entry.getKey();//方案id
            List<ZWNode> list=entry.getValue();//冲突ZW对应的全排列链表头
            BZPlanSchemaItem schemaItem=schemaList.get(id);
            Map<JZJ,ZWNode> jzjZWNodeMap=schemaItem.jzjZWNodeMap;
            for(ZWNode insertHead:list) {//全排列链表
                JZJ insertJzj = insertHead.jzj;//排列链表当前节点指向的JZJ
                String zwName=insertHead.station.getDisplayName();
                List<JZJ> jzjList=zwFJListMap.get(zwName);
                getSchemaItemOrder(jzjZWNodeMap,zwName,jzjList,insertHead,insertJzj);
            }
        }

        //TEST CODE
        for(int i=0;i<schemaList.size();i++){
            Log.v("schema"," id="+i);
            BZPlanSchemaItem schemaItem=schemaList.get(i);
            Map<JZJ,ZWNode> jzjZWNodeMap=schemaItem.jzjZWNodeMap;
            for(Map.Entry<JZJ,ZWNode> entry:jzjZWNodeMap.entrySet()){
                ZWNode p=entry.getValue();
//                Log.v(entry.getKey()+" ",p.station.getDisplayName()+"->");
                String str=entry.getKey().getDisplayName()+"： "+"F"+p.jzj.getDisplayName().split("-")[1]+p.station.getDisplayName()+"->";
                while (p.next!=null){
                    p=p.next;
                    str+="F"+p.jzj.getDisplayName().split("-")[1]+p.station.getDisplayName()+"->";
                }
                Log.v("",str);
            }
        }
    }


    public List<BZPlan> getSchemaTimeProgress(){
        int id=0;
        float time=Integer.MAX_VALUE;
        for(int i=0;i<schemaList.size();i++){
            BZPlanSchemaItem schemaItem=schemaList.get(i);
            float schemaTime=schemaItem.getSchemaTime();
            if(schemaTime<time){
                time=schemaTime;
                id=i;
            }
        }
        Log.v("schema","id="+id);
        BZPlanSchemaItem schemaItem=schemaList.get(id);
        Map<JZJ,ZWNode> jzjZWNodeMap=schemaItem.jzjZWNodeMap;
        for(Map.Entry<JZJ,ZWNode> entry:jzjZWNodeMap.entrySet()){//TSET CODE
            JZJ jzj=entry.getKey();
            ZWNode node=entry.getValue();
            String str="";
            str+=node.station.getDisplayName()+":"+"st="+node.actionStartTime+" et="+node.actionEndTime+",";//TEST CODE
            while (node.next!=null){
                node=node.next;
                str+=node.station.getDisplayName()+":"+"st="+node.actionStartTime+" et="+node.actionEndTime+",";//TEST CODE
            }
            Log.v(jzj.getDisplayName(),str);
        }
       for(Map.Entry<JZJ,ZWNode> entry:jzjZWNodeMap.entrySet()){
           JZJ jzj=entry.getKey();
           ZWNode node=entry.getValue();
           BZPlan bzPlan=new BZPlan();
           String str="";//TSET CODE
           for(int i=0;i<bzPlanList.size();i++){
               if(bzPlanList.get(i).getJzj().getDisplayName().equals(jzj.getDisplayName()))
                   bzPlan=bzPlanList.get(i);
           }
           for(int i=0;i<bzPlan.getBzPlanItemList().size();i++){
                BZPlanItem bzItem=bzPlan.getBzPlanItemList().get(i);
               while (!bzPlan.getJzj().getDisplayName().equals(node.jzj.getDisplayName()))
                   node=node.next;
               if(bzPlan.getJzj().getDisplayName().equals(node.jzj.getDisplayName())){
                   bzItem.setStartTime(node.actionStartTime);
                   bzItem.setEndTime(node.actionEndTime);
                   str+="st="+node.actionStartTime+" et="+node.actionEndTime+",";//TEST CODE
                   node=node.next;
               }
           }
           Log.v(jzj.getDisplayName(),str);
       }

        return bzPlanList;
    }

    //单方案某处插入节点
    private void getSchemaItemOrder(Map<JZJ,ZWNode> jzjZWNodeMap,String zwName,List<JZJ> jzjList,ZWNode insertHead,JZJ insertJzj){

            for(int j=0;j<jzjList.size();j++) {//对占用该zw的所有JZJ
                ZWNode oldHead = jzjZWNodeMap.get(jzjList.get(j));
                ZWNode pre = oldHead;
                String zwNameSchema = "";//指向zw为zwName的节点
                if (oldHead != null) {
                    zwNameSchema = oldHead.station.getDisplayName();
                }
                while (!zwName.equals(zwNameSchema)) {
                    if (oldHead.next != null) {
                        pre = oldHead;
                        oldHead = oldHead.next;
                        zwNameSchema = oldHead.station.getDisplayName();
                    }
                }
                ZWNode cloneNewHead=cloneNodeList(insertHead);
                if( !insertJzj.getDisplayName().equals(pre.jzj.getDisplayName())){//需要等待,即要插入节点
                    if(pre.equals(oldHead)){//头结点插入
                        jzjZWNodeMap.put(pre.jzj,cloneNewHead);
                        while (cloneNewHead.next!=null && !cloneNewHead.next.jzj.getDisplayName().equals(pre.jzj.getDisplayName())){
                            cloneNewHead=cloneNewHead.next;
                        }
                        cloneNewHead.next=pre;
                    }else {//中间节点插入
                        pre.next=cloneNewHead;
                        while (cloneNewHead.next!=null && !cloneNewHead.next.jzj.getDisplayName().equals(pre.jzj.getDisplayName())){
                            cloneNewHead=cloneNewHead.next;
                        }
                        cloneNewHead.next=oldHead;
                    }
                }
            }
    }

    private void getBZPlanNoConflictNodeList(){
        for(BZPlan bzplan:bzPlanList){
            JZJ jzj=bzplan.getJzj();
            List<BZPlanItem> bzPlanItems=bzplan.getBzPlanItemList();
            ZWNode head=new ZWNode(jzj);
            ZWNode h=head;
            for(int i=0;i<bzPlanItems.size();i++){
                BZPlanItem bzItem=bzPlanItems.get(i);
                Station station=bzItem.getStation();
                String zwName=station.getDisplayName();
                if(i==0){
                    head.station=station;
                    head.spendTime=bzItem.getSpendTime();
                }else {
                    ZWNode node=new ZWNode(jzj,station);
                    node.spendTime=bzItem.getSpendTime();
                    h.next=node;
                    h=h.next;
                }
            }
            jzjZWNodeListNoConflictMap.put(jzj,head);
        }
    }




    class BZPlanSchemaItem {
        int id;//一个时间排序方案id，先找出该方案的关键路径，即MAX_time,需要在所有的排序方案中找到最小的MAX_time
        Map<JZJ, ZWNode> jzjZWNodeMap;//对每个jzj，它有多个zw进行任务动作，因此在
        float totalSpendTime;//改方案的MAX_time

        public BZPlanSchemaItem(int id) {
            this.id = id;
            this.totalSpendTime = 0;
            this.jzjZWNodeMap = new HashMap<>();
        }

//        /TODO 这里插入的节点不为同一个，因此需修改
        //计算该方案下的关键路径时间
        float getSchemaTime(){

            float tickTock=0;
            boolean clock=true;

            while(clock){
                clock=false;
                for(Map.Entry<JZJ,ZWNode> entry : jzjZWNodeMap.entrySet()){
                    ZWNode head=entry.getValue();
                    JZJ jzj=entry.getKey();
                    while (head.next != null && head.actionEndTime != 0)
                        head = head.next;
                    if(head.jzj.getDisplayName().equals(jzj.getDisplayName())){
                        while (head.next != null && head.actionEndTime != 0)
                            head = head.next;
                        if ((tickTock - head.actionStartTime) == head.spendTime) {
                            head.actionEndTime = tickTock;
//                            if ((tickTock - head.actionStartTime) == head.spendTime) {
                                String zwName = head.station.getDisplayName();
                                List<JZJ> jzjList = zwFJListMap.get(zwName);
                                if (jzjList.size() > 1) {         //通知其他FJ,告诉其已释放ZW资源
                                    for (int index = 0; index < jzjList.size(); index++) {
                                        JZJ jzj1 = jzjList.get(index);
                                        if (!jzj.getDisplayName().equals(jzj1.getDisplayName())) {
                                            ZWNode node = jzjZWNodeMap.get(jzj1);
                                            while (node != null && !node.station.getDisplayName().equals(head.station.getDisplayName())) {
                                                node = node.next;
                                            }
                                            while(node.next != null && node.next.station.getDisplayName().equals(head.station.getDisplayName()) && node.actionStartTime!=0 && node.actionEndTime!=0){
                                                node=node.next;
                                            }
                                            if(node.actionEndTime==0) {
                                                node.actionEndTime = head.actionEndTime;
                                                if (node.next != null)
                                                    node.next.actionStartTime = head.actionEndTime;
                                            }
                                        }
                                    }
                                }

                                if (head.next != null) {
                                    head = head.next;
                                    head.actionStartTime = tickTock;
                                    clock = true;
                                }
//                            }
                        }
                        if (head.actionEndTime == 0)
                            clock = true;
                    }//else {//需等待的点
                    if(!head.jzj.getDisplayName().equals(jzj.getDisplayName())){
                        while (head.next != null && head.actionEndTime != 0)
                            head = head.next;
                        if(head.next!=null && head.actionEndTime!=0) {//等待资源
                            head.next.actionStartTime=head.actionEndTime;
                            head=head.next;
                            clock=true;
                        }
                        if (head.actionEndTime == 0)
                            clock = true;
                    }
                }

                if(!clock)
                    totalSpendTime=tickTock;
                tickTock++;
            }
            Log.v("schema"+id+" totalSpendTime=",totalSpendTime+"");
            return totalSpendTime;
        }
//        //计算该方案下的关键路径时间
//        float getSchemaTime() {
//
//            float tickTock = 0;
//            boolean clock = true;
//
//            while (clock) {
//                clock = false;
//                for (Map.Entry<JZJ, ZWNode> entry : jzjZWNodeMap.entrySet()) {
//                    ZWNode head = entry.getValue();
//                    JZJ jzj = entry.getKey();
//                    if (head.jzj.getDisplayName().equals(jzj.getDisplayName())) {//无资源冲突的节点
//                        if (head.actionEndTime == 0) {
//                            if ((tickTock - head.actionStartTime) == head.spendTime) {
//                                head.actionEndTime = tickTock;
//                                if (head.next != null) {
//                                    head = head.next;
//                                    head.actionStartTime = tickTock;
//                                    clock = true;
//                                }
//                            }
//                        } else {
//                            while (head.next != null && head.actionEndTime != 0)
//                                head = head.next;
//                            if ((tickTock - head.actionStartTime) == head.spendTime) {
//                                head.actionEndTime = tickTock;
//                                if (head.next != null) {
//                                    head = head.next;
//                                    head.actionStartTime = tickTock;
//                                    clock = true;
//                                }
//                            }
//                        }
//                        if (head.actionEndTime == 0)
//                            clock = true;
//                    }
////                    }else if(head.jzj.getDisplayName().equals(jzj.getDisplayName()) && head.actionEndTime!=0){
////                        while (head.next!=null && head.actionEndTime!=0)
////                            head=head.next;
////                        if(head.actionEndTime!=0)
////                            clock=true;
////                    }
//                    else {//需等待的点
////                        if (head.actionEndTime == 0) {//先查询资源是否已空出
////                            ZWNode node = jzjZWNodeMap.get(jzj);
////                            while (node != null && !node.station.getDisplayName().equals(head.station.getDisplayName())) {
////                                node = node.next;
////                            }
////                            if (node.actionEndTime != 0) {//资源空出
////                                head.actionEndTime = node.actionEndTime;
////                                if (head.next != null) {
////                                    head.next.actionStartTime = node.actionEndTime;
////                                    head = head.next;
////                                }
////                            }
////                            clock = true;
////                        } else {
//////                            if(head.next.jzj.getDisplayName().equals(jzj.getDisplayName()))
////                            head.next.actionStartTime = head.actionEndTime;
////                            head = head.next;
////                            while (head != null && head.actionEndTime != 0)
////                                head = head.next;
////                            if (head.jzj.getDisplayName().equals(jzj.getDisplayName()))
////                                if ((tickTock - head.actionStartTime) == head.spendTime) {
////                                    head.actionEndTime = tickTock;
////                                    if (head.next != null) {
////                                        head = head.next;
////                                        head.actionStartTime = tickTock;
////                                        clock = true;
////                                    }
////                                } else {
////
////                                }
////                            clock = true;
////                        }
////                        checkNodeTime(head, jzj,tickTock);
//                        if (head.actionEndTime == 0) clock = true;
//                    }
//                }
//
//                if (!clock) totalSpendTime = tickTock;
//                tickTock++;
//            }
//            Log.v("schema" + id + " totalSpendTime=", totalSpendTime + "");
//            return totalSpendTime;
//        }

        private void checkNodeTime(ZWNode head, JZJ jzj, float tickTock) {
            if (head.actionEndTime == 0) {//先查询资源是否已空出
                ZWNode node = jzjZWNodeMap.get(jzj);
                while (node != null && !node.station.getDisplayName().equals(head.station.getDisplayName())) {
                    node = node.next;
                }
                if (node.actionEndTime != 0) {//资源空出
                    head.actionEndTime = node.actionEndTime;
                    if (head.next != null) {
                        head.next.actionStartTime = node.actionEndTime;
                        head = head.next;
                    }
                }
//                clock = true;
            } else {
//                            if(head.next.jzj.getDisplayName().equals(jzj.getDisplayName()))
                head.next.actionStartTime = head.actionEndTime;
                head = head.next;
                while (head != null && head.actionEndTime != 0)
                    head = head.next;
                if (head.jzj.getDisplayName().equals(jzj.getDisplayName()))
                    if ((tickTock - head.actionStartTime) == head.spendTime) {
                        head.actionEndTime = tickTock;
                        if (head.next != null) {
                            head = head.next;
                            head.actionStartTime = tickTock;
//                            clock = true;
                        }
                    } else {
                        checkNodeTime(head, jzj,tickTock);
                    }
//                clock = true;
            }

        }
    }

    class ZWNode{
        private JZJ jzj;
        private Station station;
        private ZWNode next;
        private float spendTime;//完成所任务花费时间
        private float actionStartTime;//开始任务的时间节点
        private float actionEndTime;//完成任务的时间节点


        public ZWNode(JZJ jzj){
            this.jzj=jzj;
            this.actionStartTime=0;
            this.actionEndTime=0;

        }
        public ZWNode(JZJ jzj,Station station){
            this.jzj=jzj;
            this.station=station;
            this.actionStartTime=0;
            this.actionEndTime=0;

        }


    }
}
