package algo;

import edu.ufl.digitalworlds.gui.DWApp;
import edu.ufl.digitalworlds.utils.ParallelThread;
import gui.ImagePreviewer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;


public class SFSAlgo extends ParallelThread{

	int width;//image width
	int height;//image height
	int num_of_images=2;
	private float I[][][]; //[num_of_images][width][height]
	private float UV[][][];//[2][width+1][height+1]
	private float LightDir[][]; //[num_of_images][3]
	
	private int width_sub;
	private int height_sub;
	private float I_sub[][][]; //[num_of_images][width_sub][height_sub]
	
	public float YUV[][][];//[3][width+1][height+1]
	private float dH[][];//[width+1][height+1]
	public float N[][][]; //[2][width][height]  [0]:Nx, [1]:Ny 
	private float AB[][][]; //[5][width][height] [0]:magnitude of N before normalization, [1]:A, [2]:B, [3]:(1/2*h21-1/2*h11+1/2*h22-1/2*h12), [4]:(1/2*h12-1/2*h11+1/2*h22-1/2*h21)
	private float E[][][];//[num_of_images][width][height]
	public float SynthI[][][];
	
	private float Nz=1f;
	
	private int total_progress=0;
	
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	
	public SFSAlgo(int w, int h, int i)
	{
		this.width=w;
		this.height=h;
		this.num_of_images=i;
		this.I=new float[this.num_of_images][][];
		this.UV=new float[2][width+1][height+1];
		this.LightDir=new float[this.num_of_images][3];
		this.SynthI=new float[this.num_of_images][width][height];
	}
	
	public static float[] vector(double x, double y, double z)
	{
		float[] ret=new float[3];
		ret[0]=(float)x;
		ret[1]=(float)y;
		ret[2]=(float)z;
		return ret;
	}
	
	public void setImage(int id,BufferedImage i)
	{
		float[][][] yuv_=convertImage(i);
		this.I[id]=yuv_[0];
		for(int x=0;x<width;x++)
			for(int y=0;y<height;y++)
			{
				UV[0][x][y]+=yuv_[1][x][y]/num_of_images;
				UV[1][x][y]+=yuv_[2][x][y]/num_of_images;
			}
	}
	
	public void setLightDir(int id,float[] l)
	{
		this.LightDir[id][0]=l[0];
		this.LightDir[id][1]=l[1];
		this.LightDir[id][2]=l[2];
	}
	
	public float[][] getHeightmap()
	{
		return YUV[0];
	}
	
	private void computeNfromH()
	{
		float[][] H=YUV[0];
		for(int x=0;x<width_sub;x++)
		{
			for(int y=0;y<height_sub;y++)
			{
				N[0][x][y]=(H[x+1][y]-H[x][y]+H[x+1][y+1]-H[x][y+1])/2;
				N[1][x][y]=(H[x][y+1]-H[x][y]+H[x+1][y+1]-H[x+1][y])/2;
				AB[0][x][y]=(float)Math.sqrt(N[0][x][y]*N[0][x][y]+N[1][x][y]*N[1][x][y]+Nz*Nz);
				N[0][x][y]/=AB[0][x][y];
				N[1][x][y]/=AB[0][x][y];
				AB[1][x][y]=-1/(2*AB[0][x][y]*AB[0][x][y]*AB[0][x][y]);
				AB[2][x][y]=AB[1][x][y]*(-H[x+1][y]+H[x][y+1]);
				AB[1][x][y]*=(H[x][y]-H[x+1][y+1]);
				AB[3][x][y]=(H[x+1][y]-H[x][y]+H[x+1][y+1]-H[x][y+1])/2;
				AB[4][x][y]=(H[x][y+1]-H[x][y]+H[x+1][y+1]-H[x+1][y])/2;
			}
		}
	}
	
	private double computeEfromN()
	{
		double sum=0;
		long c=0;
		for(int i=0;i<num_of_images;i++)
		{
			for(int x=0;x<width_sub;x++)
			{
				for(int y=0;y<height_sub;y++)
				{
					//System.out.println(N[0][x][y]*N[0][x][y]+N[1][x][y]*N[1][x][y]+1/(AB[0][x][y]*AB[0][x][y]));
					//System.out.println(LightDir[i][0]*LightDir[i][0]+LightDir[i][1]*LightDir[i][1]+LightDir[i][2]*LightDir[i][2]);
					E[i][x][y]=N[0][x][y]*LightDir[i][0]+N[1][x][y]*LightDir[i][1]+Nz*LightDir[i][2]/AB[0][x][y]-I_sub[i][x][y];
					SynthI[i][x][y]=N[0][x][y]*LightDir[i][0]+N[1][x][y]*LightDir[i][1]+Nz*LightDir[i][2]/AB[0][x][y];
					sum+=Math.abs(E[i][x][y]);
					c+=1;
				}
			}
		}
		//System.out.println("Err:"+sum/c);
		return sum/c;
	}
	
