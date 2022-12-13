package ru.smak

import ru.smak.gui.MainWindow


fun main(args: Array<String>) {
    val w = MainWindow().apply { isVisible = true };

//    val screen = w.getScreenShot(800,600);
//
//    VideoCreator.createVideo(
//        "video.mp4",
//        listOf(
//            screen,
//        )
//    );
//
//    val frame = JFrame("panel demo")
//    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//
//    val panel = JPanel()
//    val mainPanel = JPanel();
//
//    frame.layout = GroupLayout(frame.contentPane).apply {
//        setVerticalGroup(
//            createSequentialGroup()
//                .addComponent(mainPanel)
//        )
//        setHorizontalGroup(
//            createSequentialGroup()
//                .addComponent(mainPanel)
//        )
//    };
//
//    mainPanel.add(panel);
//    panel.setSize(100, 100)
//    panel.layout = GridLayout(1000, 1)
//    for (i in 0..999) panel.add(JLabel("JLabel $i"))
//
//    val jsp = JScrollPane(panel)
//    mainPanel.add(jsp)
//
//    frame.setSize(100, 100)
//    frame.isVisible = true

}

