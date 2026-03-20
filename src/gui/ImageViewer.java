package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;



public class ImageViewer extends JComponent implements MouseMotionListener, MouseListener
{
	public ImageStructure im=null;
	int zoom;
	private int prevMouseX, prevMouseY;
		
	double scale;
	double transX;
	double transY;
	
	public ImageViewer()
	{
		reset();
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
	}
	
	public void reset()
	{
		zoom=0;
		scale=1;
		transX=0;
		transY=0;
	}
	
	public void setZoom(int zoom)
	{
		this.zoom=zoom;
		repaint();
	}
	
	public void setImage(ImageStructure im)
	{
		this.im=im;
		repaint();
	}
	
	public void paint(Graphics g)
	{
		Graphics2D g_ = (Graphics2D)g;
		
		g_.setColor(new Color(192,192,192));
		g_.fillRect(0, 0, getWidth(), getHeight());
	
			Image img;
			if(im==null || im.image==null) return;
			img=im.image;
			
			int w=img.getWidth(null);
			int h=img.getHeight(null);
			float sc=1;
			if(w*1.0/h>getWidth()*1.0/getHeight())
			{
				sc=(getWidth()*1f)/w;
			}
			else
			{
				sc=(getHeight()*1f)/h;
			}
			
			
			
			scale=sc*0.9+(getHeight()*zoom)/(h*25.0);
			
			g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g_.translate(getWidth()/2, getHeight()/2);
			g_.scale(scale, scale);
			g_.translate(transX, transY);
			im.draw(g_, this);	
	}

	
	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
	}

	public void mousePressed(MouseEvent arg0) {	
		prevMouseX = arg0.getX();
	    prevMouseY = arg0.getY();
	}

	public void mouseReleased(MouseEvent arg0) {
		
	}

	public void mouseDragged(MouseEvent arg0) {
		int x=arg0.getX();
		int y=arg0.getY();
		
		transX+=(x-prevMouseX+0d)/(0d+scale);
		transY+=(y-prevMouseY+0d)/(0d+scale);
		
		prevMouseX = arg0.getX();
	    prevMouseY = arg0.getY();
	    repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
		
	}
}
