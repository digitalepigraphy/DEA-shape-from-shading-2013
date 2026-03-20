package gui;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.Digital_Epigraphy_3D_Reconstruction_App;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import algo.SFSAlgo;
import edu.ufl.digitalworlds.gui.DWApp;
import edu.ufl.digitalworlds.net.HTTPXMLComEvent;
import edu.ufl.digitalworlds.net.HTTPXMLCommunication;
import edu.ufl.digitalworlds.utils.ParallelThread;
import edu.ufl.digitalworlds.utils.ProgressListener;

public class ReconstructionViewerPanel extends JPanel implements ChangeListener, ActionListener, ProgressListener{

	ReconstructionViewer viewer;
	JSlider zoom;
	JButton set_largest_joint_roi;
	JButton set_smallest_containing_roi;
	JButton reconstruct;
	JButton view3d;
	JToggleButton custom_roi;
	List<ImageStructure> images;
	JProgressBar progress;
	SFSAlgo algo;
	JComboBox speed;
	
	boolean unsaved_data=false;
	
	public ReconstructionViewerPanel(List<ImageStructure> list) 
	{
		super(new BorderLayout());
		images=list;
		viewer=new ReconstructionViewer(list);
		add(viewer,BorderLayout.CENTER);
		JPanel top=new JPanel(new BorderLayout());
		
		JPanel top_left=new JPanel(new BorderLayout());
		custom_roi=new JToggleButton("Set custom region");
		custom_roi.setToolTipText("Set custom region of interest.");
		custom_roi.setSelected(true);
		custom_roi.addActionListener(this);
		top_left.add(custom_roi,BorderLayout.WEST);
		
		
		set_smallest_containing_roi=new JButton("Preset region 1");
		set_smallest_containing_roi.setToolTipText("The smallest rectangle that fully contains the images.");
		set_smallest_containing_roi.addActionListener(this);
		JPanel top_left2=new JPanel(new BorderLayout());
		top_left.add(top_left2);
		top_left2.add(set_smallest_containing_roi,BorderLayout.WEST);
				
		
		set_largest_joint_roi=new JButton("Preset region 2");
		set_largest_joint_roi.addActionListener(this);
		set_largest_joint_roi.setToolTipText("The largest rectangle that is contained in one image.");
		top_left2.add(set_largest_joint_roi,BorderLayout.CENTER);
		
		reconstruct=new JButton("Reconstruct");
		reconstruct.addActionListener(this);
		top_left2.add(reconstruct,BorderLayout.EAST);
		
		progress=new JProgressBar(0,100);
		top.add(progress,BorderLayout.CENTER);
		
		top.add(top_left,BorderLayout.WEST);
		
		JPanel bottom=new JPanel(new BorderLayout());
		bottom.add(new JLabel("Zoom:"),BorderLayout.WEST);
		zoom=new JSlider(JSlider.HORIZONTAL,0,500,0);
		zoom.setToolTipText("Zoom in/out");
		zoom.addChangeListener(this);
		
		JPanel speed_panel=new JPanel(new BorderLayout());
		speed=new JComboBox();
		speed.addItem("Fast");
		speed.addItem("Medium (more accurate)");
		speed.addItem("Slow (most accurate)");
		speed.addActionListener(this);
		speed_panel.add(new JLabel(" Speed:"),BorderLayout.WEST);
		speed_panel.add(speed,BorderLayout.CENTER);
		
		
		view3d=new JButton("Save 3D model, View & Share");
		view3d.addActionListener(this);
		view3d.setEnabled(false);
		top.add(view3d,BorderLayout.EAST);
		
		bottom.add(zoom,BorderLayout.CENTER);
		bottom.add(speed_panel,BorderLayout.EAST);
		add(top,BorderLayout.NORTH);
		add(bottom,BorderLayout.SOUTH);
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource()==zoom)
		{
			viewer.setZoom(zoom.getValue());
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!((e.getSource()==reconstruct)&&(reconstruct.getText()=="Cancel"))) 
		{
			if(Digital_Epigraphy_3D_Reconstruction_App.algorithmBlock())return;
			else
			{
				if(unsaved_data && e.getSource()!=view3d)
				{
					if(!Digital_Epigraphy_3D_Reconstruction_App.showConfirmDialog("Unsaved data", "You have not saved your results! Do you want to proceed without saving?"))
					{
						custom_roi.setSelected(false);
						viewer.setEditingROI(false);
						return;
					}
					else 
					{
						viewer.heightmap=null;
						unsaved_data=false;
						view3d.setEnabled(false);
						viewer.repaint();
					}
				}
			}
		}
		
