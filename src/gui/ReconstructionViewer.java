package gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JComponent;

import org.Digital_Epigraphy_3D_Reconstruction_App;

public class ReconstructionViewer extends JComponent implements MouseMotionListener, MouseListener
{
	int zoom;
	private int prevMouseX, prevMouseY;
	
	double transX=0;
	double transY=0;
	double scale=1;
	
	double p1x,p2x,p1y,p2y;
	
	boolean editing_roi=true;
	
	List<ImageStructure> images;
	
	BufferedImage heightmap;
	
	public ReconstructionViewer(List<ImageStructure> images)
	{
		this.images=images;
		reset();
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
	}
	
	public void setEditingROI(boolean flag)
	{
		this.editing_roi=flag;
		if(flag==true)
		{
			p1x=0;
			p1y=0;
			p2x=0;
			p2y=0;
			repaint();
		}
	}
	
	public void reset()
	{
		p1x=0;
		p1y=0;
		p2x=0;
		p2y=0;
		
		zoom=0;
		transX=0;
		transY=0;
		scale=1;
		repaint();
	}
	
	
	public void setZoom(int zoom)
	{
		this.zoom=zoom;
		repaint();
	}
	
	
	public void paint(Graphics g)
	{
		Graphics2D g_ = (Graphics2D)g;
		
		g_.setColor(new Color(192,192,192));
		g_.fillRect(0, 0, getWidth(), getHeight());
	
			ImageStructure image;
			Image img;

			if(images.size()<1)return;
			image=images.get(0);
			if(image.image==null) return;
			img=image.image;
			
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
			g_.scale(scale,scale);
			g_.translate(transX, transY);
			image.draw(g_,this);

			if(images.size()>=2 && images.get(1).image!=null)
			{
				image=images.get(1);
				img=image.image;
				Composite originalComposite = g_.getComposite();
				g_.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				image.draw(g_,this);
				g_.setComposite(originalComposite);
			}
			
			if(heightmap!=null)
				g.drawImage(heightmap, (int)Math.min(p1x, p2x), (int)Math.min(p1y, p2y), (int)Math.max(p1x, p2x), (int)Math.max(p1y, p2y), 0, 0, heightmap.getWidth(), heightmap.getHeight(), this);
			
			g_.setColor(new Color(255,0,0));
			g_.drawRect((int)Math.min(p1x, p2x), (int)Math.min(p1y, p2y), (int)Math.abs(p1x-p2x),(int)Math.abs(p1y-p2y));
			
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
	   
	    if(editing_roi)
	    {
		    p1x=(prevMouseX-getWidth()/2)/scale-transX;
		    p1y=(prevMouseY-getHeight()/2)/scale-transY;
		    p2x=p1x;
		    p2y=p1y;
		}
	    repaint();
	}

	public void mouseReleased(MouseEvent arg0) {
		
	}

	public void mouseDragged(MouseEvent arg0) {
		int x=arg0.getX();
		int y=arg0.getY();

		if(!editing_roi)
		{
			transX+=(x-prevMouseX+0d)/(0d+scale);
			transY+=(y-prevMouseY+0d)/(0d+scale);
		}
		prevMouseX = arg0.getX();
	    prevMouseY = arg0.getY();
	    
	    if(editing_roi && !Digital_Epigraphy_3D_Reconstruction_App.algorithmBlock())
	    {
	    	p2x=(prevMouseX-getWidth()/2)/scale-transX;
		    p2y=(prevMouseY-getHeight()/2)/scale-transY;
		}
	    repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
		
	}
}
