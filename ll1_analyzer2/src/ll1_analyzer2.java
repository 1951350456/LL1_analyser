import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
public class ll1_analyzer2 extends JFrame {
    JDialog myDialog;
    JSplitPane splitPane;
    JSplitPane splitPane1;
    JTextArea  textArea;
    JTable table;
    JScrollPane scrollPane;
    JScrollPane scrollPane1;
    DefaultTableModel dtm;
    JButton button1;
    JButton button2;
    JButton button3;
    JPanel panel1;
    String IPT[];
    //first集合
    public static HashMap<String, HashSet<String>> first = new HashMap<>();

    //firstX集合，指任意符号串的first集合
    public static HashMap<String, HashSet<String>> firstX = new HashMap<>();

    //follower集合
    public static HashMap<String, HashSet<String>> follower = new HashMap<>();

    public Vector<Character> VtList ;
    public Vector<Character> VnList ;
    public Map<Character,Integer> VtMap = new HashMap<>();
    public Map<Character,Integer> VnMap = new HashMap<>();
    public Stack<Character> SigStack;
    public Stack<Character> InputStack;
    public String[][] AnaList;
    public String TextInput;
    public Integer Step;
    public HashMap<String,ArrayList<String>> grammarInput = new HashMap<>();
    public Vector<Printer> printer = new Vector<>();
    //构造函数
    public ll1_analyzer2(){
        setTitle("LL(1)分析");
        setBounds(500,400,1000,700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        //输入区域
        textArea = new JTextArea();
        textArea.setFont(new Font("宋体",Font.BOLD,20));
        scrollPane1 = new JScrollPane(textArea);
        splitPane1.setTopComponent(scrollPane1);
        splitPane1.setDividerLocation(200);


        //按钮区域
        panel1 = new JPanel();
        panel1.setLayout(null);
        //确认按钮
        button1 = new JButton("确认");
        button1.setBounds(500,30,100,50);
        panel1.add(button1);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                TextInput = textArea.getText().trim();
                TextInput = TextInput.replace("\\n","");
                init();
                processor();
                out();
            }
        });

        //清空按钮
        button2 = new JButton("清空");
        button2.setBounds(350,30,100,50);
        panel1.add(button2);
        splitPane1.setBottomComponent(panel1);
        splitPane.setTopComponent(splitPane1);
        button2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                textArea.setText("");
            }
        });

        //文法按钮
        button3 = new JButton("文法");
        button3.setBounds(650, 30,100,50);
        panel1.add(button3);
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VnMap = new HashMap<Character,Integer>();
                VtMap = new HashMap<Character,Integer>();
                VnList = new Vector<Character>();
                VtList = new Vector<Character>();
                MyDialog();
                int cnt1=0;
                int cnt2=0;
                for(var i:IPT){
                    if(!VnMap.containsKey(i.charAt(0))) {
                        VnMap.put(i.charAt(0), cnt1);
                        VnList.add(i.charAt(0));
                        cnt1++;
                    }
                    for(int p=3;p<i.length();p++){
                        if(i.charAt(p)>='A' && i.charAt(p)<='Z'){
                            if(!VnMap.containsKey(i.charAt(p))) {
                                VnMap.put(i.charAt(p), cnt1);
                                VnList.add(i.charAt(p));
                                cnt1++;
                            }
                        }
                        else{
                            if(!VtMap.containsKey(i.charAt(p))) {
                                VtMap.put(i.charAt(p), cnt2);
                                VtList.add(i.charAt(p));
                                cnt2++;
                            }
                        }
                    }
                }
                //求非终结符的FIRST集
                for(Character LEFT:VnList){
                    String left = String.valueOf(LEFT);
                    getFirst(left);
                }
                //求每个产生式的firstX集
                for(Character LEFT:VnList){
                    String left = String.valueOf(LEFT);
                    ArrayList<String> right = grammarInput.get(left);
                    for (String Generational : right) {
                        getFirstX(Generational);
                    }
                }
                //求非终结符的follower集
                getFollower();
                //初始化分析表
                VtMap.put('#',VtList.size());
                VtList.add('#');
                AnaList = new String[VnList.size()][VtList.size()];
                for(int i = 0; i <VnList.size(); i++) {
                    for(int j=0; j < VtList.size(); j++) {
                        AnaList[i][j] = null;
                    }
                }
                for(Character c:VnList){
                    for(String s:first.get(String.valueOf(c))){
                        String p="";
                        //若非空，从FIRST集中找
                        if(!s.equals("$")){
                            for(String G:grammarInput.get(String.valueOf(c))){
                                if(firstX.get(G).contains(s)){
                                    p+=G;
                                    break;
                                }
                            }
                            AnaList[VnMap.get(c)][VtMap.get(s.charAt(0))] = p;
                        }
                        //若为空，则从FOLLOW集中找
                        else {
                            for(String F:follower.get(String.valueOf(c))){
                               if(AnaList[VnMap.get(c)][VtMap.get(F.charAt(0))]==null){
                                   AnaList[VnMap.get(c)][VtMap.get(F.charAt(0))]="$";
                               }
                            }
                        }
                    }
                }
            }
        });

        //表格区域
        String[] columnsNames = {"步骤","分析栈","剩余输入串","所用产生式","动作"};
        dtm = new DefaultTableModel(null,columnsNames);
        table = new JTable(dtm);
        table.setFont(new Font("宋体",Font.BOLD,18));
        table.setRowHeight(20);
        scrollPane = new JScrollPane(table);
        splitPane.setBottomComponent(scrollPane);
        splitPane.setDividerLocation(300);
        add(splitPane);

        //设置输入串部分右对齐
        TableColumn column = table.getColumnModel().getColumn(2);
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.RIGHT);
        column.setCellRenderer(render);

        TableColumn column1 = table.getColumnModel().getColumn(0);
        DefaultTableCellRenderer render1 = new DefaultTableCellRenderer();
        render1.setHorizontalAlignment(SwingConstants.CENTER);
        column1.setCellRenderer(render1);

        TableColumn column3 = table.getColumnModel().getColumn(3);
        DefaultTableCellRenderer render3 = new DefaultTableCellRenderer();
        render3.setHorizontalAlignment(SwingConstants.CENTER);
        column3.setCellRenderer(render3);

        TableColumn column4 = table.getColumnModel().getColumn(4);
        DefaultTableCellRenderer render4 = new DefaultTableCellRenderer();
        render4.setHorizontalAlignment(SwingConstants.CENTER);
        column4.setCellRenderer(render4);
    }
    //初始化
    public void init(){
        if(SigStack!=null)
            SigStack.clear();
        else
            SigStack = new Stack<>();
        if(InputStack!=null)
            InputStack.clear();
        else
            InputStack = new Stack<>();
        printer.clear();
        dtm.setRowCount(0);
        InputStack.push('#');
        for(int i=TextInput.length()-1;i>=0;i--){
            InputStack.push(TextInput.charAt(i));
        }
        SigStack.push('#');
        SigStack.push('E');
        Step = 0;

        Printer p= new Printer(Step,getSigStack(),getInputStack(),"","初始化");
        printer.add(p);
    }
    //求FIRST集合
    public void getFirst(String left){
        /*
          * 如果X为终结符,First(X)=X；
         * 如果X->ε是产生式，把ε加入First(X)；
         * 如果X是非终结符，如X->YZW。从左往右扫描产生式右部，把First(Y)加入First(X)；
         * 如果First(Y)不包含ε，表示Y不可为空，便不再往后处理；如果First(Y)包含ε，表示Y可为空，则处理Z，依次类推。
         */
        //得到对应起始符的产生式的集合
        ArrayList<String> list = grammarInput.get(left);
        //判断first中是否有起始符c的键值对,若有则说明该元素first已求出，若没有，则创建一个
        HashSet<String> set;
        if (first.containsKey(left)) {
            return;
        }
        else {
            set = new HashSet<>();
        }
        //遍历所有产生式
        for (String right : list){
            if(right.equals("$")){
                set.add("$");
                break;
            }
            else{
                for(int i = 0; i < right.length(); i++){
                    //如果该符号是终结符，那么将该符号加入first中，退出循环
                    if(VtMap.containsKey(right.charAt(i))){
                        set.add(String.valueOf(right.charAt(i)));
                        break;
                    }
                    //如果该符号是非终结符，那么获取该非终结符的first将其赋给自己的first
                    if(VnMap.containsKey(right.charAt(i))){
                        //拿该终结符的first前先对其初始化
                        getFirst(String.valueOf(right.charAt(i)));
                        //得到该终结符的first集合
                        if(first.containsKey(String.valueOf(right.charAt(i)))){
                            HashSet<String> vtSet = first.get(String.valueOf(right.charAt(i)));
                            //将该非终结符的first加入自己的first集合中
                            set.addAll(vtSet);
                            //判断该非终结符的first中是否包含空
                            if(!vtSet.contains("$")){
                                //如果不包含空串，则处理下一个产生式
                                break;
                            }
                        }
                    }
                }
            }
        }
        first.put(left,set);
    }
    //任意文法符号串的FIRST集
    public void getFirstX(String c){
        /*
          构造任意文法符号串的first集，如：X->YZW；求YZW的first集
          从左往右扫描该式，加入其非空first集：把First(Y)加入First(YZW)
          若包含空串，处理下一个符号：如果First(Y)包含空串，便处理Z；不包含就退出.
          处理到尾部，即所有符号的first集都包含空串 把空串加入First(YZW)。
         */
        //判断firstX中是否有文法c的键值对,若有，则使用firstX中的hashSet，若没有，则创建一个
        HashSet<String> set;
        if (firstX.containsKey(c)) {
            return;
        } else {
            set = new HashSet<>();
        }
        int i=0;
        //follower调用过来时，第一个会有空字符，所以要跳过
        if(String.valueOf(c.charAt(0)).equals("")) {
            i=1;
        }
        while(i<c.length()){
            //如果该符号是终结符，那么将该符号加入first中,退出循环
            if(VtMap.containsKey(c.charAt(i))){
                set.add(String.valueOf(c.charAt(i)));
                break;
            }
            //如果该符号是非终结符，那么将该非终结符的first集合加入到该文法符号串的first集合中
            if(VnMap.containsKey(c.charAt(i))){
                //得到该非终结符的first集合
                HashSet<String> firstSet = first.get(String.valueOf(c.charAt(i)));
                //将first集合加入该文法的firstX中
                set.addAll(firstSet);
                //判断该非终结符的first中是否包含空
                if (!firstSet.contains("$")) {
                    //如果不包含，则退出循环
                    break;
                }

            }
            i++;
        }
        firstX.put(c,set);
    }
    //求follow集
    public void getFollower(){
        /*
         * (1)$属于FOLLOW(S)，S是开始符；
         * (2)查找输入的所有产生式，确定X后紧跟的终结符；
         * (3)如果存在A->αBβ，（α、β是任意文法符号串，A、B为非终结符），把first(β)的非空符号加入follow(B)；
         * first(β)由firstX求得
         * (4)如果存在A->αB或A->αBβ 但first(β)包含空，把follow(A)加入follow(B)。
         * 综上，有4种情况
         * 1、若A->αBaβ，（α、β是任意文法符号串，A、B为非终结符，a为终结符），则a加入follower（B）
         * 以此为例A->αBβ，（α、β是任意文法符号串，A、B为非终结符）
         * 2、若β不存在，即B是最后一个符号，则follower（A）加入到follower（B）
         * 3、若β存在，则将first（β）- 空 加入到follower（B）
         * 4、在3的基础上，若first（β）存在空元素，则将follower（A）加入到follower（B）
         */
        //A->αBβ
        HashSet<String> setA;//key的follower集合的set
        HashSet<String> setB;//B的follower集合的set
        HashSet<String> setC;//β的first集合的set
        //给起始符添加$元素
        setA = new HashSet<>();
        setA.add("#");
        follower.put(String.valueOf(VnList.get(0)), setA);
        //遍历所有产生式
        //得到所有非终结符，作为outputSet的key来拿到所有产生式
        //flag用来判断follower集合是否有变动
        boolean flag = true;
        while (flag) {
            //先将flag置为false，若下面操作中有put操作，则说明follower有变动，将flag置为true再次循环
            flag = false;
            for(Character Left:VnList){
                String left = Left.toString();
                //通过key得到产生式集合,遍历产生式集合，得到每一个产生式
                ArrayList<String> Generationals = grammarInput.get(left);
                for(String Generational:Generationals){
                    int i = Generational.length()-1;
                    //存储任意文法符号串，以方便通过firstX求得该文法符号串的first
                    String B = "";
                    while(i>=0){//倒序遍历
                        //若该元素是终结符，且前一个符号是非终结符，则将该符号加入到前一个符号的follower中
                        //A -> ...Ba...
                        if((VtMap.containsKey(Generational.charAt(i)))&&((i-1)>=0)&&VnMap.containsKey(Generational.charAt(i-1))){
                            //得到B的follower集合中的hashSet
                            setB = follower.get(String.valueOf(Generational.charAt(i-1)));
                            if(setB==null)
                                setB = new HashSet<>();
                            //如果setB中有这个元素，那么直接跳过
                            if(!setB.contains(String.valueOf(Generational.charAt(i)))){
                                //如果setB中没有这个元素，则put到follower（B）中，且表进行了更新，将flag置为true
                                setB.add(String.valueOf(Generational.charAt(i)));
                                follower.put(String.valueOf(Generational.charAt(i-1)),setB);
                                flag = true;
                            }
                            B = Generational.charAt(i-1)+B;
                            i--;
                            continue;
                        }
                        //若该元素为非终结符，且此元素为最后的一个元素
                        //A->...B
                        //则将A的follower加入到B中
                        if(i==Generational.length()-1&&VnMap.containsKey(Generational.charAt(i))){
                            //得到A，B的follower对应的hashSet
                            setA = follower.get(left);
                            if(setA==null){
                                setA = new HashSet<>();
                            }
                            setB = follower.get(String.valueOf(Generational.charAt(i)));
                            if(setB==null){
                                setB = new HashSet<>();
                            }
                            //如果steA的size=0，说明setA为空，避免空指针异常，直接跳过
                            if(setA.size()!=0){
                                //说明setA有值，遍历setA中的所有元素
                                for(String value:setA){
                                    //如果setA中的值setB中也有，则跳过，如果没有则put，且表进行了更新，将flag置为true
                                    if(!setB.contains(value)){
                                        setB.add(value);
                                        follower.put(String.valueOf(Generational.charAt(i)),setB);
                                        flag = true;
                                    }
                                }
                            }
                        }
                        //若该元素不为最后一个元素，且该元素为非终结符
                        //A->...Bβ(β为任意文法符号串
                        if(VnMap.containsKey(Generational.charAt(i))&&Generational.length()-1-i>0){
                            //将first（B）加入followerB中，得到β的first集合，得到其对应的hashSet表
                            getFirstX(B);
                            setC = firstX.get(B);
                            //得到B对应follower的hashSet表
                            setB = follower.get(String.valueOf(Generational.charAt(i)));
                            if(setB==null){
                                setB = new HashSet<>();
                            }
                            //将β的first集合的非空元素加入到followerB中
                            for(String s:setC){
                                //判断该元素是否为空，且该元素在followerB中是否存在
                                if(!s.equals("$")&&!setB.contains(s)){
                                    //若不存在该元素，则将该元素加入到followerB中，且表进行了更新，将flag置为true
                                    setB.add(s);
                                    follower.put(String.valueOf(Generational.charAt(i)),setB);
                                    flag = true;
                                }
                            }
                            //若该符号串的first中有空，则，将follower（A）加入到follower（B）中
                            if(setC.contains("$")){
                                //得到followerA的hashSet
                                setA = follower.get(left);
                                //如果setA的size=0，说明setA为空，避免空指针异常，直接跳过
                                if(setA.size()!=0){
                                    //说明setA有值，遍历setA中的所有元素
                                    for(String value:setA){
                                        //如果setA中的值setB中也有，则跳过，如果没有则put，且表进行了更新，将flag置为true
                                        if (!setB.contains(value)) {
                                            setB.add(value);
                                            follower.put(String.valueOf(Generational.charAt(i)), setB);
                                            flag = true;
                                        }
                                    }
                                }
                            }
                        }
                        //将该元素加入到B中
                        B = Generational.charAt(i) + B;
                        i--;
                    }
                }
            }
        }
    }
    //get分析栈String
    public String getSigStack(){
        Iterator value = SigStack.iterator();
        StringBuilder s = new StringBuilder();
        while(value.hasNext()){
            s.append(value.next().toString());
        }
        return s.toString();
    }
    //get剩余输入串String
    public String getInputStack(){
        Iterator value = InputStack.iterator();
        StringBuilder s = new StringBuilder();
        while(value.hasNext()){
            s.append(value.next().toString());
        }
        return s.reverse().toString();
    }
    //处理
    public void processor(){
        while(!SigStack.isEmpty()){
            Step++;
            char VNtop = SigStack.peek();
            char Iptop = InputStack.peek();
            //输入非法符号
            if(!VtMap.containsKey(Iptop)){
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,vn,vt,"ERROR!","WRONG INPUT!"));
                break;
            }
            //分析成功
            if(VNtop=='#'&&Iptop=='#'){
                SigStack.pop();
                InputStack.pop();
                printer.add(new Printer(Step,"","","","ACC"));
            }
            //弹出输入串栈
            else if(VNtop==Iptop){
                SigStack.pop();
                InputStack.pop();
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,vn,vt,"","GETNEXT(I)"));
            }
            //分析表中无可归约项
            else if(AnaList[VnMap.get(VNtop)][VtMap.get(Iptop)]==null){
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,vn,vt,"ERROR!","ERROR!"));
                break;
            }
            //空
            else if(AnaList[VnMap.get(VNtop)][VtMap.get(Iptop)].equals("$")){
                SigStack.pop();
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,vn,vt,VNtop+"->"+"$","POP"));
            }
            //正常归约
            else if(AnaList[VnMap.get(VNtop)][VtMap.get(Iptop)]!=null){
                SigStack.pop();
                char[] temp =  AnaList[VnMap.get(VNtop)][VtMap.get(Iptop)].toCharArray();
                for(int i = temp.length-1;i>=0;i--)
                    SigStack.push(temp[i]);
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,vn,vt,VNtop+"->"+AnaList[VnMap.get(VNtop)][VtMap.get(Iptop)],"POP,PUSH("+AnaList[VnMap.get(VNtop)][VtMap.get(Iptop)]+")"));
            }
            //其他错误情况
            else{
                String vn = getSigStack();
                String vt = getInputStack();
                printer.add(new Printer(Step,vn,vt,"ERROR!","ERROR!"));
                break;
            }

        }
    }
    //制作表格
    public void out(){
        for(var i:printer){
            Object[]o={i.step(),i.anlStack(),i.lftInp(),i.producer(),i.action()};
            dtm.addRow(o);
        }
    }
    //主程序
    public static void main(String[] args) {
        ll1_analyzer2 a= new ll1_analyzer2();
        a.setVisible(true);
    }
    public void MyDialog() {
        myDialog = new JDialog(ll1_analyzer2.this,"文法输入",true);
        Container container=myDialog.getContentPane();
        myDialog.setBounds(500,200,400,700);
        JSplitPane j1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane j2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        j1.setBottomComponent(j2);
        JPanel jPanel = new JPanel();
        JLabel lable_ = new JLabel("请输入文法，每个文法一行：");
        lable_.setBounds(10,10,100,50);
        jPanel.add(lable_);
        j1.setTopComponent(jPanel);
        j1.setDividerLocation(50);
        JTextArea textArea_ = new JTextArea();
        textArea_.setFont(new Font("宋体",Font.BOLD,18));
        JScrollPane s = new JScrollPane(textArea_);
        j2.setTopComponent(s);
        JButton btn_ = new JButton("确定");
        btn_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IPT = textArea_.getText().replace(" ","").split("\\n");
                myDialog.dispose();
                Vector<String> temp = new Vector<String>();
                for(var i:IPT){
                    if(i.contains("|")){
                        String[] b  = i.split("\\|");
                        for(int j=1;j<b.length;j++){
                            b[j] = b[0].charAt(0)+"->"+b[j];
                        }
                        temp.addAll(Arrays.asList(b));
                    }
                    else
                        temp.add(i);
                }
                IPT = new String[temp.size()];
                for(var i=0;i<temp.size();i++){
                    IPT[i] = temp.get(i);
                }
                for(var s:IPT){
                    String[] str = s.split("->");//通过“->”分隔文法
                    String left = str[0];
                    //判断outputSet中是否有左部，若有，通过get方法得到，若没有，新建一个ArrayList
                    ArrayList<String> list;
                    if (grammarInput.containsKey(left)) {
                        list = grammarInput.get(left);
                    } else {
                        list = new ArrayList<>();
                    }
                    list.add(str[1]);
                    grammarInput.put(left, list);
                }
            }
        });
        JPanel p = new JPanel();
        p.setLayout(null);
        btn_.setBounds(150,25,100,50);
        p.add(btn_);
        j2.setBottomComponent(p);
        j2.setDividerLocation(450);
        container.add(j1);
        myDialog.setVisible(true);
    }

}

record Printer(Integer step,String anlStack,String lftInp,String producer,String action){}
/*
E->TG
G->+TG|-TG
G->$
T->FS
S->*FS|/FS
S->$
F->(E)
F->i
 */