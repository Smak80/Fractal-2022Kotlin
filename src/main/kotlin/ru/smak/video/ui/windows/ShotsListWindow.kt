package ru.smak.video.ui.windows

import kotlinx.coroutines.DisposableHandle
import ru.smak.video.entities.Shot
import ru.smak.video.entities.ShotThumbnail
import ru.smak.video.operations.ShotsThumbnailsWindowOperations
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*


class ShotsListWindow(val mainComponent: VideoWindow) : JFrame(), DisposableHandle {

    private val _minSize = Dimension(355, 410);
    var thumbnails = mutableListOf<ShotThumbnail>();

    val mainPanel = JPanel();
    val thumbsPanel = JPanel();
    val controlPanel = JPanel();

    private val _deleteBtn = JButton("Delete").apply {
        isFocusPainted = false;
        isContentAreaFilled = false;
    };

    companion object {
        val SHRINK = GroupLayout.PREFERRED_SIZE;
        val GROW = GroupLayout.DEFAULT_SIZE;
    }


    init {
        size = _minSize;
        isResizable = false;

        configureNames()
        setupLayout();
        setupScrollbar();
        setupEventListeners();

        setLocationRelativeTo(mainComponent);
    }

    private fun configureNames() {
        _deleteBtn.name = "DeleteShotButton"
    }

    private fun setupEventListeners() {
        val shotsThumbnailsWindowOperations = ShotsThumbnailsWindowOperations(
            mainComponent,
            this
        )

        _deleteBtn.addMouseListener(shotsThumbnailsWindowOperations)
    }

    private fun setupScrollbar() { // todo

        val jsp = JScrollPane(thumbsPanel)
            .apply{setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);};

        mainPanel.add(jsp);
        add(mainPanel)
    }

    private fun setupLayout() {

        thumbsPanel.layout = FlowLayout(0, 10, 10);
        controlPanel.apply {
            layout = FlowLayout(0);
            add(_deleteBtn)
        };

        mainPanel.layout = GroupLayout(mainPanel).apply {
            setVerticalGroup(
                createSequentialGroup()
                    .addComponent(thumbsPanel, 340, 340, 340)
                    .addComponent(controlPanel, SHRINK, SHRINK, SHRINK)
            )

            setHorizontalGroup(
                createParallelGroup()
                    .addComponent(thumbsPanel, 355, 355, 355)
                    .addComponent(controlPanel, GROW, GROW, GROW)
            )
        }
    }

    fun updateThumbnails(shots: MutableList<Shot>) {
        clearThumbs();
        thumbnails = shots.map { ShotThumbnail(it) }.toMutableList();
        setupComponents();
    }

    private fun setupComponents() {
        for (thumb in thumbnails)
            thumbsPanel.add(thumb);
    }

    private fun clearThumbs() {
        for (thumb in thumbnails)
            thumbsPanel.remove(thumb);
    }

    override fun dispose() {
        for (thumb in thumbnails)
            thumbsPanel.remove(thumb);

        thumbnails.clear();
    }

}