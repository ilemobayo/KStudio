package com.musicplayer.aow.delegates.game; /************************************************
* from http://home.att.net/~gobruen/
*
*
*Roulette.java
*
* Called by GameDriver.java, needs Queue.java to run
*
*
* Methods:
* roulette() 	main driver for this game
* spin() 	gets the winning number
* takeAction()	processes user input
* procBet()	works with procBet2() to process bets
* procBet2()	determines if bets are winners
* isNum()	boolean to check if input is a number
* looksLike()	makes sure bet string is properly formatted
* isTrdCol()	boolean, is number in third column
* isFstCol()	boolean, is number in first column
* isEven()	boolean, is number even
* isRed()	boolean, is number red
* isOneTo18()	boolean, is number 1 to 18
* viewTable()	shows table layout
* howToBet()	explains betting
* rooStart()	ascii art
* help()	general help
*
*************************************************/



import java.lang.Math.*;
import java.io.*;
import java.util.*;

public class Roulette
{
	public static int roulette(int bank, String player)throws IOException{
		BufferedReader clin = new BufferedReader(new InputStreamReader(System.in));//setup for command line reading
		String action = "view";
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		rooStart();
		System.out.println("Welcome to Routlette!!!");
		System.out.println("Type \"leave\" to go to another table, type \"no bet\" to sit a game out.");
		System.out.println("Type \"view\" to see the table, type \"rules\" to see how to bet.");
		System.out.println();
		//Queue prevWins = new Queue();
		for(int i=0; i<10; i++){
			//prevWins.insert(spin());
		}
		final int SPIN = 1;
		final int BET = 2;
		final int PAYOUT = 3;
		int state = BET;

		int winNum = 38;
		boolean play = true;		
		
		while(play==true){
			switch(state){
				case SPIN: 
					System.out.println("The croupier says: \"No more bets please...\"");
					winNum = spin();
					//prevWins.insert(winNum);
					System.out.println("...roulette wheel is spinning, and stops on "+winNum);
					System.out.println("The croupier says: \"Number "+winNum+" is the winner\"");
					if(looksLike(action)){
						bank = takeAction(action, state, bank, winNum);
					}else{
						System.out.println("That didn't make sense, please check the rules.");
					}
					state = PAYOUT;
					break; 
				case BET: 
					System.out.print("Previous wins: ");
					//prevWins.showQueue();
					System.out.println("\nThe croupier says: \"Place your bets...\"");
					System.out.println();
					System.out.print("["+player+", "+bank+"]Action: ");
					action = clin.readLine();
					action = action.toLowerCase();
					if(action.equals("leave")){
						System.out.println("The croupier says: \"Good luck...\"");
						play=false;
					}
					state = SPIN;
					break;
				case PAYOUT: 
					
					System.out.println("The croupier is checking the board, removing chips and paying out.");
					System.out.println();
					System.out.print("["+player+", "+bank+"]Action: ");
					action = clin.readLine();
					action = action.toLowerCase();
					if(action.equals("leave")){
						System.out.println("The croupier says: \"Good luck...\"");
						play=false;
					}else{
						bank = takeAction(action, state, bank, winNum);
					}
					state = BET;
					break;
				default: break;
			}//end switch state machine
		}//end while
		return bank;
	}//end roulette

/******************************************************************************************************************************/
/********* other methods ******************************************************************************************************/
/******************************************************************************************************************************/


	public static int spin(){
		
		return (int)(Math.random() * 38);

	}//end spin

/******************************************************************************/

