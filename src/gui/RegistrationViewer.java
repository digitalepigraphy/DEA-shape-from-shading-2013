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

import javax.swing.JComponent;

public class RegistrationViewer extends JComponent implements MouseMotionListener, MouseListener
{
	public ImageStructure im1=null;
	public ImageStructure im2=null;
	int zoom;
	private int prevMouseX, prevMouseY;
	
	double scale;
	
	boolean rotate=true;
	
	boolean use_alpha=true;
	float alpha=0.5f;
	
	boolean stitch_together=false;
	
	public RegistrationViewer()
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
		rotate=true;
		use_alpha=true;
		alpha=0.5f;
		stitch_together=false;
		repaint();
	}
	
	public void setUseAlpha(boolean flag)
	{
		use_alpha=flag;
		repaint();
	}
	public void setAlpha(float alpha)
	{
		this.alpha=alpha;
		repaint();
	}
	
	public void setStitchTogether(boolean flag)
	{
		this.stitch_together=flag;
	}
	
	public void setRotate(boolean flag)
	{
		rotate=flag;
		repaint();
	}
	
	
	public void setZoom(int zoom)
	{
		this.zoom=zoom;
		repaint();
	}
	
	public void setImage(ImageStructure im)
	{
		this.im2=this.im1;
		this.im1=im;
		if(im==null) this.im2=null;
		repaint();
	}
	
	public void paint(Graphics g)
	{
		Graphics2D g_ = (Graphics2D)g;
		
		g_.setColor(new Color(192,192,192));
		g_.fillRect(0, 0, getWidth(), getHeight());
	
			Image img;
			if(im1==null || im1.image==null) return;
			img=im1.image;
			
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
			
			if(use_alpha)
			{
				g_.translate(getWidth()/2, getHeight()/2);
				g_.scale(scale,scale);
				im1.draw(g_,this);
				if(im2==null || im2.image==null) return;
				Composite originalComposite = g_.getComposite();
				g_.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				im2.draw(g_,this);
				g_.setComposite(originalComposite);
				}
			else
			{
				g_.translate(getWidth()/2, getHeight()/2);
				g_.scale(scale, scale);
				im1.draw(g_, this);	
			
				if(im2==null || im2.image==null) return;
				im2.drawCheckers(g_, this,true);	
			}
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
		
		
		if(im1!=null)
		{
			if(rotate)
			{
				im1.rotation+=((x-prevMouseX)+(y-prevMouseY))*0.001;
				if(im1.rotation>10.0*Math.PI/360.0) im1.rotation=10.0*Math.PI/360.0;
				if(im1.rotation<-10.0*Math.PI/360.0) im1.rotation=-10.0*Math.PI/360.0;
			}
			else
			{
				im1.translation_x+=(x-prevMouseX+0d)*Math.cos(im1.rotation)/(0d+scale)+(y-prevMouseY+0d)*Math.sin(im1.rotation)/(0d+scale);
				im1.translation_y+=-(x-prevMouseX+0d)*Math.sin(im1.rotation)/(0d+scale)+(y-prevMouseY+0d)*Math.cos(im1.rotation)/(0d+scale);
			}
		}
		
		if(stitch_together && im2!=null)
		{
			if(rotate)
			{
				im2.rotation+=((x-prevMouseX)+(y-prevMouseY))*0.001;
			}
			else
			{
				im2.translation_x+=(x-prevMouseX+0d)*Math.cos(im2.rotation)/(0d+scale)+(y-prevMouseY+0d)*Math.sin(im2.rotation)/(0d+scale);
				im2.translation_y+=-(x-prevMouseX+0d)*Math.sin(im2.rotation)/(0d+scale)+(y-prevMouseY+0d)*Math.cos(im2.rotation)/(0d+scale);
			}
		}
		prevMouseX = arg0.getX();
	    prevMouseY = arg0.getY();
	    repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
		
	}
}
