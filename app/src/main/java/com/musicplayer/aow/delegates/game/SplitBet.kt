package com.musicplayer.aow.delegates.game

class SplitBet(private val firstValue: Int, private val secondValue:Int) {

    fun readBet(): Boolean{
        var value = false
        when (firstValue){
            1 -> {
                if (secondValue == 2 || secondValue == 4){
                    value = true
                }
            }
            2 -> {
                if (secondValue == 1 || secondValue == 3 || secondValue == 5){
                    value = true
                }
            }
            3 -> {
                if (secondValue == 2 || secondValue == 6){
                    value = true
                }
            }
            4 -> {
                if (secondValue == 1 || secondValue == 5 || secondValue == 7){
                    value = true
                }
            }
            5 -> {
                if (secondValue == 2 || secondValue == 4 || secondValue == 5 || secondValue == 8){
                    value = true
                }
            }
            6 -> {
                if (secondValue == 3 || secondValue == 5 || secondValue == 9){
                    value = true
                }
            }
            7 -> {
                if (secondValue == 4 || secondValue == 8 || secondValue == 10){
                    value = true
                }
            }
            8 -> {
                if (secondValue == 5 || secondValue == 7 || secondValue == 9 || secondValue == 11){
                    value = true
                }
            }
            9 -> {
                if (secondValue == 6 || secondValue == 8 || secondValue == 12){
                    value = true
                }
            }
            10 -> {
                if (secondValue == 7 || secondValue == 11 || secondValue == 13){
                    value = true
                }
            }
            11 -> {
                if (secondValue == 8 || secondValue == 10 || secondValue == 12 || secondValue == 14){
                    value = true
                }
            }
            12 -> {
                if (secondValue == 9 || secondValue == 11 || secondValue == 15){
                    value = true
                }
            }
            13 -> {
                if (secondValue == 10 || secondValue == 14 || secondValue == 16){
                    value = true
                }
            }
            14 -> {
                if (secondValue == 11 || secondValue == 13 || secondValue == 15 || secondValue == 17){
                    value = true
                }
            }
            15 -> {
                if (secondValue == 12 || secondValue == 14 || secondValue == 18){
                    value = true
                }
            }
            16 -> {
                if (secondValue == 13 || secondValue == 17 || secondValue == 19){
                    value = true
                }
            }
            17 -> {
                if (secondValue == 14 || secondValue == 16 || secondValue == 18 || secondValue == 20){
                    value = true
                }
            }
            18 -> {
                if (secondValue == 15 || secondValue == 17 || secondValue == 21){
                    value = true
                }
            }
            19 -> {
                if (secondValue == 16 || secondValue == 20 || secondValue == 22){
                    value = true
                }
            }
            20 -> {
                if (secondValue == 17 || secondValue == 19 || secondValue == 21 || secondValue == 23){
                    value = true
                }
            }
            21 -> {
                if (secondValue == 18 || secondValue == 20 || secondValue == 24){
                    value = true
                }
            }
            22 -> {
                if (secondValue == 19 || secondValue == 23 || secondValue == 25){
                    value = true
                }
            }
            23 -> {
                if (secondValue == 20 || secondValue == 22 || secondValue == 24 || secondValue == 26){
                    value = true
                }
            }
            24 -> {
                if (secondValue == 21 || secondValue == 23 || secondValue == 27){
                    value = true
                }
            }
            25 -> {
                if (secondValue == 22 || secondValue == 26 || secondValue == 28){
                    value = true
                }
            }
            26 -> {
                if (secondValue == 23 || secondValue == 25 || secondValue == 27 || secondValue == 29){
                    value = true
                }
            }
            27 -> {
                if (secondValue == 24 || secondValue == 26 || secondValue == 30){
                    value = true
                }
            }
            28 -> {
                if (secondValue == 25 || secondValue == 29 || secondValue == 31){
                    value = true
                }
            }
            29 -> {
                if (secondValue == 26 || secondValue == 28 || secondValue == 30 || secondValue == 32){
                    value = true
                }
            }
            30 -> {
                if (secondValue == 27 || secondValue == 29 || secondValue == 33){
                    value = true
                }
            }
            31 -> {
                if (secondValue == 28 || secondValue == 32 || secondValue == 34){
                    value = true
                }
            }
            32 -> {
                if (secondValue == 29 || secondValue == 31 || secondValue == 33 || secondValue == 35){
                    value = true
                }
            }
            33 -> {
                if (secondValue == 30 || secondValue == 32 || secondValue == 36){
                    value = true
                }
            }
            34 -> {
                if (secondValue == 31 || secondValue == 35){
                    value = true
                }
            }
            35 -> {
                if (secondValue == 32 || secondValue == 34 || secondValue == 36){
                    value = true
                }
            }
            36 -> {
                if (secondValue == 33 || secondValue == 35){
                    value = true
                }
            }
        }

        return value
    }
}