	public static int takeAction(String action, int state, int bank, int winNum){

		if(action.equals("view")){viewTable();return bank;}
		if(action.equals("rules")){howToBet();return bank;}
		if(action.equals("drink")){System.out.println("That will cloud your judgement");return bank;}
		if(action.equals("smoke")){System.out.println("You light up a big stoggie");return bank;}
		if(action.equals("curse")){System.out.println("Please, this is family establishment.");return bank;}
		if(action.equals("bank")){System.out.println("You have: "+bank);return bank;}
		if(action.equals("save")){System.out.println("You can't save in the middle of play, you must leave the game then save.");return bank;}
		if(action.equals("no bet")){System.out.println("The croupier gives you a dirty look.");return bank;}
		if(action.equals("options")){System.out.println("view, rules, drink, smoke, curse, bank, no bet, leave, help");return bank;}
		if(action.equals("help")){help();}
		if(state == 3)
		{
			System.out.println("The croupier says, \"No bets yet, please\"");
		}else{
			if(looksLike(action)){
				bank = procBet(action, winNum, bank);
			}
		}

		return bank;
	}//end take action

/******** Process bets **********************************************/
	
	public static int procBet(String action, int winNum, int bank){

		String drop;
		String action2; int totalBet = 0;
		action = action.toLowerCase();//change to lowercase
		action2 = action.toLowerCase();
		String amt;

		/*
		* This first while loop obtains the total amount of the bet
		* for comparison to the bank so the player cant bet more
		* than they have.
		*/
		
		while(action2.length()!=0){
			if(action2.indexOf(',')!=-1){
				amt = action2.substring(action2.indexOf(':')+1, action2.indexOf(','));
				if(isNum(amt)){
					totalBet = totalBet + Integer.parseInt(amt.trim());
				}
					action2 = action2.substring(action2.indexOf(',')+1);
			}else{
				System.out.println(action2 + " is not a bet");
				action2 = "";
			}
			
		}//end bank while

		/*
		* Cut up action string to process individual bets
		*
		*/

		if(bank>=totalBet){

			while(action.length()!=0){
				if(action.indexOf(',')!=-1){
				drop = action.substring(0, action.indexOf(','));
				action = action.substring(action.indexOf(',')+1);
					if(drop.indexOf(':')!=-1){
						bank = procBet2(drop.substring(0, drop.indexOf(':')), drop.substring(drop.indexOf(':')+1), winNum, bank);
					}else{
						System.out.println(drop + " is not a bet");
					}
				}else{
					System.out.println(action + " is not a bet");
					action = "";
				}

			}//While loop does flow control and error checking

		}else{		
			System.out.println("You're trying to bet more than you have. Your bank is "+bank+" and you bet "+totalBet);
		}//end bank and bet amount check
		return bank;
	}//end procBet
	

/***********************************************************************************/

	public static int procBet2(String betS, String amtS, int winNum, int bank){

		int betN = 38;

		//is amtS a number?
		if(isNum(amtS)){
			int amtN = Integer.parseInt(amtS.trim());
			bank = bank - amtN;
			//Special case 00
			if((betS.equals("00"))&&(winNum==37)){
				System.out.println("You won on 00!");
				return bank + (amtN * 35);
			}else{
				if(isNum(betS)){
					betN = Integer.parseInt(betS.trim()); 
					if((betN>=0)&&(betN<=36)){
						if(betN==winNum){
							System.out.println("You won on "+winNum+"!");
							return bank + (amtN * 35);
						}//end num payout
					}//end num range check
				}//end num check
				if((betS.equals("black"))&&(!isRed(winNum))){System.out.println("You won on black!");return bank + (amtN * 2);}
				
				if((betS.equals("red"))&&(isRed(winNum))){System.out.println("You won on red!");return bank + (amtN * 2);}
				if((betS.equals("even"))&&(isEven(winNum))){System.out.println("You won on even!");return bank + (amtN * 2);}
				if((betS.equals("odd"))&&(!isEven(winNum))){System.out.println("You won on odd!");return bank + (amtN * 2);}
				if((betS.equals("low"))&&(isOneTo18(winNum))){System.out.println("You won on low!");return bank + (amtN * 2);}
				if((betS.equals("high"))&&(!isOneTo18(winNum))){System.out.println("You won on high!");return bank + (amtN * 2);}

				if((betS.equals("1c"))&&(isFstCol(winNum))){System.out.println("You won on first column!");return bank + (amtN * 3);}
				if((betS.equals("2c"))&&(!(isFstCol(winNum)|isTrdCol(winNum)))){System.out.println("You won on second column0!");return bank + (amtN * 3);}
				if((betS.equals("3c"))&&(isTrdCol(winNum))){System.out.println("You won on third column!");return bank + (amtN * 3);}

				if((betS.equals("1d"))&&(isFrt12(winNum))){System.out.println("You won on first dozen!");return bank + (amtN * 3);}
				if((betS.equals("2d"))&&(!(isFrt12(winNum)|isTrd12(winNum)))){System.out.println("You won on second dozen!");return bank + (amtN * 3);}
				if((betS.equals("3d"))&&(isTrd12(winNum))){System.out.println("You won on third dozen!");return bank + (amtN * 3);}


			}//end special case if 00
		}else{
			
			System.out.println(betS+":"+amtS+" is not a bet");
			
		}//end main if to check if amt is a num
		return bank;

	}//end procBet2

/******** Determine if string is a number **********************************************/

