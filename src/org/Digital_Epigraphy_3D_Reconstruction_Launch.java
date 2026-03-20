package org;

import java.io.IOException;
import java.net.URISyntaxException;

public class Digital_Epigraphy_3D_Reconstruction_Launch {
	public static void main(String args[]) {
		/*String javaPath = System.getProperty("java.home") + "\\bin\\java.exe";
		try {
			Runtime.getRuntime().exec("" + javaPath + " -Xmx1024m -cp DEAsfs.jar org.Digital_Epigraphy_3D_Reconstruction_App");
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		String pathToJar;
		try {
			pathToJar = Digital_Epigraphy_3D_Reconstruction_App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		      ProcessBuilder pb = new ProcessBuilder("java","-Xmx1024m", "-classpath", pathToJar, "org.Digital_Epigraphy_3D_Reconstruction_App");
		      pb.start();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
