/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.musicplayer.aow.delegates.exo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat

object Samples {

    val SAMPLES = arrayOf(Sample(
            "http://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3",
            "audio_1",
            "Jazz in Paris",
            "Jazz for the masses",
            R.drawable.album_art_1), Sample(
            "http://storage.googleapis.com/automotive-media/The_Messenger.mp3",
            "audio_2",
            "The messenger",
            "Hipster guide to London",
            R.drawable.album_art_2), Sample(
            "http://storage.googleapis.com/automotive-media/Talkies.mp3",
            "audio_3",
            "Talkies",
            "If it talks like a duck and walks like a duck.",
            R.drawable.album_art_3))

    class Sample(
            uri: String, val mediaId: String, val title: String, val description: String, val bitmapResource: Int) {
        val uri: Uri

        init {
            this.uri = Uri.parse(uri)
        }

        override fun toString(): String {
            return title
        }
    }

    fun getMediaDescription(context: Context, sample: Sample): MediaDescriptionCompat {
        val extras = Bundle()
        val bitmap = getBitmap(context, sample.bitmapResource)
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
        return MediaDescriptionCompat.Builder()
                .setMediaId(sample.mediaId)
                .setIconBitmap(bitmap)
                .setTitle(sample.title)
                .setDescription(sample.description)
                .setExtras(extras)
                .build()
    }

    fun getBitmap(context: Context, @DrawableRes bitmapResource: Int): Bitmap {
        return (context.resources.getDrawable(bitmapResource) as BitmapDrawable).bitmap
    }

}
