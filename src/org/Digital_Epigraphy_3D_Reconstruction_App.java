package org;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.ufl.digitalworlds.gui.DWApp;
import gui.ImageListPanel;
import gui.ImageViewerPanel;
import gui.ReconstructionViewerPanel;
import gui.RegistrationViewerPanel;
import gui.TabPanel;


public class Digital_Epigraphy_3D_Reconstruction_App extends DWApp{

	ImageListPanel images;
	TabPanel tabbedPanel;
	public ImageViewerPanel viewerPanel;
	public RegistrationViewerPanel registrationPanel;
	public ReconstructionViewerPanel reconstructionPanel;
	
	public static String most_recent_path="";
	public static boolean algorithm_is_running=false;
	
	@Override
	public void GUIsetup(JPanel p_root) {
		
		images=new ImageListPanel(this);
		p_root.add(images,BorderLayout.WEST);
		
		viewerPanel=new ImageViewerPanel();
		registrationPanel=new RegistrationViewerPanel();
		reconstructionPanel=new ReconstructionViewerPanel(images.images);
		
		tabbedPanel=new TabPanel(JTabbedPane.TOP);
		tabbedPanel.addTab("View", null, viewerPanel, "Game panel");
		tabbedPanel.addTab("Registration", null, registrationPanel, "Settings panel");
		tabbedPanel.addTab("3D Reconstruction", null, reconstructionPanel, "Settings panel");
		p_root.add(tabbedPanel,BorderLayout.CENTER);
		
		/*try {
			images.addImage(ImageIO.read(new FileInputStream(new File("D:\\DevelopmentTools\\Squeezes_BetterLtCamera_sRGB\\1a_filtered_sm2.png"))),"D:\\DevelopmentTools\\Squeezes_BetterLtCamera_sRGB\\1a_filtered_sm2.png");
			images.addImage(ImageIO.read(new FileInputStream(new File("D:\\DevelopmentTools\\Squeezes_BetterLtCamera_sRGB\\1b_filtered_sm2.png"))),"D:\\DevelopmentTools\\Squeezes_BetterLtCamera_sRGB\\1b_filtered_sm2.png");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}*/
		
	}
	
	public static void setAlgorithmBlock(boolean flag)
	{
		algorithm_is_running=flag;
	}
	
	public static boolean algorithmBlock()
	{
		if(algorithm_is_running) 
		{
			showInformationDialog("Algorithm is running", "You are not allowed to use this function while the algorithm is running!");
			return true;
		}
		else return false;
	}
	
	public static void main(String args[]) {
    	createMainFrame("www.digitalepigraphy.org - Machine Vision and Applications 21(6), 2010, pp. 989-998");
    	app=new Digital_Epigraphy_3D_Reconstruction_App();
    	InputStream icon=null;
    	icon=Digital_Epigraphy_3D_Reconstruction_App.class.getClassLoader().getResourceAsStream("data/DEAicon_72.png");
    	setFrameSize(930,570,icon);
    }

}
