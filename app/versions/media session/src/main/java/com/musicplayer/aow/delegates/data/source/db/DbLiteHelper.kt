package com.musicplayer.aow.delegates.data.source.db

import android.app.Activity
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment


/**
 * Created by Arca on 12/3/2017.
 */
class DbLiteHelper(context: Activity){

    var context =context

    var db:SQLiteDatabase? = SQLiteDatabase.openDatabase(
            Environment.getExternalStorageDirectory().toString() + "/" + context.packageName + "/data/db",
            //"sdcard/myfriendsDB",
            null,
            SQLiteDatabase.CREATE_IF_NECESSARY)

    fun openDatabase() {
        try {
            db = SQLiteDatabase.openDatabase(
                    Environment.getExternalStorageDirectory().toString() + "/" + context.packageName + "/data/db",
                    //"sdcard/myfriendsDB",
                    null,
                    SQLiteDatabase.CREATE_IF_NECESSARY)
            db?.close()
        } catch (e: SQLiteException) {
        }

    }//createDatabase

    fun insertSomeDbData() {
//create table: tblAmigo
        db?.beginTransaction();
        try{
            db?.execSQL("create table tblAMIGO("
                    + " recIDinteger PRIMARY KEY autoincrement, "
                    + " name text, "
                    + " phone text ); ");
            //commit your changes
            db?.setTransactionSuccessful();

        } catch(e: SQLException) {
        }
        finally{
            //finish transaction processing
            db?.endTransaction();
        }
        // populate table: tblAmigo
        db?.beginTransaction();
        try{
            //insert rows
            db?.execSQL( "insert into tblAMIGO(name, phone) "
                    + " values ('AAA', '555' );");
            db?.execSQL("insert into tblAMIGO(name, phone) "
                    + " values ('BBB', '777' );");
            db?.execSQL("insert into tblAMIGO(name, phone) "
                    + " values ('CCC', '999' );");
            //commit your changes
            db?.setTransactionSuccessful();
        }
        catch(e: SQLiteException) {
            //report problem
        }
        finally{
            db?.endTransaction();
        }
    }//insertSomeData


}