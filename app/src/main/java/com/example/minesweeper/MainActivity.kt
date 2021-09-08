package com.example.minesweeper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_play_game.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val customBoard : Button = findViewById(R.id.customBoardButton)
        val startGame : Button = findViewById(R.id.startButton)
        val radioGroup : RadioGroup = findViewById(R.id.radioGroup)

        var level  = ""
        sharedPreferences = this.getSharedPreferences("time", Context.MODE_PRIVATE)
        val btime: String? =sharedPreferences.getString("Best","00:00")
        bt_tv.text = btime
        val ltime: String? =sharedPreferences.getString("Last","00:00")
        lt_tv.text= ltime

        radioGroup.setOnCheckedChangeListener { _, i ->
            //setting the value of level var from selected radio button
            val rb : RadioButton = findViewById(i)
            level = rb.text.toString()
        }

        startGame.setOnClickListener {
            /*when user clicks start button, on the basis of chosen level (same as text of radio button),
            we pass the data and call setBoard function */
            if (level == "") {
                Toast.makeText(this,"Choose a level to start game",Toast.LENGTH_SHORT).show()
            }
            when (level) {
                "Easy" -> setBoard(8,8,12)
                "Medium" -> setBoard(10,10,20)
                "Hard" -> setBoard(12,12,28)
            }
        }

        // what happens if user clicks custom board button
        customBoard.setOnClickListener {
            //creating an alert dialog using builder
            val builder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView : View = inflater.inflate(R.layout.activity_custom_board,null)

            val bHeight = dialogView.findViewById<EditText>(R.id.height)
            val bWidth = dialogView.findViewById<EditText>(R.id.width)
            val mCount = dialogView.findViewById<EditText>(R.id.noOfMines)

            val pBt = dialogView.findViewById<Button>(R.id.positiveButton)
            //disabling positive button of alert dialog initially
            pBt.isEnabled = false

            //checking for empty fields in dialog box and marking them as mandatory fields
            val arr = arrayOf(bHeight,bWidth,mCount)
            for (editText in  arr) {
                editText.error = if (editText.text.toString().isNotBlank()) null else "Field can't be empty!"
                editText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
//                     // if user enters all three values to enable start button but then clears a value, start button will still be enabled, this condition will prevent that.
                        if (p0.isNullOrBlank()) {
                            pBt.isEnabled = false
                        }
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        //enabling start button only if all fields are provided by user
                        if (bHeight.text.toString().isNotBlank() && bWidth.text.toString().isNotBlank()
                            && mCount.text.toString().isNotBlank()) {
                            pBt.isEnabled = true
                        }
                    }
                })
            }

            with(builder) {
                setView(dialogView)
                //after view is set, setting positive button of Alert Dialog box
                pBt.setOnClickListener {
                    // passing integer value to setBoard function
                    setBoard(bHeight.text.toString().toInt(),bWidth.text.toString().toInt(),mCount.text.toString().toInt())
                }
                // do nothing if user chooses cancel
                setNegativeButton("Cancel") {_,_->}
            }
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
        }
    }

    //necessary to avoid going back to parent activity in some cases
    override fun onBackPressed() {
        finishAffinity()
    }

    //to add custom action bar and inflating it into our layout
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_action_bar,menu) //name of menu xml file
        return true
    }

    // providing functionality to info card button on Action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_info_card -> {
                val builder  = AlertDialog.Builder(this)
                with(builder) {
                    setTitle("About Minesweeper")
                    setMessage(R.string.i_button_message)
                    setNegativeButton("Ok") {_,_->}
                }
                val ad = builder.create()
                ad.show()
            }
        }
        return true
    }


    //function to set Board and start the game. This function takes dimensions of board and mines count and then generate the board
    private fun setBoard(boardHeight: Int, boardWidth: Int, minesCount: Int) {
        val intent = Intent(this, PlayGame::class.java)
        intent.putExtra("bH", boardHeight)
        intent.putExtra("bW" , boardWidth)
        intent.putExtra("mC" , minesCount)
        startActivity(intent)
    }
}