package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;


public class Thumbnail implements Icon {
	
	int fWidth, fHeight;//, fCharHeight, fDescent; // Cached for speed
	public ImageIcon im;
	ImageStructure father;
	
	public void updateImage()
	{
		BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        paintIcon(null, image.getGraphics(), 0, 0);
        im=new ImageIcon(image);
	}
	
	public void setSize(int w,int h)
	{
		fWidth=w;
		fHeight=h;
		updateImage();
	}
	
	
 	public Thumbnail(ImageStructure father, int w, int h) {
		this.father=father;
 		setSize(w,h);
	}
	

	private void drawArrow(Graphics2D g)
	{
		g.setColor(new Color(255,0,0));
    	g.drawLine(0,0, 20, 0);
    	g.drawLine(0,0,5,5);
    	g.drawLine(0,0,5,-5);
	}

  
    public void paintIcon(Component c, Graphics g, int x, int y) {
		    	
    	Graphics2D g_=(Graphics2D)g;
    	new ImageIcon(father.image.getScaledInstance(fWidth,fHeight, Image.SCALE_SMOOTH)).paintIcon(c, g, 0, 0);
    	
    	int l=father.getLightingInterval();
    
    	if(l==0)
    	{
    		g_.translate(5, 10);
        	drawArrow(g_);
    	}
    	else if(l==1)
    	{
    		g_.rotate(-Math.PI/2);
    		g_.translate(-25, 10);
        	drawArrow(g_);
    	}
    	else if(l==2)
    	{
    		g_.rotate(Math.PI);
    		g_.translate(-25, -10);
        	drawArrow(g_);
    	}
    	else if(l==3)
    	{
    		g_.rotate(Math.PI/2);
    		g_.translate(5, -10);
        	drawArrow(g_);
    	}
	}
    
    
    public int getIconWidth() {
		return fWidth;
	}
	
    
    public int getIconHeight() {
		return fHeight;
	}
	
}