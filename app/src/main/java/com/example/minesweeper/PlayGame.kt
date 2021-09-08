package com.example.minesweeper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_play_game.*
import kotlinx.android.synthetic.main.status_won.*
import kotlinx.android.synthetic.main.status_won.view.*
import kotlin.properties.Delegates
import kotlin.random.Random

const val MINE = -1
class PlayGame : AppCompatActivity() {

    var flaggedMines = 0
    var fastestTime = "NA"
    var lastGameTime = "NA"
    var pauseAt : Long = 0
    var status  = Status.ONGOING

    /*so that variables can be used outside scope of onCreate function as well
         at the same time assigning them values carried from previous activity*/
    var bHeight by Delegates.notNull<Int>() // similar to lateinit
    var bWidth by Delegates.notNull<Int>()
    var mCount by Delegates.notNull<Int>()



    // variable used when user want to make a button as a flag or open it //
    // choice  = 1 for opening cell and choice  = 2 for marking it as flag
    var playersChoice  = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_game)

        bHeight = intent.getIntExtra("bH",0) //bHeight = no of rows but size of each row = bWidth
        bWidth = intent.getIntExtra("bW",0) //bWidth = no of columns but size of each column = bHeight
        mCount = intent.getIntExtra("mC",0)

        // calling setUpBoardStartGame() to setup board
        setUpBoardStartGame()

        //setting default image button icon
        flagButton.setImageResource(R.drawable.uncolored_flag)

        //starting timer as soon as activity starts
        chronometer.base = SystemClock.elapsedRealtime() - pauseAt
        chronometer.start()

        //what happens if user clicks restart button
        restartButton.setOnClickListener {
            restartGame()
        }

        // will have to use ImageButton to change icon from here or else change text on button
        //toggling players choice and with same button using it for marking flags and going back to uncovering buttons
        flagButton.setOnClickListener {
            if (playersChoice == 1) {
                playersChoice = 2
                flagButton.setImageResource(R.drawable.colored_flag)
            }
            else {
                playersChoice = 1
                flagButton.setImageResource(R.drawable.uncolored_flag)
            }
        }
    }

    private fun restartGame() {
        //stopping timer until user is on alert dialog box
        pauseAt = SystemClock.elapsedRealtime() - chronometer.base
        chronometer.stop()

        //confirming from user using Alert Dialog, if he chooses to restart game
        val builder  = AlertDialog.Builder(this)
        with(builder) {
            setTitle("Confirm Restart")
            setMessage("Are you sure you want to restart game?")
            setPositiveButton("Yes") {_,_->
                // If he chooses yes, resetting time and restarting game
                //finishing activity and starting activity again using same intent
                finish()
                startActivity(intent)
                Toast.makeText(this@PlayGame,"Game Restarted!",Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Continue Game") {_,_->
                // if user wishes to continue game, we continue timer right from where it was paused
                chronometer.base = SystemClock.elapsedRealtime() - pauseAt
                chronometer.start()
            }
        }
        val ad = builder.create()
        ad.show()
    }


    private fun setUpBoardStartGame() {
        //counter to be used in assigning ids to buttons
        var counter = 1

        mineCountTextView.text = mCount.toString()

        // array to store button (*button is reference variable for CellClass objects) arrays
        val gameBoard = Array(bHeight) {Array(bWidth) { CellClass(this) }}

        /* parameters used to distribute linear layout. We will be creating bHeight no of horizontal linear layouts in our vertical linear layout
        and then add bWidth buttons in each horizontal linear layout */
        val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0)
        val params2 = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT)

        // will be used to set mines on board
        // should not be member variable as its value will be changed on first try of app and will not change for every game
        var firstClick = true

        //creating layouts dynamically
        for (i in 0 until bHeight) {
            val horLinLay = LinearLayout(this)
            horLinLay.orientation =LinearLayout.HORIZONTAL
            horLinLay.layoutParams = params1
            params1.weight = 1.0f //weight takes a float value

            //adding buttons in horizontal linear layout
            for (j in 0 until bWidth) {
                //creating a obj of CellClass as with name button
                val button = CellClass(this)
                //val button = Button(this)
                button.id = counter++
                button.setTextColor(ContextCompat.getColor(this, R.color.red))
                button.layoutParams = params2
                params2.weight = 1.0f
                button.setBackgroundResource(R.drawable.darkdrop2)

                //adding button into the array with its coordinates
                gameBoard[i][j] = button


                //when user clicks on a button
                button.setOnClickListener {
                    //disabling the button once user clicks it
                    button.isEnabled = false
                    //if this is the first click of the user, setting up mines using setMines function
                    if (firstClick) {
                        firstClick = false
                        setMines(i,j,gameBoard,bHeight,bWidth,mCount)
                    }
                    // if this is not the first move of player, using move function to make changes on board
                    makeMove(playersChoice,i,j,gameBoard,bHeight,bWidth,mCount)
                    showBoard(gameBoard)
                }
                // finally adding button to horizontal linear layout
                horLinLay.addView(button)
            }
            // adding each horLinLay to vertical linear layout
            board.addView(horLinLay)
        }
    }

    // function to distribute mines randomly on board
    private fun setMines(i: Int, j: Int, gameBoard: Array<Array<CellClass>>, bHeight: Int, bWidth: Int, mCount: Int) {

        //traversing loop mCount times to place mines one by one
        // cannot use for loop because it will increase k irrespective of if() statement result  i.e.
        // {if statement condition is true --> k should not increase and a new random coordinate should be generated to place that same mine}
        // where as while loop on encountering continue statement will repeat for current value of k
        var k = 1
        while (k <= mCount) {
            //generating a coordinate to place mine
            val c = (Random(System.nanoTime())).nextInt(0,bHeight)
            val r = (Random(System.nanoTime())).nextInt(0,bWidth)
            //checking if mine already exists at that coordinate
            if ( r == i || gameBoard[r][c].isMine) {
                continue
            }
            //if not, then setting mine on randomly generated r,c coordinates and setting its value to -1 and then updating surrounding cells
            gameBoard[r][c].isMine = true
            gameBoard[r][c].value = -1
            updateSurroundCells(r,c,gameBoard,bHeight,bWidth)
            k++
        }
    }


    // this function with take coordinates i,j as input and make necessary changes in board
    // return value is true if move is feasible and false it it isn't
    // although return value of this function is never user, but return statements are necessary to break out from code at desired point
    private fun makeMove(playersChoice: Int, i: Int, j: Int, gameBoard: Array<Array<CellClass>>, bHeight: Int, bWidth: Int, mCount: Int) : Boolean {
        //if player wishes to reveal the mine
        if (playersChoice== 1) {
            // cell should not be marked or revealed
            if ((gameBoard[i][j].isMarked || gameBoard[i][j].isRevealed)) {
                return false
            }
            // if user clicks on a mine, setting status to LOST (value of cell containing mine is set to -1 in setMines function)
            // and updating score
            if (gameBoard[i][j].value == MINE){
                // first set status to lost and then only update score
                status = Status.LOST                                                                // it hurt most
                updateScore()
                return true
            }
            // if the value that cell holds is > 0, that means it has a mine in its surrounding cells.
            // setting status to revealed and checking status fo game using checkStatus() function
            else if (gameBoard[i][j].value > 0 ) {
                gameBoard[i][j].isRevealed = true
                checkStatus(gameBoard,bHeight,bWidth)
                return true
            }
            // if the value that cell holds is 0, that means cell has no nearby mines and
            // revealing board cells until a non zero value if fount in every direction (8) using revealZeros() function
            else if (gameBoard[i][j].value == 0) {
                revealZeros(i,j,gameBoard,bHeight,bWidth)
                checkStatus(gameBoard,bHeight,bWidth)
                return true
            }
        }
        // if player wisher to mark flags
        if (playersChoice== 2) {
            // should work only if the cell is not already revealed, else not a feasible move
            if (gameBoard[i][j].isRevealed) {
                return false
            }
            // if cell is marked, changing its background and changing its Marked status
            if (gameBoard[i][j].isMarked) {
                gameBoard[i][j].setBackgroundResource(R.drawable.backdrop)
                gameBoard[i][j].isMarked =false
                checkStatus(gameBoard,bHeight,bWidth)
                // reducing no of flags that can be marked by 1
                flaggedMines --
            }
            else {
                // if already mCount flags  are marked on board, no more flags should be allowed to mark
                if (flaggedMines == mCount) {
                    Toast.makeText(this,"All mine flags exhausted!", Toast.LENGTH_LONG).show()
                    return false
                }
                // increasing flag count and setting isMarked = true for that cell
                flaggedMines++
                checkStatus(gameBoard,bHeight,bWidth)
                gameBoard[i][j].isMarked = true
            }
            //updating mine count in the text view
            mineCountTextView.text = (mCount-flaggedMines).toString()
            return true
        }
        return false
    }


    // function to show buttons on board as per game status
    private fun showBoard(gameBoard: Array<Array<CellClass>>) {
        gameBoard.forEach { eachRow ->
            // for every row (again no of rows = bHeight) in gameBoard, each cell will be traversed
            // and its content is set using  setTextOnButtons() function
            eachRow.forEach {
                when {
                        // if the cell is revealed, show its value
                    it.isRevealed -> setTextOnButtons(it)
                    // if user has chosen to mark it as flag
                    it.isMarked -> it.setBackgroundResource(R.drawable.flag)
                    // if user clicked on mine
                    (status == Status.LOST && it.value == MINE ) -> it.setBackgroundResource(R.drawable.mine)
                    // if user has won, show all mines as flags
                    (status == Status.WON && it.value == MINE) -> it.setBackgroundResource(R.drawable.flag)
                    else -> it.text = " "
                }
            }
        }
    }


    //updating surrounding cells of every cell
    private val movement = intArrayOf(-1, 0, 1)
    private fun updateSurroundCells(r: Int, c: Int, gameBoard: Array<Array<CellClass>>, bHeight: Int, bWidth: Int) {
        for (i in movement) {
            for (j in movement) {
                //checking in each of 8 surroundings that if next block is in range and is not a mine
                if (((r+i) in 0 until bWidth) && ((c+j) in 0 until bHeight) && gameBoard[r+i][c+j].value != MINE) {
                    gameBoard[r+i][c+j].value++
                }
            }
        }
    }


    //revealing board cells until a non zero value if fount in every direction using following arrays
    // Handles when gameBoard[x][y]==0
    private val xDir = intArrayOf(-1, -1, 0, 1, 1, 1, 0, -1)
    private val yDir = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
    private fun revealZeros(i: Int, j: Int, gameBoard: Array<Array<CellClass>>, bHeight: Int, bWidth: Int) {
        gameBoard[i][j].isRevealed = true
        for ( k in 0 until 8) {
            var xStep = i+xDir[k]
            var yStep = j+yDir[k]
            //checking if coordinates are in board bounds or not
            if((xStep<0 || xStep>= bWidth) || (yStep<0 || yStep>=bHeight)){
                continue;
            }
            // if cell value is non zero and it is not marked then revealing the cell and done with function
            if(gameBoard[xStep][yStep].value>0 && !gameBoard[xStep][yStep].isMarked){
                gameBoard[xStep][yStep].isRevealed = true
            }
            //if again cell value is again zero and cell is neither marked nor revealed , using recursive call to inspect next 8 cells around this cell
            else if( !gameBoard[xStep][yStep].isRevealed && !gameBoard[xStep][yStep].isMarked && gameBoard[xStep][yStep].value==0){
                revealZeros(xStep,yStep,gameBoard,bHeight,bWidth)
            }
        }
    }

    // function to check current status of game and update details
    private fun checkStatus(gameBoard: Array<Array<CellClass>>, bHeight: Int, bWidth: Int) {
        var condition1 = true
        var condition2 = true
        for(i in 0 until bWidth){
            for(j in 0 until bHeight){
                // any cell giving true in these condition will ensure that win in not through this condition
                // if the cell is a mine and it is not marked flag
                if(gameBoard[i][j].value==MINE && !gameBoard[i][j].isMarked){
                    condition1 = false
                }
                // if the cell is not mine and revealed
                if(gameBoard[i][j].value!=MINE && !gameBoard[i][j].isRevealed){
                    condition2 = false
                }
            }
        }
        // any one condition will be sufficient condition for winning,
        // either every mine cell is marked as flag or every non mine cell is revealed
        status = if(condition1 || condition2) Status.WON  else Status.ONGOING
        if(status==Status.WON) {
            updateScore()
        }
    }


    //to update best time and last game time in main activity xml file, passing data using intent
    private fun updateBestAndLastTime() {
        Log.d("MainActivity","inside to main"+fastestTime+" "+lastGameTime)
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("highScore",fastestTime)
        intent.putExtra("lastTime",lastGameTime)
        startActivity(intent)
    }

    // function to update scores. mainly updates time in main screen
    private fun updateScore(){
        chronometer.stop()

        // Getting elapsed time from chronometer
        val elapsedTime = SystemClock.elapsedRealtime() - chronometer.base;
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val lastTime = elapsedTime.toInt()

        // Setting up highscore ///////////////////////////////
        var highScore = sharedPref.getInt(getString(R.string.saved_high_score_key), Integer.MAX_VALUE)

        var isHighScore=false

        // Comparing high score if the last game's status is won
        if(status==Status.WON) {
            if (lastTime < highScore) {
                highScore = lastTime
                isHighScore = true
            }
            with(sharedPref.edit()) {
                putInt(getString(R.string.saved_high_score_key), highScore)
                putInt(getString(R.string.last_time), lastTime)
                commit()
            }
            // Setting time formats to send to another activity
            lastGameTime = ""+((lastTime / 1000) / 60)+" m "+((lastTime / 1000) % 60)+" s"
        }
        else{
            lastGameTime = " Lost!"
            fastestTime = " NA"
        }

        if(highScore==Integer.MAX_VALUE){
            fastestTime = " NA"
        }
        else {
            // Setting time formats to send to another activity
            fastestTime = "" + ((highScore / 1000) / 60) + " m " + ((highScore / 1000) % 60) + " s";
        }

        if(status == Status.WON){


            var currentTime=chronometer.text.toString()
            val sharedPreferences: SharedPreferences =
                this.getSharedPreferences("time", Context.MODE_PRIVATE)

            var best=sharedPreferences.getString("Best","00.00")
            if(best!! > currentTime){
                best=sharedPreferences.getString("Best",currentTime)
            }
            chronometer.stop()

            val builder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView : View = inflater.inflate(R.layout.status_won,null)
            val pBt = dialogView.findViewById<Button>(R.id.new_game_button)

            with (builder) {
                setView(dialogView)
                pBt.setOnClickListener {
                    val intent = Intent(this@PlayGame, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            val adb = builder.create()
            adb.window?.setBackgroundDrawableResource(android.R.color.transparent)
            adb.setCancelable(false)
            adb.show()
        }

        else if(status == Status.LOST){

            var currentTime=chronometer.text.toString()

            val sharedPreferences: SharedPreferences =
                this.getSharedPreferences("time", Context.MODE_PRIVATE)

            sharedPreferences.edit().putString("Last",currentTime).apply()

            val builder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView : View = inflater.inflate(R.layout.status_lost,null)
            val pBt = dialogView.findViewById<Button>(R.id.new_game_button)
            with (builder) {
                setView(dialogView)
                pBt.setOnClickListener {
                    val intent = Intent(this@PlayGame, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            val adb = builder.create()
            adb.window?.setBackgroundDrawableResource(android.R.color.transparent)
            adb.setCancelable(false)
            adb.show()
        }

    }

    private fun setTextOnButtons(button:CellClass) {
        for (k in 0..8) {
            if(button.value==k) button.text = button.value.toString()
            button.textSize = 20.0f
            button.setBackgroundResource(R.drawable.backdrop)
            button.setTextColor(ContextCompat.getColor(this,R.color.red))
        }
    }

    // ctrl + o (or code --> override methods) --> search oBackPressed
    //code to handle what happens if user presses back button
    override fun onBackPressed() {
        //stopping timer until user is on alert dialog box
        pauseAt = SystemClock.elapsedRealtime() - chronometer.base
        chronometer.stop()

        //confirming from user using Alert Dialog, if he chooses to terminate the game
        val builder  = AlertDialog.Builder(this)
        with(builder) {
            setTitle("Go back to Homepage")
            setMessage("Are you sure you want to end the game?")
            setPositiveButton("Yes") {_,_->
                // If he chooses yes, we close the activity using finish() or using super.onBackPressed()
                // and update scores and best & last game times in main activity text views
                updateBestAndLastTime()
                updateScore()
                super.onBackPressed() //this will implicitly invoke finish()
                Toast.makeText(this@PlayGame,"Game Ended!",Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Continue Game") {_,_->
                // if user wishes to continue game, we continue timer right from where it was paused
                chronometer.base = SystemClock.elapsedRealtime() - pauseAt
                chronometer.start()
            }
        }
        val ad = builder.create()
        ad.show()
    }
}