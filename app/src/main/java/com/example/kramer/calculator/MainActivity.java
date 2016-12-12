package com.example.kramer.calculator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String mid = null;
    TextView textView_Outlet = null;//主textView，用于记录当次输入及结果
    TextView textView_Outlet0 = null;//用于记录当次计算的表达式
    TextView textView_Outlet1 = null;//用于Binary/Hex的转换结果
    //操作符栈
    static LinkedList<String> opStack=new LinkedList<>();
    //优先级映射
    static Map<String, Integer> priority=new HashMap<String, Integer>(){
        {
            put("+",0);
            put("-",0);
            put("×",1);
            put("÷",1);
        }
    };

    public void init(){
        textView_Outlet=(TextView) findViewById(R.id.textView_Outlet);
        textView_Outlet.setTextSize(55);
        textView_Outlet0 = (TextView)findViewById(R.id.textView_Outlet0);
        textView_Outlet0.setTextSize(30);
        textView_Outlet1 = (TextView)findViewById(R.id.textView_Outlet1);
        textView_Outlet1.setTextSize(20);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    //每个字符输入后显示在Text中
    public void ShowText(View view){
        Button bt = (Button)view;
        String s = (String)bt.getText();
        String text = (String) textView_Outlet.getText();
        textView_Outlet.setText(text + s);
    }
    //运算符不能重复输入
    public void ShowTextOperator(View view){
        String text = (String)textView_Outlet.getText();
        String s = "";
        Button bt = (Button)view;
        String s1 = (String)bt.getText();
        //输入框为空时不允许输入除了减号（"-"）操作符以外的操作符
        if((text==null || text.length()==0) && s1.equals("-")){
            ShowText(view);
            return;
        }
        if(text==null || text.length() == 0) return;
        //如果输入框最后一个是一个操作符或者小数点(".")，继续输入操作符会覆盖掉上一个操作符
        if(text != null && text.length() != 0)
            s = text.substring(text.length()-1,text.length());
        if (s.equals("+") || s.equals("-") || s.equals("×") || s.equals("÷")||s.equals
                (".")){
            textView_Outlet.setText(text.substring(0,text.length()-1));
            ShowText(view);
            return;
        }
        ShowText(view);
    }



    //浮点数只能有一个小数点
    public void ShowTextPoint(View view){
        String text = (String) textView_Outlet.getText();
        String s;

        //Outlet为空时输入小数点，默认在前面加"0"
        if(text == null || text.length() == 0){
            textView_Outlet.setText(text+"0");
            ShowText(view);
            return;
        }
        if(text != null && text.length() != 0) {
            s = goToSplit(text)[goToSplit(text).length-1];
            for (int i = 0; i < s.length(); i++){
                //Outlet最末一个数字中已经小数点则不能在输入
                if (s.charAt(i) == '.')
                    return;
                    //Outlet中最末一位是操作符，则默认在输入 "0."
                else if(s.charAt(i) == '+' ||s.charAt(i) == '-' ||s.charAt(i) == '×' ||
                        s.charAt(i) == '÷' ){
                    textView_Outlet.setText(text+"0");
                    ShowText(view);
                    return;
                }
            }
        }
        ShowText(view);
    }
    //del退格键，删除最后一个字符
    public void delete(View view){
        String text = (String)textView_Outlet.getText();
        String text0 = (String)textView_Outlet0.getText();
        //仅删除最末一位
        if(text != null && text.length()!= 0){
            text = text.substring(0,textView_Outlet.length()-1);
            textView_Outlet.setText(text);
        }else if((text == null || text.length() == 0) && (text0 != null && text0.length() !=
                0)){
            //如Outlet已为null，则将上次计算的表达式退回到Outlet
            textView_Outlet.setText(text0);
        }
    }
    //清空Text中所有的内容
    public void clear(View view){
        textView_Outlet.setText("");
        //textView_Outlet0.setText("");
        textView_Outlet1.setText("");
    }
    //计算出结果
    public void calc(View view){
        mid = (String) textView_Outlet.getText();
        textView_Outlet0.setText(mid);
        //如首位为减号"-"，将其作为负号
        if(mid.charAt(0)== '-')
            mid = "0"+mid;
        //将Outlet中的String分段，分为数字和操作符
        String[] midSplit = goToSplit(mid);
        String s ;
        Double ans = 0.0;

        try {
            //将分段的midSplit按照优先级排序存入List中
            List<String> after = midToAfter(midSplit);
            //将最后结果保存在answer中
            ans = afterValue(after);
            //限制结果精确度
            if(ans.toString().length() > 11)
                s = ans.toString().substring(0,11);
            else
                s = ans.toString();
            textView_Outlet.setText(s);
        } catch (Exception e) {
            Toast.makeText(this, "输入不合法，请检查", Toast.LENGTH_LONG).show();
        }
    }
    //分段操作，将Outlet中String的数字和操作符分开
    public String[] goToSplit(String s){
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i < s.length();i++){
            if(s.charAt(i) != '.' && (s.charAt(i) < '0' || s.charAt(i) > '9')){
                sb.append("#"+s.charAt(i)+"#"); //操作符
            }else{
                sb.append(s.charAt(i));     //浮点数和多位数
            }
        }
        String[] split = sb.toString().split("#");
        return split;
    }

    //按优先级排序
    public static List<String> midToAfter(String [] mid) throws Exception{
        LinkedList<String> after = new LinkedList<>();
        for(String ss:mid){
            if(priority.get(ss) == null){   //说明是操作数
                after.add(ss);
            }else {
                while(!opStack.isEmpty() && priority.get(ss) <= priority.get(opStack.peek()))
                    after.add(opStack.pop());
                opStack.push(ss);
            }
        }
        while(!opStack.isEmpty())
            after.add(opStack.pop());
        return after;
    }
    //计算，将最后结果放入栈顶
    public static double afterValue(List<String> after) throws Exception{
        LinkedList<Double> number = new LinkedList<>();
        for(String ss:after){
            if(priority.get(ss) != null){//是操作符,取出两个数，按操作符计算后入数字栈
                Double y = number.pop();
                Double x = number.pop();
                if(ss.equals("+")) number.push(x+y);
                else if(ss.equals("-")) number.push(x-y);
                else if(ss.equals("×")) number.push(x*y);
                else if(ss.equals("÷")) number.push(x/y);
            }else{
                number.push(Double.valueOf(ss));
            }
        }
        return number.pop();
    }

    int num = 0;
    //将Outlet中的结果转换为Binary or Hex or Oct
    public void Conversion(View view) {
        num++;
        String text = (String) textView_Outlet.getText();
        //为null不转换
        if(text == null || text.length() == 0) return;
        String text1, text2;
        for (int i = 0; i < text.length(); i++) {
            //含有操作符，不转换
            if(text.charAt(i) == '+' ||text.charAt(i) == '-' ||text.charAt(i) == '×' ||
                    text.charAt(i) == '÷' ){return;}
            //找到小数点，整数部分和小数部分分别转换
            else if (text.charAt(i) == '.') {
                text1 = text.substring(0, i);//整数部分存入text1
                text2 = text.substring(i+1, text.length());//小数部分存入text2
                if(num%3 == 1) {
                    textView_Outlet1.setText("[Bin]"+Converse(text1, text2, 2));return;
                }else if(num%3 == 0){
                    textView_Outlet1.setText("[Hex]"+Converse(text1, text2, 16));return;
                }else if(num%3 == 2){
                    textView_Outlet1.setText("[Oct]"+Converse(text1, text2, 8));return;
                }
            }
        }
        //Outlet为整数，使用库函数直接转换
        if(num%3 == 1) {
            textView_Outlet1.setText("[Bin]"+Integer.toBinaryString(Integer.valueOf(text)));
        }else if(num%3 == 0){
            textView_Outlet1.setText("[Hex]"+Integer.toHexString(Integer.valueOf(text)));
        }else if(num%3 == 2){
            textView_Outlet1.setText("[Oct]"+Integer.toOctalString(Integer.valueOf(text)));
        }
    }
    public String Converse(String s1,String s2,int n){
        String temp1 = "",temp2 = "0." +s2;
        double a = Double.valueOf(temp2);
        temp2 = "";//小数部分的转换结果存入text2
        //Binary
        if(n == 2) temp1 = Integer.toBinaryString(Integer.valueOf(s1));
            //Hex
        else if(n == 16) temp1 = Integer.toHexString(Integer.valueOf(s1));
            //Oct
        else if(n == 8) temp1 = Integer.toOctalString(Integer.valueOf(s1));
        //小数部分
        int opp = 0;int number;
        while(a != 0 && opp <= 20){
            number = (int)(a*n);
            if(number < 10 && number >= 0){
                temp2 += number;
            }else if(number >= 10){
                switch (number){
                    case 15:{temp2 += "f";break;}
                    case 14:{temp2 += "e";break;}
                    case 13:{temp2 += "d";break;}
                    case 12:{temp2 += "c";break;}
                    case 11:{temp2 += "b";break;}
                    case 10:{temp2 += "a";break;}
                    default:break;
                }
            }
            a = n*a-number;
            opp++;
        }
        if(temp2.equals("0")) return temp1;
        else{
            if(temp1.length()+temp2.length() > 18)
                temp2 = temp2.substring(0,18-temp1.length());
            return temp1+"."+temp2;
        }
    }
}

