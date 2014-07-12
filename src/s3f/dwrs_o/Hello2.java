/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.dwrs_o;

import s3f.core.plugin.Data;
import s3f.core.plugin.Plugabble;

/**
 *
 * @author anderson
 */
public class Hello2 implements Plugabble {

    private final Data data;

    public Hello2() {
        data = new Data("hello2", "s3f.dwrs", "Hello2");
        data.setProperty("teste", TmpClass.class);
    }

    @Override
    public void init() {
        System.out.println("init");
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public Plugabble createInstance() {
        return new Hello2();
    }

}
