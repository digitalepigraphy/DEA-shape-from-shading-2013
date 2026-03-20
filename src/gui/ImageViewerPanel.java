package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.Digital_Epigraphy_3D_Reconstruction_App;

public class ImageViewerPanel extends JPanel implements ChangeListener, ActionListener {

	ImageViewer viewer;
	JComboBox lighting;
	JSlider zoom;
	JCheckBox show_heightmap;
	
	public ImageViewerPanel() 
	{
		super(new BorderLayout());
		viewer=new ImageViewer();
		add(viewer,BorderLayout.CENTER);
		JPanel top=new JPanel(new BorderLayout());
		
		JPanel top_left=new JPanel(new BorderLayout());
		top_left.add(new JLabel("Lighting Orientation:"),BorderLayout.WEST);
		lighting=new JComboBox();
		lighting.setEnabled(false);
		lighting.addItem("0 degrees - from right to left");
		lighting.addItem("90 degrees - from top to bottom");
		lighting.addItem("180 degrees - from left to right");
		lighting.addItem("270 degrees - from bottom to top");
		lighting.addActionListener(this);
		//lighting.addActionListener(this);
		top_left.add(lighting);
		
		top.add(top_left,BorderLayout.WEST);
		
		show_heightmap=new JCheckBox("Show heightmap");
		show_heightmap.addActionListener(this);
		top.add(show_heightmap,BorderLayout.EAST);
		
		JPanel bottom=new JPanel(new BorderLayout());
		bottom.add(new JLabel("Zoom:"),BorderLayout.WEST);
		zoom=new JSlider(JSlider.HORIZONTAL,0,500,0);
		zoom.setToolTipText("Zoom in/out");
		zoom.addChangeListener(this);
		
		bottom.add(zoom,BorderLayout.CENTER);
		add(top,BorderLayout.NORTH);
		add(bottom,BorderLayout.SOUTH);
		
		enableControls(false);
	}

	public void enableControls(boolean flag)
	{
		lighting.setEnabled(flag);
		zoom.setEnabled(flag);
		show_heightmap.setEnabled(flag);
	}
	
	private ImageStructure im;
	
	public void setImage(ImageStructure im)
	{
		this.im=im;
		viewer.setImage(im);
		if(im==null) enableControls(false);
		else enableControls(true);
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
		if(Digital_Epigraphy_3D_Reconstruction_App.algorithmBlock()) return;
		
		if(e.getSource()==show_heightmap)
		{
			if(show_heightmap.isSelected())
			{
				im.updateHeightmap();
			}
			else
			{
				im.heightmap=null;
			}
			viewer.repaint();
		}
		else if(e.getSource()==lighting)
		{
			if(im!=null)
			{
				if(im.getLightingInterval()!=lighting.getSelectedIndex())
				{
					if(lighting.getSelectedIndex()==0) im.setLightingOrientation(0);
					else if(lighting.getSelectedIndex()==1)im.setLightingOrientation(90);
					else if(lighting.getSelectedIndex()==2)im.setLightingOrientation(180);
					else if(lighting.getSelectedIndex()==3)im.setLightingOrientation(270);
					ImageListPanel.dataModel.fireTableDataChanged();
					
					if(im.heightmap!=null)
					{
						im.updateHeightmap();
						viewer.repaint();
					}
				}
			}
		}
	}	
}