	private void computeDHfromE()
	{
		for(int x=0;x<width_sub-1;x++)
		{
			for(int y=0;y<height_sub-1;y++)
			{
				dH[x+1][y+1]=0;
				for(int i=0;i<num_of_images;i++)
				{
					//QUAD (H[x],H[y],H[x+1],H[y+1])
					dH[x+1][y+1]+=2*E[i][x][y]*(LightDir[i][0]*(AB[0][x][y]/2-AB[3][x][y]*AB[1][x][y])+LightDir[i][1]*(AB[0][x][y]/2-AB[4][x][y]*AB[1][x][y])+LightDir[i][2]*(-Nz*AB[1][x][y]));
					//QUAD (H[x+1],H[y],H[x+2],H[y+1])
					dH[x+1][y+1]+=2*E[i][x+1][y]*(LightDir[i][0]*(-AB[0][x+1][y]/2+AB[3][x+1][y]*AB[2][x+1][y])+LightDir[i][1]*(AB[0][x+1][y]/2+AB[4][x+1][y]*AB[2][x+1][y])+LightDir[i][2]*(Nz*AB[2][x+1][y]));
					//QUAD (H[x],H[y+1],H[x+1],H[y+2])
					dH[x+1][y+1]+=2*E[i][x][y+1]*(LightDir[i][0]*(AB[0][x][y+1]/2-AB[3][x][y+1]*AB[2][x][y+1])+LightDir[i][1]*(-AB[0][x][y+1]/2-AB[4][x][y+1]*AB[2][x][y+1])+LightDir[i][2]*(-Nz*AB[2][x][y+1]));
					//QUAD (H[x+1],H[y+1],H[x+2],H[y+2])
					dH[x+1][y+1]+=2*E[i][x+1][y+1]*(LightDir[i][0]*(-AB[0][x+1][y+1]/2+AB[3][x+1][y+1]*AB[1][x+1][y+1])+LightDir[i][1]*(-AB[0][x+1][y+1]/2+AB[4][x+1][y+1]*AB[1][x+1][y+1])+LightDir[i][2]*(Nz*AB[1][x+1][y+1]));
				}
				
				//smoothing
				//dH[x+1][y+1]+=0.1*2*(4*H[x+1][y+1]-H[x+1][y+2]-H[x+1][y]-H[x+2][y+1]-H[x][y+1]);
			}
		}
	}
	
	private void computeHfromDH(float dt)
	{
		float[][] H=YUV[0];
		dt=(dt*2)/num_of_images;
		for(int x=1;x<width_sub;x++)
		{
			for(int y=1;y<height_sub;y++)
			{
				H[x][y]-=dt*dH[x][y];
			}
		}
	}
	
	/*
Nx derivs
11:-1/(2||N||)+(1/2*h21-1/2*h11+1/2*h22-1/2*h12)*A
12:-1/(2||N||)+(1/2*h21-1/2*h11+1/2*h22-1/2*h12)*B
21:1/(2||N||)-(1/2*h21-1/2*h11+1/2*h22-1/2*h12)*B
22:1/(2||N||)-(1/2*h21-1/2*h11+1/2*h22-1/2*h12)*A

Ny derivs
11:-1/(2||N||)+(1/2*h12-1/2*h11+1/2*h22-1/2*h21)*A
12:1/(2||N||)+(1/2*h12-1/2*h11+1/2*h22-1/2*h21)*B
21:-1/(2||N||)-(1/2*h12-1/2*h11+1/2*h22-1/2*h21)*B
22:1/(2||N||)-(1/2*h12-1/2*h11+1/2*h22-1/2*h21)*A

Nz derivs:
11:-1/(2||N||||N||||N||)*(h11-h22)=A
12:-1/(2||N||||N||||N||)*(-h21+h12)=B
21:-1/(2||N||||N||||N||)*(h21-h12)=-B
22:-1/(2||N||||N||||N||)*(-h11+h22)=-A
	*/
	
	public float dt=0.2f;
	public int num_of_iterations=100;
	double total_error=1;
	public int num_of_levels=5;
	
	@Override
	public void run()
	{

		if(progress_listener!=null) progress_listener.setMaxProgress(num_of_iterations*num_of_levels);
		
		for(int i=num_of_levels;i>=1;i--)		
		{
			sub_algo(i);
		}
		if(progress_listener!=null) progress_listener.setProgress(num_of_iterations*num_of_levels);
	}
	
