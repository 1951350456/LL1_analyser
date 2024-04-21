import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
public class ll1_analyzer extends JFrame {
    JSplitPane splitPane;
    JSplitPane splitPane1;
    JTextArea  textArea;
    JTable table;
    JScrollPane scrollPane;
    DefaultTableModel dtm;
    JButton button1;
    JButton button2;
    JPanel panel1;
    public Character[] VtList = {'+','-','*','/','(',')','i','#'};
    public Character[] VnList = {'E','G','T','S','F'};
    public Map<Character,Integer> VtMap = new HashMap<>();
    public Map<Character,Integer> VnMap = new HashMap<>();
    public Stack<Character> SigStack;
    public Stack<Character> InputStack;
    public String[][] AnaList;
    public String TextInput;
    public Integer Step;
    public Vector<Printer> printer = new Vector<>();
    //构造函数
    public ll1_analyzer(){
        //初始化分析表
        AnaList = new String[VnList.length][VtList.length];
        for(int i=0; i<VnList.length; i++){
            for(int j=0; j<VtList.length; j++){
                AnaList[i][j]=null;
            }
        }
        AnaList[0][4]="TG";
        AnaList[0][6]="TG";
        AnaList[1][0]="+TG";
        AnaList[1][1]="-TG";
        AnaList[1][5]="$";
        AnaList[1][7]="$";
        AnaList[2][4]="FS";
        AnaList[2][6]="FS";
        AnaList[3][0]="$";
        AnaList[3][1]="$";
        AnaList[3][2]="*FS";
        AnaList[3][3]="/FS";
        AnaList[3][5]="$";
        AnaList[3][7]="$";
        AnaList[4][4]="(E)";
        AnaList[4][6]="i";
        for (int i=0;i<VtList.length;i++) {
            VtMap.put(VtList[i],i);
        }
        for(int i=0;i<VnList.length;i++) {
            VnMap.put(VnList[i],i);
        }


        setTitle("LL(1)分析");
        setBounds(500,400,1000,700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        //输入区域
        textArea = new JTextArea();
        splitPane1.setTopComponent(textArea);
        splitPane1.setDividerLocation(200);
        textArea.setFont(new Font("宋体",Font.BOLD,20));

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
            //输入非终结符
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
        ll1_analyzer a= new ll1_analyzer();
        a.setVisible(true);
    }
}
record Printer(Integer step,String anlStack,String lftInp,String producer,String action){}