	public static boolean isNum(String s)
	{
		int count=0;
		for(int j=0; j<s.length(); j++)
		{
			if (Character.isDigit(s.charAt(j)))
			count++;
		}
		if(count==s.length())
			return true;
		else 
			return false;
	}//end isNum
    


/****************************************************************************************************************/
/*********** board booleans *************************************************************************************/
/****************************************************************************************************************/


/******* column booleans *******************************/

	public static boolean isTrdCol(int winNum){
		if(winNum%3==0){
			return true;
		}
	return false;
	}//end isTrdCol

	public static boolean isFstCol(int winNum){
		if(((winNum-1)%3==0)||(winNum==1)){
			return true;
		}
	return false;
	}//end isFstCol


/****** 12 section booleans ***************************/

	public static boolean isFrt12(int winNum){
		if(winNum <= 12){
			return true;
		}
	return false;
	}//end isFrt12

	public static boolean isTrd12(int winNum){
		if(winNum >=25){
			return true;
		}
	return false;
	}//end isTrd12


/****** odd&even, red&black, 1 to 18, 18 to 36 *********/

	public static boolean isEven(int winNum){
		if(winNum%2==0){
			return true;						
		}
		return false;
	}//end isEven

	public static boolean isRed(int winNum){	
		int red[] = {1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};
		for(int i=0;i<red.length;i++){
			if(winNum==red[i]){
				return true;
			}
		}
		return false;
	}//end isRed

	public static boolean isOneTo18(int winNum){
		if(winNum<=18){
			return true;
		}
		return false;
	}//end isOneTo18
	
	public static boolean looksLike(String action){
		boolean lookslike = true;
		if(action.length()>1){
			if(action.charAt(action.length()-1)!=','){
				lookslike = false;
			}
			if(action.indexOf(':')==-1){
				lookslike = false;
			}
			if((action.charAt(0)==',')||(action.charAt(0)==':')){
				lookslike = false;
			}
			for(int i = 0; i<action.length()-2;i++){
				if((action.charAt(i)==',')&&(action.charAt(i+1)==',')){
					lookslike = false;
				}
				if((action.charAt(i)==':')&&(action.charAt(i+1)==',')){
					lookslike = false;
				}
				if((action.charAt(i)==':')&&(action.charAt(i+1)==':')){
					lookslike = false;
				}
				if((action.charAt(i)==',')&&(action.charAt(i+1)==':')){
					lookslike = false;
				}

			}
		}else{
			lookslike = false;
		}
		return lookslike;
	}


/******************************************************************************/
/********* Displays************************************************************/
/******************************************************************************/

/***********Table view********************/

