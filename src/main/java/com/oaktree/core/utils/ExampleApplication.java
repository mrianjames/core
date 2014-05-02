package com.oaktree.core.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ExampleApplication {
	JFrame frame;
    
    public static void main(String[] args) throws Exception {
        ExampleApplication db = new ExampleApplication();
    }

    public ExampleApplication(){
        frame = new JFrame("Show Message Dialog");
        System.out.println("Test stdout");
        System.err.println("Test stderr");
        System.out.flush();
        System.err.flush();
        JButton button = new JButton("Click Me");
        button.addActionListener(new MyAction());
        frame.add(button);
        frame.setSize(400, 400);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public class MyAction implements ActionListener {
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(frame,"oaktree designs. A member of team GB. ");
        }
    }
}
