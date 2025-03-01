package com.ttxp.demo;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HTMLPanelExample {
    public static void main(String[] args) {
        // 在事件调度线程中创建和操作 GUI 组件
        SwingUtilities.invokeLater(() -> {
            // 创建主窗口
            JFrame frame = new JFrame("说明");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);

            // 创建一个 JEditorPane 用于显示 HTML 内容
            JEditorPane editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            try {
                // 使用 ClassLoader 读取本地资源文件
                java.net.URL url = HTMLPanelExample.class.getResource("/static/About.html");
                if (url != null) {
                    editorPane.setPage(url);
                } else {
                    System.err.println("未找到 About.html 文件。");
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            // 设置要显示的 HTML 内容
            editorPane.setEditable(false); // 禁止用户编辑内容

            // 为 JEditorPane 添加超链接监听器
            editorPane.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            // 获取链接的 URI
                            URI uri = e.getURL().toURI();
                            // 检查 Desktop 是否支持打开浏览器
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                // 打开默认浏览器并访问链接
                                Desktop.getDesktop().browse(uri);
                            }
                        } catch (URISyntaxException | IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            // 创建一个滚动面板，将 JEditorPane 放入其中
            JScrollPane scrollPane = new JScrollPane(editorPane);

            // 将滚动面板添加到主窗口的内容面板
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

            // 设置窗口居中显示
            frame.setLocationRelativeTo(null);

            // 显示主窗口
            frame.setVisible(true);
        });
    }
}
