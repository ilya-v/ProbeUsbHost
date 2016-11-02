package com.probe.usb.test;

import com.google.common.eventbus.Subscribe;
import com.probe.usb.host.bus.Bus;
import com.probe.usb.host.bus.Receiver;
import com.probe.usb.host.bus.UiCommand;

import javax.swing.*;

public class BusTest {

    static int n = 0;

    static public class Event {//implements UiCommand{
        String from;
        int i;
    }


    static public class AReceiver extends Receiver {

        public AReceiver() {
            System.out.println("AReceiver ctor");
        }

        @Subscribe
        void receive(Event e) {
            System.out.println("Trecv:" + Thread.currentThread().getName() + ":  "
                    + this.getClass().getName() + ": "  + e.i + "; " + e.from);
        }
    }

    static public class BReceiver extends Receiver {
        @Subscribe
        void receive(UiCommand e) {
            System.out.println("Trecv:" + Thread.currentThread().getName() + ":  "
                    + this.getClass().getName());
        }

    }

    static public class Sender {
        public void send() {
            Event e = new Event();
            e.from = "Tfrom: " + Thread.currentThread().getName();
            e.i = n;
            n++;
            Bus.post(e);
        }
    }

    public static void main(String [] args) {

        JFrame frame = new JFrame();
        frame.setVisible(true);

        Sender sender = new Sender();
        AReceiver receiver = new AReceiver();
        BReceiver r2 = new BReceiver();

        SwingUtilities.invokeLater(() -> new Sender().send());


        sender.send();
        //sender.send();
    }

}
