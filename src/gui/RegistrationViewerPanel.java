package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.Digital_Epigraphy_3D_Reconstruction_App;

public class RegistrationViewerPanel extends JPanel implements ChangeListener, ActionListener{

	RegistrationViewer viewer;
	JToggleButton rotate;
	JToggleButton together;
	JCheckBox use_alpha;
	JSlider alpha;
	JSlider zoom;
	JButton reset_transform;
	
	public RegistrationViewerPanel() 
	{
		super(new BorderLayout());
		viewer=new RegistrationViewer();
		add(viewer,BorderLayout.CENTER);
		JPanel top=new JPanel(new BorderLayout());
		
		JPanel top_left=new JPanel(new BorderLayout());
		rotate=new JToggleButton("Rotate");
		rotate.setToolTipText("Drag mouse to rotate, use space bar to toggle state.");
		rotate.setSelected(true);
		rotate.addActionListener(this);
		top_left.add(rotate,BorderLayout.WEST);
		use_alpha=new JCheckBox("Alpha Blend");
		use_alpha.setSelected(true);
		use_alpha.addActionListener(this);
		JPanel top_left2=new JPanel(new BorderLayout());
		top_left.add(top_left2);
		top_left2.add(use_alpha,BorderLayout.WEST);
		alpha=new JSlider(JSlider.HORIZONTAL,0,255,128);
		alpha.setToolTipText("Alpha value");
		alpha.addChangeListener(this);
		JPanel top_left3=new JPanel(new BorderLayout());
		top_left2.add(top_left3);
		top_left3.add(alpha,BorderLayout.WEST);
		together=new JToggleButton("Stich together");
		together.addActionListener(this);
		top_left3.add(together);
		
		top.add(top_left,BorderLayout.WEST);
		
		JPanel bottom=new JPanel(new BorderLayout());
		bottom.add(new JLabel("Zoom:"),BorderLayout.WEST);
		zoom=new JSlider(JSlider.HORIZONTAL,0,500,0);
		zoom.setToolTipText("Zoom in/out");
		zoom.addChangeListener(this);
		
		reset_transform=new JButton("Reset");
		reset_transform.addActionListener(this);
		top.add(reset_transform,BorderLayout.EAST);
		
		bottom.add(zoom,BorderLayout.CENTER);
		add(top,BorderLayout.NORTH);
		add(bottom,BorderLayout.SOUTH);
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource()==zoom)
		{
			viewer.setZoom(zoom.getValue());
		}
		else if(e.getSource()==alpha)
		{
			viewer.setAlpha(alpha.getValue()/255f);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(Digital_Epigraphy_3D_Reconstruction_App.algorithmBlock()) return;
		
		if(e.getSource()==use_alpha)
		{
			viewer.setUseAlpha(use_alpha.isSelected());
		}
		else if(e.getSource()==rotate)
		{
			viewer.setRotate(rotate.isSelected());
		}
		else if(e.getSource()==reset_transform)
		{
			if(viewer.im1!=null) viewer.im1.resetTransform();
			if(viewer.im2!=null) viewer.im2.resetTransform();
			zoom.setValue(0);
			alpha.setValue(128);
			rotate.setSelected(true);
			use_alpha.setSelected(true);
			together.setSelected(false);
			viewer.reset();
		}
		else if(e.getSource()==together)
		{
			viewer.setStitchTogether(together.isSelected());
		}
	}
	
}
