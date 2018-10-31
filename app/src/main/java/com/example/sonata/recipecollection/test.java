package com.example.sonata.recipecollection;

public class test {

    public static void main(String[] args)
    {
//        String str = "玫瑰腐乳,适量;盐,适量;八角,适量;草果,适量;香叶,适量;料酒,适量;米醋,适量;生姜,适量";
//        StringToArray(str);
    }

    public void StringToArray(String test)
    {
        String[] sent = test.split(";");
        System.out.print(sent.length);

    }
}
