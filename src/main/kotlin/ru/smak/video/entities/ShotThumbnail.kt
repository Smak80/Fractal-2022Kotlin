package ru.smak.video.entities

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JPanel

class ShotThumbnail(val shot: Shot) : JPanel() {

    var isSelected = false

    init {
        size = Dimension(shot.thumbnailImage.width,shot.thumbnailImage.height)
        minimumSize = Dimension(shot.thumbnailImage.width,shot.thumbnailImage.height)
        preferredSize = Dimension(shot.thumbnailImage.width,shot.thumbnailImage.height)
        background = Color.RED
        setupEventListeners()
    }

    fun select()
    {
        isSelected = true
        border = BorderFactory.createLineBorder(Color.RED,2)
    }

    fun unselect()
    {
        isSelected = false
        border = null
    }

    private fun setupEventListeners() {
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                e?.apply {
                    if (button == 1) {
                        if(!isSelected) select()
                        else unselect()
                        repaint()
                    }
                }

            }
        })
    }

    override fun paintComponent(g: Graphics?) {
        g?.drawImage(shot.thumbnailImage, 0, 0, this)
    }
}