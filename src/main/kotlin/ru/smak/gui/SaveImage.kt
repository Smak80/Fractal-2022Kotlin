package ru.smak.gui
import ru.smak.graphics.FractalPainter
import ru.smak.graphics.Plane
import java.awt.Color
import java.awt.Component
import java.awt.Image
import java.awt.Panel
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

const val infoHeight = 100
fun preparImg(img:Image,mainPanel:GraphicsPanel,plane:Plane) {
    val ig = img.graphics
    ig.color = Color.WHITE
    ig.fillRect(0, mainPanel.height, mainPanel.width, infoHeight)
    mainPanel.paint(ig)
    ig.color = Color.BLACK
    ig.drawString("xMin =${plane.xMax} xMax =${plane.xMax} yMin = ${plane.yMin} yMax = ${plane.yMax}",  30,mainPanel.height + 40)
}
class SaveImage(val image : BufferedImage) : ActionListener {

    override fun actionPerformed(e: ActionEvent?) {

        val fileChooser = JFileChooser().apply {
            isAcceptAllFileFilterUsed = false

        }
        val filterList = mutableListOf<FileNameExtensionFilter>()
        filterList.add(FileNameExtensionFilter("Format jpg", "JPG", "JPEG"))
        filterList.add(FileNameExtensionFilter("Format png", "PNG"))
        filterList.forEach{v->fileChooser.fileFilter = v}
        val result: Int = fileChooser.showSaveDialog(null)
        var saveFile: File? = fileChooser.selectedFile
        var isExt:String? = null
        val filters =fileChooser.choosableFileFilters
        if(saveFile !=null)
            for (i in 0 until filterList.size)
            {
                filterList[i].extensions.forEach {
                    saveFile?.let {v->
                        if (v.name.lowercase().endsWith('.' + it.lowercase()))
                            isExt = it.lowercase()
                    }
                }
            }
        if (isExt == null && saveFile!=null) {
            saveFile = File("$saveFile.jpg")
            isExt="jpg"
        }
        if (result == JFileChooser.APPROVE_OPTION){
            try {
                ImageIO.write(image,isExt, saveFile)
                println("Saved successful ! ")
            } catch (e: Exception){
                return
            }
        }
    }

}



