package org;

import java.io.InputStream;

import edu.ufl.digitalworlds.gui.DWApplet;

@SuppressWarnings("serial")
public class Digital_Epigraphy_3D_Reconstruction_Applet extends DWApplet
{
	public void init()
	{
		createMainFrame("www.digitalepigraphy.org - Machine Vision and Applications 21(6), 2010, pp. 989-998");
		Digital_Epigraphy_3D_Reconstruction_App.app=new Digital_Epigraphy_3D_Reconstruction_App();
    	InputStream icon=null;
    	icon=Digital_Epigraphy_3D_Reconstruction_App.class.getClassLoader().getResourceAsStream("data/DEAicon_72.png");
    	setFrameSize(930,570,icon);
	}
}

