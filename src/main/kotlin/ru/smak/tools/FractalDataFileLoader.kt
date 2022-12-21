package ru.smak.tools

import java.io.EOFException
import java.io.FileInputStream
import java.io.ObjectInputStream
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

object FractalDataFileLoader {
    fun loadData() : FractalData? {
        val fileChooser = JFileChooser()
        with(fileChooser) {
            dialogTitle = "Открытие фрактала"
            fileFilter = FileNameExtensionFilter(
                "Состояние фрактала с возможностью восттановления",
                "fractal")
            isAcceptAllFileFilterUsed = false
            fileSelectionMode = JFileChooser.OPEN_DIALOG
        }
        val openDialogResult = fileChooser.showOpenDialog(fileChooser)
        if (openDialogResult == JFileChooser.APPROVE_OPTION) {
            val fileAbsolutePath = fileChooser.selectedFile.absolutePath
            val loadResult = load(fileAbsolutePath)
            if (loadResult != null) {
                return loadResult
            } else {
                JOptionPane.showMessageDialog(fileChooser, "Возникла ошибка")
                return null
            }
        } else {
            return null
        }
    }
    
    private fun load(filePath: String) : FractalData? {
        val fileStream = FileInputStream(filePath)
        val objectStream = ObjectInputStream(fileStream)
        while (true) {
            try {
                return objectStream.readObject() as? FractalData
            } catch (e: EOFException) {
                break
            }
        }
        return null
    }
}