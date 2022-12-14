package ru.smak.tools

import java.io.FileOutputStream
import java.io.ObjectOutputStream
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class FractalDataFileSaver(data: FractalData) {
    init {
        val fileChooser = JFileChooser()
        with(fileChooser) {
            dialogTitle = "Сохранение состояния фрактала"
            fileFilter = FileNameExtensionFilter(
                "Состояние фрактала с возможностью восттановления", 
                "fractal")
            isAcceptAllFileFilterUsed = false
            fileSelectionMode = JFileChooser.OPEN_DIALOG
        }
        val openDialogResult = fileChooser.showSaveDialog(fileChooser)
        if (openDialogResult == JFileChooser.APPROVE_OPTION) {
            val fileAbsolutePath = fileChooser.currentDirectory.absolutePath + 
                    "\\" + 
                    fileChooser.selectedFile.nameWithoutExtension + 
                    ".fractal"
            save(fileAbsolutePath, data)
            JOptionPane.showMessageDialog(fileChooser, "Файл '$fileAbsolutePath' успешно сохранен")
        }
    }
    
    private fun save(filePath: String, data: FractalData) {
        val fileStream = FileOutputStream(filePath)
        val objectStream = ObjectOutputStream(fileStream)
        objectStream.writeObject(data)
        objectStream.close()
    }
}