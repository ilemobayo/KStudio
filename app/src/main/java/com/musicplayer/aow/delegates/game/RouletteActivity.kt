package com.musicplayer.aow.delegates.game

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.*

import com.musicplayer.aow.R
import kotlinx.android.synthetic.main.activity_roulette.*

import java.util.Random
import kotlinx.android.synthetic.main.roulette_table.*


/*
*
* https://github.com/phillip-ables/android-roulette-game/blob/master/Roulette/app/src/main/java/com/example/techtron/roulette/RouletteActivity.java
*
 */
class RouletteActivity : AppCompatActivity() {
    internal lateinit var button: Button
    internal lateinit var textView: TextView
    internal lateinit var wheelRoul: ImageView

    internal lateinit var r: Random
    internal var degree = 0
    internal var degree_old = 0
    internal var coin = 0

    //37 == 0..36
    private val splitValue = arrayOf("0 | 00","27 | 10","10 | 25",
            "25 | 29","29 | 12","12 | 8","8 | 19","19 | 31","31 | 18",
            "18 | 6","6 | 21","21 | 33","33 | 16","16 | 4","4 | 23","23 | 35","35 | 14",
            "14 | 2","2 | 28","28 | 9","9 | 26","26 | 30","30 | 11","11 | 7",
            "7 | 20","20 | 32","32 | 17","17 | 5","5 | 22","22 | 34","34 | 15","15 | 3",
            "3 | 24","24 | 36","36 | 13","13 | 1","1 | 27")
    private var rouletteButton: Array<Button>? = null
    private var straightBetNumber = 0
    private val firstDozen = arrayOf(1,2,3,4,5,6,7,8,9,10,11,12)
    private val secondDozen = arrayOf(13,14,15,16,17,18,19,20,21,22,23,24)
    private val thirdDozen = arrayOf(25,26,27,28,29,30,31,32,33,34,35,36)
    private val firstColumn = arrayOf(3,6,9,12,15,18,21,24,27,30,33,36)
    private val secondColumn = arrayOf(2,5,8,11,14,17,20,23,26,29,32,35)
    private val thirdColumn = arrayOf(1,4,7,10,13,16,19,22,25,28,31,34)
    private val lowBetNumbers = arrayOf(1..18)
    private val highBetNumbers = arrayOf(19..36)
    private var outsideBetOnRed = false
    private var outsideBetOnEven = false
    private var firstSplitBetNumber = 0
    private var secondSplitBetNumber = 0
    private var color = 0
    private var number = 0
    private var wager_amount = 0
    private var gameOdd = 0
    private var betType = 0
    private var betNumber = 0
    private var payOut = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roulette)
        button = findViewById<View>(R.id.btn_spin) as Button
        textView = findViewById<View>(R.id.textView) as TextView
        wheelRoul = findViewById<View>(R.id.imRoulette) as ImageView

        rouletteButton = arrayOf(btn_0,btn_00,btn_1,btn_2,btn_3,btn_4,btn_5,btn_6,btn_7,btn_8,btn_9,btn_10,
                btn_11,btn_12,btn_13,btn_14,btn_15,btn_16,btn_17,btn_18,btn_19,btn_20,btn_21,btn_22,btn_23,btn_24,btn_25,
                btn_26,btn_27,btn_28,btn_29,btn_30,btn_31,btn_32,btn_33,btn_34,btn_35,btn_36,btn_1_to_18,btn_19_to_36,
                btn_even,btn_odd,btn_red,btn_black,btn_1st_dozen,btn_2nd_dozen,btn_3rd_dozen,btn_1st_column,btn_2nd_column,
                btn_3rd_column)

        coin = 5000
        player_money.text = coin.toString()

        //
        straight_bet_picker.minValue = 0
        straight_bet_picker.maxValue = 36
        straight_bet_picker.wrapSelectorWheel = true
        straight_bet_picker.setOnValueChangedListener { picker, oldVal, newVal ->
            betNumber = newVal
            your_bet_number.text = betNumber.toString()
        }


        //Wager Amount
        wager.setOnEditorActionListener { v, actionId, event ->
            if ((event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                if (wager.text != null) {
                    wager_amount = wager.text.toString().toInt()
                    final_wager.text = wager_amount.toString()
                    payOut = wager_amount * gameOdd
                    winning_amount.text = (payOut).toString()
                }
            }
            false
        }
        //Bet type selector
        // Spinner click listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                   0 -> {
                       payoff.text = "35 - 1"
                       gameOdd = 35
                       betType = 0
                       straight_bet_plane.visibility = View.VISIBLE
                       split_bet_plane.visibility = View.GONE
                   }
                   1 -> {
                       payoff.text = "17 - 1"
                       gameOdd = 17
                       betType = 1
                       firstSplitBetNumber = 0
                       secondSplitBetNumber = 0
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.VISIBLE
                       promtSplitInput()
                   }
                   2 -> {
                       payoff.text = "11 - 1"
                       gameOdd = 11
                       betType = 2
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                   3 -> {
                       payoff.text = "8 - 1"
                       gameOdd = 8
                       betType = 3
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                   4 -> {
                       payoff.text = "6 - 1"
                       gameOdd = 6
                       betType = 4
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                   5 -> {
                       payoff.text = "5 - 1"
                       gameOdd = 5
                       betType = 5
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                   6 -> {
                       payoff.text = "2 - 1"
                       gameOdd = 2
                       betType = 6
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                   7 -> {
                       payoff.text = "2 - 1"
                       gameOdd = 2
                       betType = 7
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                   8 -> {
                       payoff.text = "1 - 1"
                       gameOdd = 1
                       betType = 8
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                       txt_info.text = "Please select your bet color Red/Black"
                       your_bet_number.text = "Please select your bet color Red/Black"
                   }
                   9 -> {
                       payoff.text = "1 - 1"
                       gameOdd = 1
                       betType = 9
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                   10 -> {
                       payoff.text = "1 - 1"
                       gameOdd = 1
                       betType = 10
                       straight_bet_plane.visibility = View.GONE
                       split_bet_plane.visibility = View.GONE
                   }
                }
            }

        }

        // Spinner Drop down elements
        val categories = ArrayList<String>()
        categories.add(resources.getString(R.string.straight_bet))
        categories.add(resources.getString(R.string.split_bet))
        categories.add(resources.getString(R.string.street_bet))
        categories.add(resources.getString(R.string.square_bet))
        categories.add(resources.getString(R.string.basket_bet))
        categories.add(resources.getString(R.string.avenue_bet))
        categories.add(resources.getString(R.string.dozen_bet))
        categories.add(resources.getString(R.string.column_bet))
        categories.add(resources.getString(R.string.redblack_bet))
        categories.add(resources.getString(R.string.evenodd_bet))
        categories.add(resources.getString(R.string.lowhigh_bet))

        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // attaching data adapter to spinner
        spinner.adapter = dataAdapter

        //split
        split_clear.setOnClickListener{
            firstSplitBetNumber = 0
            secondSplitBetNumber = 0
            split_bet_picker_one.text = 0.toString()
            split_bet_picker_two.text = 0.toString()
            promtSplitInput()
        }

        r = Random()
        button.setOnClickListener {
            if (coin > 0 && coin >= wager_amount){
                playGame()
            }
        }

        btn_board_table.setOnClickListener{
            if (board_table.visibility == View.VISIBLE) {
                board_table.visibility = View.GONE
                board.visibility = View.VISIBLE
            } else {
                board.visibility = View.GONE
                board_table.visibility = View.VISIBLE

                tableButtonClicked()
            }
        }
    }

    fun playGame(){
        wager.isEnabled = false
        degree_old = degree % 360
        degree = r.nextInt(360) + 720
        val rotate = RotateAnimation(degree_old.toFloat(), degree.toFloat(),
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 3600 * 3
        rotate.fillAfter = true
        rotate.interpolator = DecelerateInterpolator()
        rotate.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {
                win_or_loss_plane.text = "Spinning..."
                textView.text = "Wheel"
            }

            override fun onAnimationEnd(animation: Animation) {
                textView.text = currentNumber(360 - (degree % 360))
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        wheelRoul.startAnimation(rotate)
    }

    private fun currentNumber(degrees: Int): String {
        var text = "Spinning..."
        if (degrees >= FACTOR * 1 && degrees < FACTOR * 3) {
            number = 27
            color = 1
            text = "27 red"
        }
        if (degrees >= FACTOR * 3 && degrees < FACTOR * 5) {
            number = 10
            color = 2
            text = "10 black"
        }
        if (degrees >= FACTOR * 5 && degrees < FACTOR * 7) {
            number = 25
            color = 1
            text = "25 red"
        }
        if (degrees >= FACTOR * 7 && degrees < FACTOR * 9) {
            number = 29
            color = 2
            text = "29 black"
        }
        if (degrees >= FACTOR * 9 && degrees < FACTOR * 11) {
            number = 12
            color = 1
            text = "12 red"
        }
        if (degrees >= FACTOR * 11 && degrees < FACTOR * 13) {
            number = 8
            color = 2
            text = "8 black"
        }
        if (degrees >= FACTOR * 13 && degrees < FACTOR * 15) {
            number = 19
            color = 1
            text = "19 red"
        }
        if (degrees >= FACTOR * 15 && degrees < FACTOR * 17) {
            number = 31
            color = 2
            text = "31 black"
        }
        if (degrees >= FACTOR * 17 && degrees < FACTOR * 19) {
            number = 18
            color = 1
            text = "18 red"
        }
        if (degrees >= FACTOR * 19 && degrees < FACTOR * 21) {
            number = 6
            color = 2
            text = "6 black"
        }
        if (degrees >= FACTOR * 21 && degrees < FACTOR * 23) {
            number = 21
            color = 1
            text = "21 red"
        }
        if (degrees >= FACTOR * 23 && degrees < FACTOR * 25) {
            number = 33
            color = 2
            text = "33 black"
        }
        if (degrees >= FACTOR * 25 && degrees < FACTOR * 27) {
            number = 16
            color = 1
            text = "16 red"
        }
        if (degrees >= FACTOR * 27 && degrees < FACTOR * 29) {
            number = 4
            color = 2
            text = "4 black"
        }
        if (degrees >= FACTOR * 29 && degrees < FACTOR * 31) {
            number = 23
            color = 1
            text = "23 red"
        }
        if (degrees >= FACTOR * 31 && degrees < FACTOR * 33) {
            number = 35
            color = 2
            text = "35 black"
        }
        if (degrees >= FACTOR * 33 && degrees < FACTOR * 35) {
            number = 14
            color = 1
            text = "14 red"
        }
        if (degrees >= FACTOR * 35 && degrees < FACTOR * 37) {
            number = 2
            color = 2
            text = "2 black"
        }
        if (degrees >= FACTOR * 37 && degrees < FACTOR * 39) {
            number = 0
            color = 0
            text = "0 green"
        }
        if (degrees >= FACTOR * 39 && degrees < FACTOR * 41) {
            number = 28
            color = 2
            text = "28 black"
        }
        if (degrees >= FACTOR * 41 && degrees < FACTOR * 43) {
            number = 9
            color = 1
            text = "9 red"
        }
        if (degrees >= FACTOR * 43 && degrees < FACTOR * 45) {
            number = 26
            color = 2
            text = "26 black"
        }
        if (degrees >= FACTOR * 45 && degrees < FACTOR * 47) {
            number = 30
            color = 1
            text = "30 red"
        }
        if (degrees >= FACTOR * 47 && degrees < FACTOR * 49) {
            number = 11
            color = 2
            text = "11 black"
        }
        if (degrees >= FACTOR * 49 && degrees < FACTOR * 51) {
            number = 7
            color = 1
            text = "7 red"
        }
        if (degrees >= FACTOR * 51 && degrees < FACTOR * 53) {
            number = 20
            color = 2
            text = "20 black"
        }
        if (degrees >= FACTOR * 53 && degrees < FACTOR * 55) {
            number = 32
            color = 1
            text = "32 red"
        }
        if (degrees >= FACTOR * 55 && degrees < FACTOR * 57) {
            number = 17
            color = 2
            text = "17 black"
        }
        if (degrees >= FACTOR * 57 && degrees < FACTOR * 59) {
            number = 5
            color = 1
            text = "5 red"
        }
        if (degrees >= FACTOR * 59 && degrees < FACTOR * 61) {
            number = 22
            color = 2
            text = "22 black"
        }
        if (degrees >= FACTOR * 61 && degrees < FACTOR * 63) {
            number = 34
            color = 1
            text = "34 red"
        }
        if (degrees >= FACTOR * 63 && degrees < FACTOR * 65) {
            number = 15
            color = 2
            text = "15 black"
        }
        if (degrees >= FACTOR * 65 && degrees < FACTOR * 67) {
            number = 3
            color = 1
            text = "3 red"
        }
        if (degrees >= FACTOR * 67 && degrees < FACTOR * 69) {
            number = 24
            color = 2
            text = "24 black"
        }
        if (degrees >= FACTOR * 69 && degrees < FACTOR * 71) {
            number = 36
            color = 1
            text = "36 red"
        }
        if (degrees >= FACTOR * 71 && degrees < FACTOR * 73) {
            number = 13
            color = 2
            text = "13 black"
        }
        if (degrees >= FACTOR * 73 && degrees < FACTOR * 75) {
            number = 1
            color = 1
            text = "1 red"
        }
        if (degrees >= FACTOR * 75 && degrees < FACTOR * 77) {
            number = 0
            color = 0
            text = "00 green"
            //text = "zero"
        }
        if (degrees >= FACTOR * 75 && degrees < 360 || degrees >= 0 && degrees < FACTOR * 77) {
            //text = "zero"
        }

        if (isEvenNumber(number)){
            number_cat.text = "even"
        } else {
            number_cat.text = "odd"
        }

        number_selected.text = number.toString()
        when(color){
            0 -> {
                number_color.text = "Green"
                number_color.setBackgroundColor(Color.GREEN)
            }
            1 -> {
                number_color.text = "Red"
                number_color.setBackgroundColor(Color.RED)
            }
            2 -> {
                number_color.text = "Black"
                number_color.setBackgroundColor(Color.BLACK)
            }
        }
        wager.isEnabled = true
        wager.setText(resources.getString(R.string.empty))
        winOrLoss()

        return text
    }

    fun isEvenNumber(num: Int): Boolean {
        return num % 2 == 0
    }

    private fun winOrLoss(win: Boolean, wagerMoney: Int = 0){
        if (win) {
            coin += payOut
            win_or_loss_plane.text = "Win"
            player_money.text = coin.toString()
        } else {
            coin -= if(wagerMoney == 0) wager_amount else wagerMoney
            win_or_loss_plane.text = "Loss"
            player_money.text = coin.toString()
        }
    }

    fun winOrLoss(){
        when(betType){
            0 -> {
                if (number == betNumber) {
                    winOrLoss(true)
                } else {
                    winOrLoss(false)
                }
            }
            1 -> {
                split()
            }
            2 -> {

            }
            3 -> {

            }
            4 -> {

            }
            5 -> {

            }
            6 -> {

            }
            7 -> {

            }
            8 -> {
                outsideColor()
            }
            9 -> {
                if (number == 0){
                    winOrLoss(false, wager_amount /2 )
                } else if (outsideBetOnEven == isEvenNumber(number)){
                    winOrLoss(true)
                } else {
                    winOrLoss(false)
                }
            }
            10 -> {

            }
        }
    }

    fun promtSplitInput(){
        if (firstSplitBetNumber == 0){
            txt_info.text = "Please enter your split bet 1st number"
            your_bet_number.text = "Please enter your split bet 1st number"
        } else if (secondSplitBetNumber == 0) {
            txt_info.text = "Please enter your split bet 2nd number"
            your_bet_number.text = "Please enter your split bet 2nd number"
        }
    }


    private fun tableButtonClicked(){
        rouletteButton!!.forEach { btn ->
            btn.setOnClickListener{
                when(btn){
                    btn_0 -> {
                        val value = 0
                        betType(value)
                    }
                    btn_00 -> {
                        val value = 0
                        betType(value)
                    }
                    btn_1 -> {
                        val value = 1
                        betType(value)
                    }
                    btn_2 -> {
                        val value = 2
                        betType(value)
                    }
                    btn_3 -> {
                        val value = 3
                        betType(value)
                    }
                    btn_4 -> {
                        val value = 4
                        betType(value)
                    }
                    btn_5 -> {
                        val value = 5
                        betType(value)
                    }
                    btn_6 -> {
                        val value = 6
                        betType(value)
                    }
                    btn_7 -> {
                        val value = 7
                        betType(value)
                    }
                    btn_8 -> {
                        val value = 8
                        betType(value)
                    }
                    btn_9 -> {
                        val value = 9
                        betType(value)
                    }
                    btn_10 -> {
                        val value = 10
                        betType(value)
                    }
                    btn_11 -> {
                        val value = 11
                        betType(value)
                    }
                    btn_12 -> {
                        val value = 12
                        betType(value)
                    }
                    btn_13 -> {
                        val value = 13
                        betType(value)
                    }
                    btn_14 -> {
                        val value = 14
                        betType(value)
                    }
                    btn_15 -> {
                        val value = 15
                        betType(value)
                    }
                    btn_16 -> {
                        val value = 16
                        betType(value)
                    }
                    btn_17 -> {
                        val value = 17
                        betType(value)
                    }
                    btn_18 -> {
                        val value = 18
                        betType(value)
                    }
                    btn_19 -> {
                        val value = 19
                        betType(value)
                    }
                    btn_20 -> {
                        val value = 20
                        betType(value)
                    }
                    btn_21 -> {
                        val value = 21
                        betType(value)
                    }
                    btn_22 -> {
                        val value = 22
                        betType(value)
                    }
                    btn_23 -> {
                        val value = 23
                        betType(value)
                    }
                    btn_24 -> {
                        val value = 24
                        betType(value)
                    }
                    btn_25 -> {
                        val value = 25
                        betType(value)
                    }
                    btn_26 -> {
                        val value = 26
                        betType(value)
                    }
                    btn_27 -> {
                        val value = 27
                        betType(value)
                    }
                    btn_28 -> {
                        val value = 28
                        betType(value)
                    }
                    btn_29 -> {
                        val value = 29
                        betType(value)
                    }
                    btn_30 -> {
                        val value = 30
                        betType(value)
                    }
                    btn_31 -> {
                        val value = 31
                        betType(value)
                    }
                    btn_32 -> {
                        val value = 32
                        betType(value)
                    }
                    btn_33 -> {
                        val value = 33
                        betType(value)
                    }
                    btn_34 -> {
                        val value = 34
                        betType(value)
                    }
                    btn_35 -> {
                        val value = 35
                        betType(value)
                    }
                    btn_36 -> {
                        val value = 36
                        betType(value)
                    }
                    btn_odd -> {
                        outsideEvenOdd(1)
                    }
                    btn_even -> {
                        outsideEvenOdd(0)
                    }
                    btn_red -> {
                        outsideColor(1)
                    }
                    btn_black -> {
                        outsideColor(0)
                    }
                    btn_1_to_18 -> {
                        Toast.makeText(applicationContext, "1 to 18 (Low Bet)", Toast.LENGTH_SHORT).show()
                    }
                    btn_19_to_36 -> {
                        Toast.makeText(applicationContext, "19 to 36 (High Bet)", Toast.LENGTH_SHORT).show()
                    }
                    btn_1st_column -> {
                        Toast.makeText(applicationContext, "1st Column", Toast.LENGTH_SHORT).show()
                    }
                    btn_2nd_column -> {
                        Toast.makeText(applicationContext, "2nd Column", Toast.LENGTH_SHORT).show()
                    }
                    btn_3rd_column -> {
                        Toast.makeText(applicationContext, "3rd Column", Toast.LENGTH_SHORT).show()
                    }
                    btn_1st_dozen -> {
                        Toast.makeText(applicationContext, "1st Dozen", Toast.LENGTH_SHORT).show()
                    }
                    btn_2nd_dozen -> {
                        Toast.makeText(applicationContext, "2nd Dozen", Toast.LENGTH_SHORT).show()
                    }
                    btn_3rd_dozen -> {
                        Toast.makeText(applicationContext, "3rd Dozen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun betType(straightValue: Int = 0){
        when(betType){
            0 -> {
                straightBetValue(straightValue)
            }
            1 -> {
                if (firstSplitBetNumber == 0){
                    firstSplitBetNumber = straightValue
                    split_bet_picker_one.text = firstSplitBetNumber.toString()
                    promtSplitInput()
                } else if (secondSplitBetNumber == 0){
                    val validSplit = SplitBet(firstSplitBetNumber, straightValue).readBet()
                    if (validSplit){
                        secondSplitBetNumber = straightValue
                        split_bet_picker_two.text = secondSplitBetNumber.toString()
                        txt_info.text = "Split numbers are $firstSplitBetNumber or $secondSplitBetNumber"
                        your_bet_number.text = "$firstSplitBetNumber or $secondSplitBetNumber"
                    } else {
                        txt_info.text = "Split numbers not valid, example 10/7,10/11 or 10/13"
                        your_bet_number.text = "Split numbers not valid, example 10/7,10/11 or 10/13"
                    }
                } else {
                    txt_info.text = "Split numbers are $firstSplitBetNumber or $secondSplitBetNumber"
                    your_bet_number.text = "$firstSplitBetNumber or $secondSplitBetNumber"
                }
            }
            2 -> {

            }
            3 -> {

            }
            4 -> {

            }
            5 -> {

            }
            6 -> {

            }
            7 -> {

            }
            8 -> {

            }
            9 -> {

            }
            10 -> {

            }
        }
    }

    private fun straightBetValue(value: Int){
        if (value in 0..36) {
            betNumber = value
            txt_info.text = betNumber.toString()
            your_bet_number.text = betNumber.toString()
        }
    }

    private fun split(){
        val validSplit = SplitBet(firstSplitBetNumber, secondSplitBetNumber).readBet()
        if (validSplit){
            if (firstSplitBetNumber == number || secondSplitBetNumber == number) {
                winOrLoss(true)
            } else {
                winOrLoss(false)
            }
        } else {
            txt_info.text = "Split numbers not valid, example 10/7,10/11 or 10/13"
            your_bet_number.text = "Split numbers not valid, example 10/7,10/11 or 10/13"
        }
    }

    private fun outsideEvenOdd(value: Int){
        if (value == 0){
            outsideBetOnEven = true
            var evenNumbers = ""
            for (i in 0..36){
                if (isEvenNumber(i)){
                    evenNumbers = "$evenNumbers$i "
                }
            }
            txt_info.text = "Even Number Bet $evenNumbers"
            your_bet_number.text = "Even Number Bet $evenNumbers"
        } else {
            outsideBetOnEven = false
            var oddNumbers = ""
            for (i in 0..36){
                if (!isEvenNumber(i)){
                    oddNumbers = "$oddNumbers$i "
                }
            }
            txt_info.text = "Odd Number Bet $oddNumbers"
            your_bet_number.text = "Odd Number Bet $oddNumbers"
        }
    }

    private fun outsideColor(){
        if (color == 0){
            winOrLoss(false, wager_amount /2 )
        } else if (outsideBetOnRed && color == 1){
            winOrLoss(true)
        } else if (!outsideBetOnRed && color == 2){
            winOrLoss(true)
        } else {
            winOrLoss(false)
        }
    }

    private fun outsideColor(value: Int){
        when(value){
            0 -> {
                outsideBetOnRed = true
                your_bet_number.text = "Red"
                your_bet_number.setTextColor(ContextCompat.getColor(applicationContext, R.color.red_dim))
            }
            1 -> {
                outsideBetOnRed = false
                your_bet_number.text = "Black"
                your_bet_number.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
            }
        }
    }

    companion object {
        //his was 37 but i had an extra zero
        //because there is 38 sectors on the wheel (9.47 degrees each)
        private val FACTOR = 4.7368f
    }
}