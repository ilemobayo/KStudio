package com.musicplayer.aow.delegates.player

enum class PlayMode {
    SINGLE,
    default,
    LIST,
    LOOP,
    SHUFFLE;


    companion object {

        fun switchNextMode(current: PlayMode): PlayMode {
            if (current == null){
                return default
            }
            if (current == default){
                return LOOP
            }

            when (current) {
                LOOP -> return LIST
                LIST -> return SHUFFLE
                SHUFFLE -> return SINGLE
                SINGLE -> return LOOP
            }
            return default
        }
    }
}
