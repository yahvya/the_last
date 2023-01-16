package yahaya_rachelle.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javafx.scene.image.Image;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;

public class Scenes extends Configurable{

    private ArrayList<Scene> scenes;
    
    private Random random;

    public Scenes() throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.scenes = new ArrayList<Scene>();
        this.random = new Random();
        this.setConfig();
        this.fillScenes();
    }

    /**
     * rempli la liste des scènes
     */
    public void fillScenes(){

        @SuppressWarnings("unchecked") ArrayList<JSONObject> scenes = new ConfigGetter<JSONArray>(this).getValueOf(Config.Scenes.SCENES.key);

        scenes.forEach(sceneObject -> this.scenes.add(new Scene(sceneObject) ) );
    }   

    @Override
    protected String getConfigFilePath() {
        return "/config/scenes.json";
    }

    /**
     * 
     * @return une scène aléatoire
     */
    public Scene getRandomScene(){
        return this.scenes.get(this.random.nextInt(0,this.scenes.size() - 1) );        
    }

    public ArrayList<Scene> getScenes(){
        return this.scenes;
    }

    class Scene extends ImageDatas{
        private Image sceneImage;

        public Scene(JSONObject sceneObject){
            super(true);

            this.config = sceneObject;

            this.sceneImage = this.getImage(Config.Scenes.IMAGE.key);
        }

        public Image getSceneImage(){
            return this.sceneImage;
        }

        @Override
        protected String getConfigFilePath() {
            return null;
        }
    }
    
}
