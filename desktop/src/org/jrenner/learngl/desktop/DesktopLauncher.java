package org.jrenner.learngl.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.jrenner.learngl.Main;
import org.jrenner.learngl.View;


public class DesktopLauncher {

	public static void main (String[] args) {
		try {
			for (String arg : args) {
				processArg(arg);
			}
		} catch (Exception e) {
			System.err.println("ERROR PROCESSING COMMAND LINE ARGUMENTS:\n");
			e.printStackTrace();
			System.exit(0);
		}
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setIdleFPS(60);
		config.setForegroundFPS(60);
		config.useVsync(true);
		config.setWindowedMode(1880, 1000);
		config.setTitle("Terra - Kotlin Voxel");
		new Lwjgl3Application(new Main(), config);
	}

	private static void processArg(String arg) throws Exception {
		String[] pieces = arg.split("=");
		if (pieces[0].equals("-dist")) {
			int viewDist = Integer.parseInt(pieces[1]);
			int max = 250;
			if (viewDist > max) {
				System.out.println("VIEW DIST CANNOT BE OVER " + max);
				System.exit(0);
			} else {
				View.Companion.setMaxViewDist(viewDist);
				System.out.println("COMMAND LINE ARG VIEW DIST SET: " + viewDist);
			}

		}
	}
}
