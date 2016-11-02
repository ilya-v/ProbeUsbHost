package com.probe.usb.test;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.AbstractMap;

public class GuavaTest {

    @Test
    public void guavaTest() {

        AbstractMap.SimpleEntry<Integer, String> e = new AbstractMap.SimpleEntry<>(10, "aaa");

        class T extends AbstractMap.SimpleEntry<Integer, String> {

            public T(Integer key, String value) {
                super(key, value);
            }
        }

        T t = new T(100,"aaaa");
        t.getKey();
        t.getValue();

        class Q extends AbstractMap.SimpleImmutableEntry<Integer, String> {

            public Q(Integer key, String value) {
                super(key, value);
            }
        }

        Q q = new Q(10, "a");

        ImmutableMap<Integer, String> imap2 =  ImmutableMap.of(10, "aaa");
        ImmutableMap<Long, Double> imap3 = ImmutableMap.of(10L, 1.0);

        //Assert.assertTrue(imap1 instanceof imap2);

    }
}