	public void sub_algo(int level)
	{
		int skip=1;
		for(int i=1;i<level;i++)skip*=2;
		width_sub=(int)Math.ceil((width*1.0)/skip);
		height_sub=(int)Math.ceil((height*1.0)/skip);

		//subdivide the original images
		I_sub=new float[num_of_images][width_sub][height_sub];
		for(int im=0;im<num_of_images;im++)
		for(int x=0;x<width_sub;x+=1)
			for(int y=0;y<height_sub;y+=1)
			{
				int counter=0;
				for(int i=0;i<skip && x*skip+i<width;i+=1)
					for(int j=0;j<skip && y*skip+j<height;j+=1)
					{
						I_sub[im][x][y]+=I[im][x*skip+i][y*skip+j];
						counter+=1;
					}
				I_sub[im][x][y]/=counter;
			}
		
		
		if(YUV==null) 
		{
			YUV=new float[3][][];
			YUV[0]=new float[width_sub+1][height_sub+1];
		}
		else
		{
			float[][] YUV_tmp=new float[width_sub+1][height_sub+1];
			for(int x=0;x<width_sub;x+=1)
				for(int y=0;y<height_sub;y+=1)
					YUV_tmp[x][y]=YUV[0][(int)Math.floor(x/2.0)][(int)Math.floor(y/2.0)];//*2;
			YUV[0]=YUV_tmp;
		}
		YUV[1]=UV[0];
		YUV[2]=UV[1];
		dH=new float[width_sub+1][height_sub+1];
		N=new float[2][width_sub][height_sub];
		AB=new float[5][width_sub][height_sub];
		E=new float[num_of_images][width_sub][height_sub];
		total_error=1;
		
		for(int iter=0;iter<num_of_iterations && thread!=null;iter++)
		{
			//System.out.println(iter);
			total_progress+=1;
			if(progress_listener!=null) progress_listener.setProgress(total_progress);
			computeNfromH();
			total_error=computeEfromN();
			//System.out.println(total_error);
			computeDHfromE();
			computeHfromDH(dt);
			//if((iter+1)%250==0) saveSynthImages();
		}
		//try {ImageIO.write(convertImageYUV(YUV,true),"PNG",new File("tmp"+level+".png"));} catch (IOException e) {}
		
	}
	
	
	private void saveSynthImages()
	{
		for(int i=0;i<num_of_images;i++)
			try {
				ImageIO.write(convertImageY(SynthI[i],true), "PNG", new FileOutputStream(new File(most_recent_path+"\\synth_"+i+".png")));
			} catch (FileNotFoundException e) {} catch (IOException e) {}
		try {
			ImageIO.write(convertImageYUV(YUV,true), "PNG", new FileOutputStream(new File(most_recent_path+"\\synth_"+num_of_images+".png")));
		} catch (FileNotFoundException e) {} catch (IOException e) {}

	}
	
	private static String most_recent_path="";
	public static BufferedImage openImage()
	{
		BufferedImage out=null;
			JFileChooser chooser = new JFileChooser();
	        chooser.setFileHidingEnabled(false);
	        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	        chooser.setMultiSelectionEnabled(false);
	        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	        if(most_recent_path.length()>0)
				chooser.setCurrentDirectory(new File(most_recent_path));
	        chooser.setDialogTitle("Open an image file");
	        ImagePreviewer previewer = new ImagePreviewer(chooser);
	        previewer.setToolTipText("This is a quick preview of the selected image.");
	        chooser.setAccessory(previewer);
	        chooser.setApproveButtonText("Open"); 
	        
	        if (chooser.showOpenDialog(DWApp.app)== JFileChooser.APPROVE_OPTION) 
	        {
	        	most_recent_path=chooser.getCurrentDirectory().getAbsolutePath();
				
            	if(previewer.thumbnail==null)
            	{
            		DWApp.showErrorDialog("Image Format Error", "This image file format is not supported.");
            	}
            	else
            	{
            		try {
						out=ImageIO.read(new FileInputStream(chooser.getSelectedFile()));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
	        }
	        return out;
	}
	
	public static void saveImage(BufferedImage img)
	{
			JFileChooser chooser = new JFileChooser();
	        chooser.setFileHidingEnabled(false);
	        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	        chooser.setMultiSelectionEnabled(false);
	        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
	        if(most_recent_path.length()>0)
				chooser.setCurrentDirectory(new File(most_recent_path));
	        chooser.setDialogTitle("Save heightmap");
	        chooser.setApproveButtonText("Save"); 
	        
	        if (chooser.showSaveDialog(DWApp.app)== JFileChooser.APPROVE_OPTION) 
	        {
	        	most_recent_path=chooser.getCurrentDirectory().getAbsolutePath();
				
            	try {
					ImageIO.write(img, "PNG", new FileOutputStream(chooser.getSelectedFile()));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
          
            	
	        }
	        
	}
	
	
	
		
	public static void main(String args[]) {
	
		most_recent_path="C:\\ArchaeologyScans\\2013-09-05\\0002";
		
		BufferedImage img=openImage();
		SFSAlgo myalgo=new SFSAlgo(img.getWidth(),img.getHeight(),2);
		float[][][] i=convertImage(img);
		int angle1=guessLightDirection(i[0]);
		myalgo.setImage(0, img);
		myalgo.setLightDir(0, vector(Math.cos((180-angle1)*Math.PI/180),Math.sin((180-angle1)*Math.PI/180),0));
		System.out.println(angle1);
		
		img=openImage();
		i=convertImage(img);
		int angle2=guessLightDirection(i[0]);
		myalgo.setImage(1, img);
		myalgo.setLightDir(1, vector(Math.cos((180-angle2)*Math.PI/180),Math.sin((180-angle2)*Math.PI/180),0));
		System.out.println(angle2);
		
		myalgo.num_of_iterations=500;
		myalgo.dt=0.2f;
		myalgo.run();
		saveImage(convertImageYUV(myalgo.YUV,true));
		
		
	}

}
