package edu.ufl.digitalworlds.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;

import edu.ufl.digitalworlds.utils.ProgressListener;

public abstract class ParallelThread implements Runnable{

	protected ProgressListener progress_listener;
	protected Thread thread;
	
	public void addProgressListener(ProgressListener progress_listener)
	{
		this.progress_listener=progress_listener;
	}
	
	public void start(String name)
	{
		thread = new Thread(this);
        thread.setName(name);
        thread.start();
	}
	
	public void startSerial()
	{
		thread = new Thread(this);
        run();
	}
	
	public void stop()
	{
		thread=null;
	}

	public static float[][][] convertImage(BufferedImage image)
	{
		if(image==null) return null;
		int width=image.getWidth();
		int height=image.getHeight();
		float[][][] i=new float[3][width][height];
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				Color clr=new Color(image.getRGB(x, y));
				i[0][x][y]=(float)(Math.floor(0.299*clr.getRed()+0.587*clr.getGreen()+0.114*clr.getBlue())/255);
				i[1][x][y]=(float)(0.492*(clr.getBlue()/255.0-i[0][x][y]));
				i[2][x][y]=(float)(0.877*(clr.getRed()/255.0-i[0][x][y]));
			}
		return i;
	}

	public static BufferedImage normalizeImage(BufferedImage in)
	{
		int width=in.getWidth();
		int height=in.getHeight();
	
		BufferedImage out=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		
		long counters[]=new long[256];
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				Color clr=new Color(in.getRGB(x, y));
				counters[clr.getRed()]+=1;
				//out.setRGB(x, y, new Color(v,v,v).getRGB());
			}

		float cdf[]=new float[256];
		for(int i=0;i<256;i++)
		{
			cdf[i]=(counters[i]*1f)/(width*height);
			if(i>0) cdf[i]+=cdf[i-1];
		}

		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				Color clr=new Color(in.getRGB(x, y));
				int v=(int)(clr.getRed()*cdf[clr.getRed()]);
				out.setRGB(x, y, new Color(v,v,v).getRGB());
			}
		
		return out;
	}
	
	public static BufferedImage convertImageN(float[][][] N)
	{
		int width=N[0].length;
		int height=N[0][0].length;
		BufferedImage normalmap=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		
		float s=127.5f/255f;
		float Nz=0.1f;
		
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				
				float R=(-N[0][x][y]+1)*s;
				float G=(N[1][x][y]+1)*s;
				float N2=(float)Math.sqrt(1-N[0][x][y]*N[0][x][y]-N[1][x][y]*N[1][x][y]);
				float B=(N2+1)*s;
				
				if(R<0)R=0;else if(R>1)R=1;
				if(G<0)G=0;else if(G>1)G=1;
				if(B<0)B=0;else if(B>1)B=1;
				
				normalmap.setRGB(x, y, new Color(R,G,B).getRGB());
			}
		return normalmap;
	}
	
	public static BufferedImage convertImageYUV(float[][][] i,boolean scale)
	{
		int width=i[0].length;
		int height=i[0][0].length;
		BufferedImage heightmap_uploaded=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		float heightmap_min=i[0][0][0];
		float heightmap_max=i[0][0][0];
		if(scale)
		{
			for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				if(i[0][x][y]>heightmap_max)heightmap_max=i[0][x][y];
				if(i[0][x][y]<heightmap_min)heightmap_min=i[0][x][y];
			}
		}
		else
		{
			heightmap_min=0;
			heightmap_max=1;
		}
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				float Y_=(i[0][x][y]-heightmap_min)/(heightmap_max-heightmap_min);
				if(Y_>=0){}else{Y_=0;}
				if(Y_<=1){}else{Y_=1;}
				
				float R=Y_+i[2][x][y]/0.877f;
				float G=Y_-0.395f*i[1][x][y]-0.581f*i[2][x][y];
				float B=Y_+i[1][x][y]/0.492f;
				
				if(R<0)R=0;else if(R>1)R=1;
				if(G<0)G=0;else if(G>1)G=1;
				if(B<0)B=0;else if(B>1)B=1;
				
				heightmap_uploaded.setRGB(x, y, new Color(R,G,B).getRGB());
			}
		return heightmap_uploaded;
	}
	
	public static BufferedImage convertImageY(float[][] i,boolean scale)
	{
		int width=i.length;
		int height=i[0].length;
		BufferedImage heightmap_uploaded=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		float heightmap_min=i[0][0];
		float heightmap_max=i[0][0];
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				if(i[x][y]>heightmap_max)heightmap_max=i[x][y];
				if(i[x][y]<heightmap_min)heightmap_min=i[x][y];
			}
		if(!scale)
		{
			heightmap_min=0;
			heightmap_max=1;
		}
		
		System.out.println("Scale: "+(heightmap_max-heightmap_min));
		
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				int v=(int)(255*(i[x][y]-heightmap_min)/(heightmap_max-heightmap_min));
				if(v>=0){}else{v=0;}
				if(v<256){}else{v=255;}
				heightmap_uploaded.setRGB(x, y, new Color(v,v,v).getRGB());
			}
		return heightmap_uploaded;
	}
	
	public static int guessLightDirection(float[][] i)
	{
		int width=i.length;
		int height=i[0].length;
		float v;
		float sum[]=new float[2];
		int sum2[]=new int[2];
		
		for(int x=0;x<width-1;x++)
			for(int y=0;y<height-1;y++)
			{
				v=i[x+1][y]-i[x][y];
				sum[0]+=Math.abs(v);
				v=i[x][y+1]-i[x][y];
				sum[1]+=Math.abs(v);
			}

		int ret=0;
		if(sum[0]>sum[1])//light along x-axis
		{
			
			for(int y=0;y<height-1;y++)
			{
				float sum_plus=0;
				float sum_minus=0;
				for(int x=0;x<width-1;x++)
				{
					v=i[x+1][y]-i[x][y];
					if(v>0) sum_plus+=v;
					else sum_minus-=v;
				}
				if(sum_plus>sum_minus) sum2[0]+=1;
				else sum2[1]+=1;
			}
			if(sum2[0]>sum2[1])
				ret=180;
			else
				ret=0;
		}
		else //light along y-axis
		{
			for(int x=0;x<width-1;x++)	
			{
				float sum_plus=0;
				float sum_minus=0;
				for(int y=0;y<height-1;y++)
				{
					v=i[x][y+1]-i[x][y];
					if(v>0) sum_plus+=v;
					else sum_minus-=v;
				}
				if(sum_plus>sum_minus) sum2[0]+=1;
				else sum2[1]+=1;
			}
			if(sum2[0]>sum2[1])
				ret=90;
			else
				ret=270;

		}
		

		
		return ret;
	}
	
	public static float[] vector(double x, double y, double z)
	{
		float[] ret=new float[3];
		ret[0]=(float)x;
		ret[1]=(float)y;
		ret[2]=(float)z;
		return ret;
	}

}
