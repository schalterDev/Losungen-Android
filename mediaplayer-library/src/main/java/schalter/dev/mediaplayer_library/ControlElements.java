package schalter.dev.mediaplayer_library;

/**
 * Created by martin on 22.05.16.
 */
public interface ControlElements {

    //is called when a new song is loaded
    //duration is the length in milli seconds
    public void init(int duration);

    public void songSet(String title, String subtitle);

    //when the media player is paused
    public void pause();

    //When the media player is resumed / started the first time
    public void play();

    //When the media player is closed
    public void cancel();

    //When music is seeked to a position
    public void seekedTo(int position);
}
