package gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


@SuppressWarnings("serial")
public class TabPanel extends JTabbedPane implements ChangeListener
{	
	public TabPanel(int tabPlacement)
	{
		super(tabPlacement);
		addChangeListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		
	}
	
/*	protected void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.drawImage(ImageAssets.tabbedpane_image,0,this.getHeight()-ImageAssets.tabbedpane_image.getHeight(),ImageAssets.tabbedpane_image.getWidth(),ImageAssets.tabbedpane_image.getHeight(),this);
		super.paintComponent(g);       
    }*/

}