	public static void viewTable(){
		System.out.println();
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Table Layout:");
		System.out.println("\t----------------------------------------------------------------");
		System.out.println("\t| 19 to 36\t| ODD\t| Black\t| Red\t| EVEN\t| 1 to 18  |");
		System.out.println("\t----------------------------------------------------------------");
		System.out.println("\t|\t3rd 12\t|\t2nd 12\t\t|\t1st 12\t\t|");
		System.out.println("\t----------------------------------------------------------------");
		System.out.println("1st Col\t| 34r| 31b| 28b| 25r| 22b| 19r| 16r| 13b| 10b| 7r | 4b | 1r | 0 ");
		System.out.println("\t-------------------------------------------------------------");
		System.out.println("2nd Col\t| 35b| 32r| 29b| 26b| 23r| 20b| 17b| 14r| 11b| 8b | 5r | 2b |---");
		System.out.println("\t-------------------------------------------------------------");
		System.out.println("3rd Col\t| 36r| 33b| 30r| 27r| 24b| 21r| 18r| 15b| 12r| 9r | 6b | 3r | 00");
		System.out.println("\t----------------------------------------------------------------");
		System.out.println("The lower case letter next to each number\n represents the color of the square: r = red, b = black");
		System.out.println();
		System.out.println("========================================================");
		System.out.println("--------------------------------------------------------------------------------");
	}//viewTable()


/************** Rules ***************************/

	public static void howToBet(){
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("How to bet: ");
		System.out.println("bet:amount,newbet:newamount");
		System.out.println("example:  \"23:10,black:20,\" would be 10 dollars on 23 and 20 dollars on black");
		System.out.println("All bets must be placed in this format, even if it's only one. \"BET\":\"AMOUNT\",");
		System.out.println();
		System.out.println("Enter number squares by the number, enter other bets by these values:");
		System.out.println("ODD - odd, EVEN - even, Black - black, Red - red");
		System.out.println("1st Col - 1c, 2nd Col - 2c, 3rd Col - 3c");
		System.out.println("19 to 36 - high, 1 to 18 - low");
		System.out.println("3rd 12 - 3d, 2nd 12 - 2d, 1st 12 - 1d");
		System.out.println();
		System.out.println("Payouts: ");
		System.out.println("Numbers pay 35 to 1");
		System.out.println("Odd/Even, Red/Black, Low/high pay 1 to 1");
		System.out.println("Columns and Dozens pay 3 to 1");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println();
	}//end howToBet	

	public static void rooStart(){
		System.out.println("                         |*|2222222222222222222222222");
		System.out.println("2222222222  33333333333  |*|2          22          22");
		System.out.println("2222222222  33333333333  |*|2          22          22");
		System.out.println("        22           33  |*|222222222  2222222222  22");
		System.out.println("        22           33  |*|222222222  2222222222  22");
		System.out.println("        22           33  |*|222222222  2222222222  22");
		System.out.println("2222222222     33333333  |*|2          22          22");
		System.out.println("2222222222     33333333  |*|2          22          22");
		System.out.println("22                   33  |*|2  2222222222  2222222222");
		System.out.println("22                   33  |*|2  2222222222  2222222222");
		System.out.println("22                   33  |*|2  2222222222  2222222222");
		System.out.println("2222222222   3333333333  |*|2          22          22");
		System.out.println("2222222222   3333333333  |*|2          22          22");
		System.out.println("                         |*|2222222222222222222222222");
		System.out.println("--------------------------*--------------------------");
		System.out.println("2222222222222222222222222|*|                         ");
		System.out.println("2          22          22|*|  11111      9999999999  ");
		System.out.println();

	}

	public static void help(){
		System.out.println();
		System.out.println("The prompt shows your name and how much is in your bank");
		System.out.println("[joe, 50]Action: ");
		System.out.println("  ^    ^");
		System.out.println("  |    |");
		System.out.println(" name bank");
		System.out.println("Your bank does not include whatever money is in play.");
		System.out.println("Type \"leave\" to leave a table and return to the casino floor");
		System.out.println();

	}//end help



}//end class