		if(e.getSource()==custom_roi)
		{
			viewer.setEditingROI(custom_roi.isSelected());
		}
		else if(e.getSource()==set_smallest_containing_roi)
		{
			for(int i=0;i<images.size();i++)
			{
				ImageStructure img=images.get(i);
				if(i==0)
				{
					viewer.p1x=img.getMinX();
					viewer.p1y=img.getMinY();
					viewer.p2x=img.getMaxX();
					viewer.p2y=img.getMaxY();
				}
				else
				{
					double v=img.getMinX();
					if(v<viewer.p1x) viewer.p1x=v;
					v=img.getMaxX();
					if(v>viewer.p2x) viewer.p2x=v;
					v=img.getMinY();
					if(v<viewer.p1y) viewer.p1y=v;
					v=img.getMaxY();
					if(v>viewer.p2y) viewer.p2y=v;
				}
			}
			viewer.setEditingROI(false);
			viewer.repaint();
			custom_roi.setSelected(false);
		}
		else if(e.getSource()==set_largest_joint_roi)
		{
			for(int i=0;i<images.size();i++)
			{
				ImageStructure img=images.get(i);
				if(i==0)
				{
					viewer.p1x=img.getMinX();
					viewer.p1y=img.getMinY();
					viewer.p2x=img.getMaxX();
					viewer.p2y=img.getMaxY();
				}
				else
				{
					double v=img.getMinX();
					if(v>viewer.p1x) viewer.p1x=v;
					v=img.getMaxX();
					if(v<viewer.p2x) viewer.p2x=v;
					v=img.getMinY();
					if(v>viewer.p1y) viewer.p1y=v;
					v=img.getMaxY();
					if(v<viewer.p2y) viewer.p2y=v;
				}
			}
			viewer.setEditingROI(false);
			viewer.repaint();
			custom_roi.setSelected(false);
		}
		else if(e.getSource()==reconstruct)
		{
			if(reconstruct.getText()=="Reconstruct")
			{
				/*if(images.size()<2)
				{
					DWApp.showInformationDialog(this, "Information", "You need at least two images to reconstruct the 3d model!");
					return;
				}*/
				
				if(Math.abs(viewer.p1x-viewer.p2x)==0 || Math.abs(viewer.p1y-viewer.p2y)==0)
				{
					DWApp.showInformationDialog(this, "Information", "You must select first a region of interest!");
					return;
				}
				
				if(images.size()>1)
				{
					int intervals[]=new int[2];
					for(int i=0;i<images.size();i++)
						intervals[(images.get(i).getLightingInterval())%(2)]+=1;
					
					if(intervals[0]<1 || intervals[1]<1) 
					{
						DWApp.showInformationDialog(this, "Information", "The lighting directions were not set properly. (They can be set from the View tab.)");
						return;
					}
				}
				
				
				reconstruct.setEnabled(false);
				reconstruct.setText("Cancel ");
				custom_roi.setSelected(false);
				viewer.setEditingROI(false);
				for(int i=0;i<images.size();i++)
				{
					try {
						BufferedImage img=images.get(i).getTransformedImage(viewer.p1x, viewer.p1y, viewer.p2x, viewer.p2y);
						if(i==0)
						{
							if(images.size()==1) algo=new SFSAlgo(img.getWidth(),img.getHeight(),2);
							else algo=new SFSAlgo(img.getWidth(),img.getHeight(),images.size());
						}
						algo.setImage(i, img);
						double angle=images.get(i).getLightingOrientation();
						algo.setLightDir(i, SFSAlgo.vector(Math.cos((180-angle)*Math.PI/180),Math.sin((180-angle)*Math.PI/180),0));
						ImageIO.write(img,"PNG",new File(images.get(i).path+"_"+i+".png"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				if(images.size()==1)
				{
					double angle=images.get(0).getLightingOrientation()+90;
					algo.setImage(1, ImageStructure.newImage(algo.getWidth(), algo.getHeight(), 127));
					algo.setLightDir(1, SFSAlgo.vector(Math.cos((180-angle)*Math.PI/180),Math.sin((180-angle)*Math.PI/180),0));
				}
				
				if(speed.getSelectedIndex()==0) algo.num_of_iterations=50;
				else if(speed.getSelectedIndex()==1) algo.num_of_iterations=200;
				else	algo.num_of_iterations=500;
				algo.dt=0.2f;
				algo.addProgressListener(this);
				algo.start("SFSalgorithm");
				Digital_Epigraphy_3D_Reconstruction_App.setAlgorithmBlock(true);
			}
			else if(reconstruct.getText()=="Cancel")
			{
				if(algo!=null)algo.stop();
			}
		}
		else if(e.getSource()==view3d)
		{
			ImageUploader iu=new ImageUploader(SFSAlgo.convertImageYUV(algo.YUV,true),1500000,this);
			iu.start("ImageUploader");
			Digital_Epigraphy_3D_Reconstruction_App.setAlgorithmBlock(true);
		}
	}

	public void onImageUploaded(String id, BufferedImage uploaded_image)
	{
		if(id.length()>0)
		{
			unsaved_data=false;
			viewer.heightmap=null;
			viewer.repaint();
			progress.setValue(0);
			DWApp.showInformationDialog("Success", "The 3D reconstructed model was saved succesfully! Close this window to view the data in 3D.");
			DWApp.openURL("http://www.digitalepigraphy.org/viewfull?heightmap="+id);
		}
	
		//try {ImageIO.write(uploaded_image,"PNG",new File(images.get(0).path+"_uploaded.png"));} catch (IOException e) {}
		
		Digital_Epigraphy_3D_Reconstruction_App.setAlgorithmBlock(false);
	}
	
	
	@Override
	public void setMaxProgress(int val) {
		progress.setMaximum(val);
	}

	@Override
	public void setProgress(int val) {
		
		if(val==5)
		{
			reconstruct.setText("Cancel");
			reconstruct.setEnabled(true);
		}
		
		if(val<10 || val%20==0)
		{
			viewer.heightmap=SFSAlgo.normalizeImage(SFSAlgo.convertImageY(algo.getHeightmap(),true));
			viewer.repaint();
			unsaved_data=true;
			view3d.setEnabled(true);
		}
		progress.setValue(val);
		if(val==progress.getMaximum())
		{
			/*for(int i=0;i<images.size();i++)
				try {
					ImageIO.write(SFSAlgo.convertImageY(algo.SynthI[i],true), "PNG", new FileOutputStream(new File(images.get(i).path+"_synth_"+i+".png")));
				} catch (FileNotFoundException e) {} catch (IOException e) {}*/
			try {
				ImageIO.write(SFSAlgo.convertImageYUV(algo.YUV,true), "PNG", new FileOutputStream(new File(images.get(0).path+"_hist.png")));
			} catch (FileNotFoundException e) {} catch (IOException e) {}
			
			try {
				ImageIO.write(SFSAlgo.convertImageN(algo.N), "PNG", new FileOutputStream(new File(images.get(0).path+"_norm.png")));
			} catch (FileNotFoundException e) {} catch (IOException e) {}
			
			progress.setValue(0);
			reconstruct.setText("Reconstruct");
			Digital_Epigraphy_3D_Reconstruction_App.setAlgorithmBlock(false);
		}
	}
	
}
