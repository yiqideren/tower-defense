package com.git.tdgame.screen;

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.git.tdgame.TDGame;
import com.git.tdgame.data.DataProvider;
import com.git.tdgame.gameActor.Base;
import com.git.tdgame.gameActor.Gold;
import com.git.tdgame.gameActor.level.Enemy;
import com.git.tdgame.gameActor.level.LevelModel;
import com.git.tdgame.gameActor.level.Wave;
import com.git.tdgame.gameActor.tower.Tower;
import com.git.tdgame.gameActor.tower.TowerConstructButton;
import com.git.tdgame.gameActor.tower.TowerRemoveButton;
import com.git.tdgame.gameActor.tower.TowerUpgradeButton;
import com.git.tdgame.map.TDGameMapHelper;


public class GameScreen implements Screen, InputProcessor{

	// To access game functions
	private TDGame game;
	
	private Tower hoveredTower;

	// Stage
	private Stage stage;
	private Image splashImage;
	private boolean defeat = false;
	private boolean victory = false;
	private List<Wave> waves;
	private int currentWave = 0;
	private LevelModel levelModel;
	private Gold gold;
	
	private HashMap<String, HashMap<String,String>> enemyTypes;
	private HashMap<String, HashMap<String,String>> towerTypes;

	// Map variables
	private TDGameMapHelper tdGameMapHelper;
	private Array<Array<Vector2>> paths;
	private Vector2 tileSize;
	
	// Wave variables
	private float spawnTime = 0;
	private int spawnLeft;
	private final float spawnDelay = 0.5f;
	private float waveDelay;
	private int totalSpawnLeft = 0;
	
	// Tower gui popup
	private TowerUpgradeButton towerUpgradeButton;
	private TowerRemoveButton towerRemoveButton;
	
	// Selected tower
	private TowerConstructButton selectedTower; 

	public GameScreen(TDGame game, LevelModel levelModel)
	{
		this.game = game;
		this.levelModel = levelModel;
		this.enemyTypes = DataProvider.getEnemyTypes();
		this.towerTypes = DataProvider.getTowerTypes();
		this.waves = levelModel.getWaveList();
	}
	
	@Override
	public void render(float delta)
	{
		// Clear screen
        Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );

        // Map render
		tdGameMapHelper.render();

		// Stage update
		if(!defeat && !victory)
			stage.act(delta);
        stage.draw();

        // Spawn enemies
        waveDelay -= delta;
        if(waveDelay < 0 && !defeat && spawnLeft <= 0)
        {
        	if(waves.size() > currentWave)
        	{
        		if(currentWave+1 == waves.size())
        			waveDelay = 0;
        		else
        			waveDelay = waves.get(currentWave+1).getDelay();
        		
        		if(waves.get(currentWave).getEnemies() != null)
        			spawnLeft = waves.get(currentWave).getEnemies().size();
        		else
        			spawnLeft = 0;
        		
        		currentWave++;
        	} else if(totalSpawnLeft <= 0) {
        		// To Do : Victory
        		boolean isKilledAll = true;
            	Array<Actor> actors = stage.getActors();
            	for(Actor a: actors) {
            		if(a instanceof Enemy)
            		{
            			Enemy e = (Enemy)a;
            			if(e.isAlive())
            			{
            				isKilledAll = false;
            				break;
            			}
            		}
            	}
            	if(isKilledAll)
            	{
            		victory();
            	}
        		
        	}
        }
        
