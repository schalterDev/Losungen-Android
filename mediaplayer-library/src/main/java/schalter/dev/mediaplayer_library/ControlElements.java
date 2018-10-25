package schalter.dev.mediaplayer_library;

/**
 * Created by martin on 22.05.16.
 */
public interface ControlElements {

    //is called when a new song is loaded
    //duration is the length in milli seconds
    void init(AudioService audioService, int duration);

    void songSet(String title, String subtitle);

    //when the media player is paused
    void pause();

    //When the media player is resumed / started the first time
    void play();

    //When the media player is closed
    void cancel();

    //When music is seeked to a position
    void seekedTo(int position);
}
