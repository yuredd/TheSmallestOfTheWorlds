package com.yuredd.tsotw.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.yuredd.tsotw.TheSmallestOfTheWorlds;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "The Smallest Of The Worlds by yuredd";
		config.width = 1024;
		config.height = 768;
		config.resizable = false;
		new LwjglApplication(new TheSmallestOfTheWorlds(), config);
	}
}