        if(spawnLeft > 0 && currentWave > 0)
        {
        	spawnTime += delta;
            if(spawnTime > spawnDelay)
            {
            	spawnTime = 0;
        		
                // TO DO : Spawn from selected path
                for(Array<Vector2> path : paths)
                {
                	String currentEnemy = waves.get(currentWave-1).getEnemies().get(waves.get(currentWave-1).getEnemies().size()-spawnLeft);
                	Enemy e = new Enemy(path, enemyTypes.get(currentEnemy));
	                stage.addActor(e);
                }
                
                --spawnLeft;
                --totalSpawnLeft;
            }
        }
	}
	
	private void victory() {
		splashImage = new Image(new Texture(Gdx.files.internal("data/game/victory.png")));
		splashImage.setPosition(tdGameMapHelper.getWidth()*0.25f, tdGameMapHelper.getHeight()*0.25f);

		stage.addActor(splashImage);
		if(!victory)
			game.unlockLevels(game.getUnlockedLevels()+1);
		
		victory = true;
	}

	@Override
	public void resize(int width, int height)
	{
	}

	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(this);
		// Map load
		tdGameMapHelper = new TDGameMapHelper();
		tdGameMapHelper.setPackerDirectory("data/packer");
		tdGameMapHelper.loadMap(levelModel.getMapPath());
		tileSize = new Vector2(tdGameMapHelper.getMap().tileWidth,tdGameMapHelper.getMap().tileHeight);

		// Set paths
		Array<Vector2> spawnPoints = tdGameMapHelper.getStartPoints();
		paths = new Array<Array<Vector2>>();
		for(Vector2 spawnPoint : spawnPoints)
		{
			paths.add(tdGameMapHelper.getPath(spawnPoint));
		}
		
		// Camera configuration
		tdGameMapHelper.prepareCamera(game.getScreenWidth(), game.getScreenHeight());
		tdGameMapHelper.getCamera().viewportWidth = tdGameMapHelper.getWidth();
		tdGameMapHelper.getCamera().viewportHeight = tdGameMapHelper.getHeight();
		tdGameMapHelper.getCamera().position.x = tdGameMapHelper.getWidth()/2;
		tdGameMapHelper.getCamera().position.y = tdGameMapHelper.getHeight()/2;
		tdGameMapHelper.getCamera().update();

		// Stage configuration
		stage = new Stage();
		stage.setCamera(new OrthographicCamera(game.getScreenWidth(),game.getScreenHeight()));
		stage.getCamera().rotate(180,1,0,0);
		stage.getCamera().update();
		stage.setViewport(tdGameMapHelper.getWidth(), tdGameMapHelper.getHeight(), false);
		
		gold = new Gold(new Vector2(0,(tdGameMapHelper.getMap().height-1)*tileSize.y), levelModel.getGold());
		stage.addActor(gold);
		
		Vector2 endPoint = tdGameMapHelper.getEndPoint();
		stage.addActor(new Base(new Vector2(endPoint.x*tileSize.x,endPoint.y*tileSize.y),this, levelModel.getBaseHealth()));
		
		int guiPosition = 0;
		for( String key : towerTypes.keySet()  )
		{
			TowerConstructButton btn = new TowerConstructButton(towerTypes.get(key).get("texturePath"), key, Integer.valueOf(towerTypes.get(key).get("range")));
			btn.setPosition(guiPosition, 0);
			
			guiPosition += 64;
			
			stage.addActor( btn );
		}
		
		if(waves.size()>currentWave)
		{
			waveDelay = waves.get(currentWave).getDelay();
		}
		totalSpawnLeft = 0;
		for(Wave w : waves)
		{
			totalSpawnLeft += w.getEnemies().size();
		}
	}
	
	public void defeat()
	{
		splashImage = new Image(new Texture(Gdx.files.internal("data/game/defeat.png")));
		splashImage.setPosition(tdGameMapHelper.getWidth()*0.25f, tdGameMapHelper.getHeight()*0.25f);

		stage.addActor(splashImage);
		defeat = true;
	}

	@Override
	public void hide()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(defeat || victory)
			return false;
		Vector2 hover = stage.screenToStageCoordinates(new Vector2(screenX,screenY));
		Actor a = stage.hit(hover.x,hover.y,true);
		
		if(a instanceof Tower)
		{
			hoveredTower = (Tower) a;
			
			hoveredTower.setHovered(true);
			
			towerUpgradeButton = new TowerUpgradeButton(hoveredTower);
			towerUpgradeButton.setPosition(hoveredTower.getX() + 32, hoveredTower.getY());
			towerUpgradeButton.setZIndex(2);
			
			towerRemoveButton = new TowerRemoveButton(hoveredTower);
			towerRemoveButton.setPosition(hoveredTower.getX() + 32, hoveredTower.getY() + 64);
			towerRemoveButton.setZIndex(2);
			
			stage.addActor( towerUpgradeButton );
			stage.addActor( towerRemoveButton );
		}
		
		if(a instanceof TowerConstructButton)
		{
			if( selectedTower == null )
			{
				selectedTower = new TowerConstructButton((TowerConstructButton) a);
				selectedTower.setPosition((int)(hover.x / tileSize.x) * tileSize.x, (int)(hover.y / tileSize.y) * tileSize.y);
				selectedTower.setHovered(true);
				stage.addActor(selectedTower);
			}
		}
		
		return false;
	}

	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(defeat || victory)
		{
			game.goToLevelSelectScreen();
		}
		
		if(hoveredTower != null)
		{
			hoveredTower.setHovered(false);
		}

		Vector2 hover = stage.screenToStageCoordinates(new Vector2(screenX,screenY));
		Actor a = stage.hit(hover.x,hover.y,true);
		
		if(a instanceof TowerUpgradeButton)
		{
			towerUpgradeButton = (TowerUpgradeButton) a;
			//upgrade tower
			Tower tower = towerUpgradeButton.getTower();
			if(gold.spentGold(tower.getUpgradeCost()))
			{
				tower.upgrade();
			}
			
		}
		
		if(a instanceof TowerRemoveButton)
		{
			towerRemoveButton = (TowerRemoveButton) a;
			Tower tower = towerUpgradeButton.getTower(); 
			gold.addGold(tower.getRefund());
			tower.remove();
		}
		
		if(selectedTower != null)
		{
			selectedTower.setPosition((int)(hover.x / tileSize.x) * tileSize.x, (int)(hover.y / tileSize.y) * tileSize.y);
			Vector2 constructionTile = new Vector2((int)(hover.x / tileSize.x), (int)(hover.y / tileSize.y));
			Vector2 constructionPixel = new Vector2(constructionTile.x * tileSize.x, constructionTile.y * tileSize.y);
			Tower newTower = new Tower(constructionPixel, towerTypes.get(selectedTower.getTowerName()));
			if(isConstructableTile(constructionTile) && gold.spentGold(newTower.getCost()))
			{
				stage.addActor(newTower);
			}
			selectedTower.setHovered(false);
			selectedTower.remove();
			selectedTower = null;
		}
		
		hidePopupButtons();
		return false;
	}

	private boolean isConstructableTile(Vector2 constructionTile)
	{
		int x = (int)constructionTile.x;
		int y = (int)constructionTile.y;
		
		// On first row
		if(y <= 0)
			return false;
		
		// On path
		if(tdGameMapHelper.getTiles(tdGameMapHelper.getPATH_LAYER())[y][x] != 0)
			return false;
		
		// On other tower
		for(Actor a : stage.getActors())
		{
			if(a instanceof Tower)
			{
				Tower t = (Tower) a;
				if((int)(t.getX()/tileSize.x) == x && (int)(t.getY()/tileSize.y) == y)
					return false;
			}
		}
		return true;
	}

	private void hidePopupButtons()
	{
		if(towerUpgradeButton != null)
		{
			towerUpgradeButton.remove();
			towerUpgradeButton = null;	
		}
		if(towerRemoveButton != null)
		{
			towerRemoveButton.remove();
			towerRemoveButton = null;
		}
		
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {

		Vector2 hover = stage.screenToStageCoordinates(new Vector2(screenX,screenY));
		if(selectedTower != null)
		{
			selectedTower.setPosition((int)(hover.x / tileSize.x) * tileSize.x, (int)(hover.y / tileSize.y) * tileSize.y);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
