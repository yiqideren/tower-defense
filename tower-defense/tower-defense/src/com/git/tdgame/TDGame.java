package com.git.tdgame;

import com.badlogic.gdx.Game;
import com.git.tdgame.screen.GameScreen;
import com.git.tdgame.screen.LevelSelectScreen;
import com.git.tdgame.screen.MenuScreen;

public class TDGame extends Game {
	private MenuScreen menuScreen;
	private LevelSelectScreen levelSelectScreen;
	private GameScreen gameScreen;

	private int screenWidth;
	private int screenHeight;

	public TDGame() {
		screenWidth = -1;
		screenHeight = -1;
	}

	public TDGame(int width, int height)
	{
		super();
		screenWidth = width;
		screenHeight = height;
	}

	@Override
	public void create() {
		
		menuScreen = new MenuScreen(this);
		levelSelectScreen = new LevelSelectScreen(this);
		gameScreen = new GameScreen(this);
		setScreen(menuScreen);
	}
	
	public void goToGameScreen()
	{
		setScreen(gameScreen);
	}

	@Override
	public void resume() {
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void dispose() {
	}
	
	public int getScreenWidth()
	{
		return screenWidth;
	}
	
	public int getScreenHeight()
	{
		return screenHeight;
	}
}