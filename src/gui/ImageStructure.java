package gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import algo.SFSAlgo;


public class ImageStructure {

	BufferedImage image;
	Thumbnail thumbnail;
	double rotation=0;
	double translation_x=0;
	double translation_y=0;
	String path="";
		
	BufferedImage heightmap;
	
	double scale_from_the_original=1;
	private double lighting_orientation=0;
	
	public ImageStructure(BufferedImage image)
	{
		this.image=image;
		thumbnail=new Thumbnail(this,100,100);
	}
	
	public BufferedImage getHeightMap()
	{
		if(heightmap!=null) return heightmap;
		else {updateHeightmap();return heightmap;}
	}
	
	public void updateHeightmap()
	{
		SFSAlgo algo=new SFSAlgo(image.getWidth(),image.getHeight(),2);
		algo.setImage(0, image);
		double angle=getLightingOrientation();
		algo.setLightDir(0, SFSAlgo.vector(Math.cos((180-angle)*Math.PI/180),Math.sin((180-angle)*Math.PI/180),0));
		
		angle+=90;
		algo.setImage(1, ImageStructure.newImage(algo.getWidth(), algo.getHeight(), 127));
		algo.setLightDir(1, SFSAlgo.vector(Math.cos((180-angle)*Math.PI/180),Math.sin((180-angle)*Math.PI/180),0));
		
		algo.num_of_iterations=10;
		algo.dt=0.2f;
		algo.num_of_levels=1;
		algo.startSerial();
		
		heightmap=SFSAlgo.normalizeImage(SFSAlgo.convertImageY(algo.getHeightmap(),true));
	}
	
	public Thumbnail getThumbnail()
	{
		return thumbnail;
	}
	
	public void setLightingOrientation(double angle)
	{
		this.lighting_orientation=angle;
		thumbnail.updateImage();
	}
	
	public double getLightingOrientation() {return lighting_orientation;}
	
	public void resetTransform()
	{
		rotation=0;
		translation_x=0;
		translation_y=0;
	}
	
	public void draw(Graphics2D g_, ImageObserver o_)
	{
		int w=image.getWidth();
		int h=image.getHeight();
		g_.rotate(rotation);
		g_.translate(translation_x-w/2,translation_y-h/2);
		if(heightmap==null) g_.drawImage(image, 0,0,w,h,o_);	
		else g_.drawImage(heightmap, 0,0,w,h,o_);
		g_.translate(-(translation_x-w/2),-(translation_y-h/2));
		g_.rotate(-rotation);
	}
	
	public void drawCheckers(Graphics2D g_, ImageObserver o_, boolean mode)
	{
		int w=image.getWidth();
		int h=image.getHeight();
		g_.rotate(rotation);
		g_.translate(translation_x-w/2,translation_y-h/2);
		
		for(int x=0;x<w;x+=100)
		for(int y=0;y<h;y+=100)
		{
			if((mode && (x%200+y%200)%200==0)||(!mode && (x%200+y%200)%200!=0))
			{
				if(heightmap==null)
					g_.drawImage(image,x,y,Math.min(x+100,w),Math.min(y+100,h),x,y,Math.min(x+100,w),Math.min(y+100,h),o_);
				else g_.drawImage(heightmap,x,y,Math.min(x+100,w),Math.min(y+100,h),x,y,Math.min(x+100,w),Math.min(y+100,h),o_);
			}
		}
		g_.translate(-(translation_x-w/2),-(translation_y-h/2));
		g_.rotate(-rotation);
	}
	
	private double[] map(double x,double y)
	{
		double ret[]=new double[2];
		int w=image.getWidth();
		int h=image.getHeight();
		ret[0]=(x+(translation_x-w/2))*Math.cos(rotation)-(y+(translation_y-h/2))*Math.sin(rotation);
		ret[1]=(x+(translation_x-w/2))*Math.sin(rotation)+(y+(translation_y-h/2))*Math.cos(rotation);
		
		return ret;
	}
	
	public double getMinX()
	{
		int w=image.getWidth();
		int h=image.getHeight();
		double min=0;
		double p[]=map(0,0);
		min=p[0];
		p=map(w,0);if(p[0]<min)min=p[0];
		p=map(0,h);if(p[0]<min)min=p[0];
		p=map(w,h);if(p[0]<min)min=p[0];
		
		return min;
	}
	
	public double getMaxX()
	{
		int w=image.getWidth();
		int h=image.getHeight();
		double max=0;
		double p[]=map(0,0);
		max=p[0];
		p=map(w,0);if(p[0]>max)max=p[0];
		p=map(0,h);if(p[0]>max)max=p[0];
		p=map(w,h);if(p[0]>max)max=p[0];
		
		return max;
	}
	
	public double getMinY()
	{
		int w=image.getWidth();
		int h=image.getHeight();
		double min=0;
		double p[]=map(0,0);
		min=p[1];
		p=map(w,0);if(p[1]<min)min=p[1];
		p=map(0,h);if(p[1]<min)min=p[1];
		p=map(w,h);if(p[1]<min)min=p[1];
		
		return min;
	}
	
	public double getMaxY()
	{
		int w=image.getWidth();
		int h=image.getHeight();
		double max=0;
		double p[]=map(0,0);
		max=p[1];
		p=map(w,0);if(p[1]>max)max=p[1];
		p=map(0,h);if(p[1]>max)max=p[1];
		p=map(w,h);if(p[1]>max)max=p[1];
		
		return max;
	}
	
	public static BufferedImage newImage(int width, int height, int color)
	{
		BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g_= (Graphics2D) img.getGraphics();
		g_.setColor(new Color(color,color,color));
		g_.drawRect(0, 0, width, height);
		return img;
	}
	
	public BufferedImage getTransformedImage(double p1x, double p1y, double p2x, double p2y)
	{
		BufferedImage img=new BufferedImage((int)Math.abs(p1x-p2x),(int)Math.abs(p1y-p2y),BufferedImage.TYPE_INT_RGB);
		Graphics2D g_= (Graphics2D) img.getGraphics();

		int w=image.getWidth();
		int h=image.getHeight();
		
		g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g_.setColor(new Color(192,192,192));
		g_.fillRect(0, 0, img.getWidth(), img.getHeight());
		g_.translate(-p1x, -p1y);
		//g_.translate(-getMinX(), -getMinY());
		g_.rotate(rotation);
		g_.translate(translation_x-w/2,translation_y-h/2);
		g_.drawImage(image, 0,0,w,h,null);
		
		return img;
	}
	
	public int getLightingInterval()
	{
		int ret=0;
		double l=lighting_orientation;
		while(l>360)l-=360;
		if(l<=45 || l>315)
    	{
			ret=0;
    	}
    	else if(l<=135 && l>45)
    	{
    		ret=1;
    	}
    	else if(l<=225 && l>135)
    	{
    		ret=2;
    	}
    	else if(l<=315 && l>225)
    	{
    		ret=3;
    	}
		return ret;
	}
}
