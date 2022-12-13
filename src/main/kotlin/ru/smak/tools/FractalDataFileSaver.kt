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
            dialogTitle = "Сохранение данных фрактала в файл"
            fileFilter = FileNameExtensionFilter("dat", "dat")
        }
        fileChooser.fileSelectionMode = JFileChooser.OPEN_DIALOG
        val openDialogResult = fileChooser.showSaveDialog(fileChooser)
        if (openDialogResult == JFileChooser.APPROVE_OPTION) {
            var fileAbsolutePath = fileChooser.selectedFile.absolutePath
            if (fileChooser.selectedFile.extension.isEmpty()) {
                if (fileChooser.fileFilter.description != "All Files") {
                    fileAbsolutePath += "." + fileChooser.fileFilter.description
                } else {
                    fileAbsolutePath += ".dat"
                }
            }
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