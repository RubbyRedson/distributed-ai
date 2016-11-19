package behaviours;

import domain.Artifact;
import domain.Interest;
import domain.OnDone;
import jade.core.behaviours.OneShotBehaviour;

import java.util.Date;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class CreateArtworkBehaviour extends OneShotBehaviour {

    OnDone<Artifact> onDone;

    public CreateArtworkBehaviour(OnDone<Artifact> onDone){
        this.onDone = onDone;
    }

    @Override
    public void action() {
        System.out.println("Creating some kind of artwork. Its not implemented yet");
        onDone.done(new Artifact(Interest.Cabbage, "Cabbage boi", "BiggieMcNasty", "2016-09-10"));
    }
}
