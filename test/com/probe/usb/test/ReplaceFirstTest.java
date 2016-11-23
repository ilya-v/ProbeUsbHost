package com.probe.usb.test;

import org.junit.Test;


public class ReplaceFirstTest {


    @Test
    public void replaceFirstTest() {

        String
                s = "C:\\Users\\user\\file.txt",
                d = "C:\\Users\\user\\",
                fname = s;

        if (s.startsWith(d))
            fname = s.substring(d.length());

        System.out.println(fname);
    }
}
