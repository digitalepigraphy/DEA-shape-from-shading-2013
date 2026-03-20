package gui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ufl.digitalworlds.net.HTTPXMLComEvent;
import edu.ufl.digitalworlds.net.HTTPXMLCommunication;
import edu.ufl.digitalworlds.utils.AutoProgress;
import edu.ufl.digitalworlds.utils.ParallelThread;
import edu.ufl.digitalworlds.utils.ProgressListener;

public class ImageUploader extends ParallelThread implements ProgressListener{

	ReconstructionViewerPanel panel;
	BufferedImage uploaded_image;
	int bytelimit=0;
	
	public ImageUploader(BufferedImage img, ReconstructionViewerPanel panel)
	{
		this.uploaded_image=img;
		this.panel=panel;
	}
	
	public ImageUploader(BufferedImage img, int bytelimit, ReconstructionViewerPanel panel)
	{
		this.uploaded_image=img;
		this.panel=panel;
		this.bytelimit=bytelimit;
	}
	
	@Override
	public void setProgress(int value) {
		panel.progress.setValue(value);
	}

	@Override
	public void setMaxProgress(int value) {
		panel.progress.setMaximum(value);
	}
	
	private String uploadImage(BufferedImage img)
	{
		String uploaded_id="";
		String keys[]=new String[3];keys[0]="width";keys[1]="height";keys[2]="file";
		String values[]=new String[3];values[0]=""+img.getWidth();values[1]=""+img.getHeight();values[2]="search.png";	
		
	HTTPXMLComEvent e=HTTPXMLCommunication.sendFileRequest("http://www.digitalepigraphy.org/upload_heightmap.php", keys, values, img);
	
	if(e.wasSuccessful())
	{
		NodeList nList = e.getDocument().getElementsByTagName("image");
		//System.out.println("-----------------------");
 
		for (int temp = 0; temp < nList.getLength(); temp++) {
 
		   Node nNode = nList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
		      Element eElement = (Element) nNode;
 
		      uploaded_id=HTTPXMLCommunication.getTagValue("id", eElement);
		     }
		}
	}
	if(uploaded_id=="_0") uploaded_id="";
	return uploaded_id;
	}

	private BufferedImage scale(BufferedImage img, double scaling_factor)
	{
		BufferedImage new_img = new BufferedImage((int)(img.getWidth()*scaling_factor), (int)(img.getHeight()*scaling_factor), BufferedImage.TYPE_INT_RGB);
		Graphics2D g_= (Graphics2D) new_img.getGraphics();
		g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g_.drawImage(img, 0,0,(int)(img.getWidth()*scaling_factor),(int)(img.getHeight()*scaling_factor),null);
		return new_img;
	}
	
	public BufferedImage getSizeLimitedImage(BufferedImage i, int bytelimit) throws IOException
	{
		double factor=1;
		boolean done=false;
		BufferedImage bi=null;
		
		for(;done==false;)
		{
			bi=scale(i,factor);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", baos);
			baos.flush();
			baos.close();
			//System.out.println(baos.size());
		    if(baos.size()<bytelimit) 
		    {
		    	done=true;
		    }
		    else factor-=0.05;
		}
		return bi;
	}

	
	@Override
	public void run() {
		AutoProgress auto=new AutoProgress(1,1.1);
		auto.addProgressListener(this);
		auto.start("AutoProgress");
		if(bytelimit>0)
			try {uploaded_image=getSizeLimitedImage(uploaded_image, bytelimit);} catch (IOException e) {e.printStackTrace();}
		
		String id=uploadImage(uploaded_image);
		auto.stop();
		panel.onImageUploaded(id,uploaded_image);
		thread=null;
	}

}
