package ru.smak.video.operations

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class ProgressWindowOperations() : MouseAdapter() {


    override fun mouseClicked(e: MouseEvent?) {
        if (e == null) return;

        when (e.component.name) {

            "CancelButton" -> onCacelButtonClick()

            else -> {
                throw IllegalArgumentException("Cannot recognize button name")
            }
        }
    }

    private fun onCacelButtonClick() {
        // todo: cancel video creation
    }

}