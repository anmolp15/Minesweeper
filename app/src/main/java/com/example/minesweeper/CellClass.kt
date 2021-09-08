package com.example.minesweeper

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

/* this helped to assign properties to individual button like value, isMarked etc
 while being in loop. At the same time, this allows to assign parameters
 like id and test and background to button variable */
class CellClass : AppCompatButton {

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    )

    //value of a mine cell will be -1 always
    var value:Int = 0
    var isRevealed: Boolean = false
    var isMarked: Boolean = false
    var isMine : Boolean = false
}