package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.Digital_Epigraphy_3D_Reconstruction_App;

import edu.ufl.digitalworlds.gui.DWApp;
import algo.SFSAlgo;

public class ImageListPanel extends JPanel implements ListSelectionListener, ActionListener{

	JButton open;
	JButton clear;
	public List<ImageStructure> images;
	JTable table;
    public static AbstractTableModel dataModel;
    int selected_row=-1;
    Digital_Epigraphy_3D_Reconstruction_App app;
    
	public ImageListPanel(Digital_Epigraphy_3D_Reconstruction_App app)
	{
		super(new BorderLayout());
		this.app=app;
		
		images=new ArrayList<ImageStructure>();
		
		Dimension d=getSize();
		d.setSize(200, d.getHeight());
		setPreferredSize(d);
		setMinimumSize(d);
		//setPreferedSize(d);
		open=new JButton("Open Image");
		open.addActionListener(this);
		add(open,BorderLayout.NORTH);
		
		clear=new JButton("Clear list");
		clear.addActionListener(this);
		add(clear,BorderLayout.SOUTH);
		
		final String[] names = {"#", "Image"};

        
        
		dataModel = new AbstractTableModel() {
            public int getColumnCount() { return names.length; }
            public int getRowCount() { return images.size();}
            public Object getValueAt(int row, int col) { 
                 
            	if (col==0) {
            		return ""+(row+1);
            	} else if (col == 1) {
            		TableColumn tc=table.getColumn("Image");
            		if(images.get(row).getThumbnail().getIconWidth()!=tc.getWidth()) images.get(row).getThumbnail().setSize(tc.getWidth(),100);
                    return images.get(row).getThumbnail().im;
                } 
            	else return "";
                
            }
            public String getColumnName(int col) {return names[col]; }
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
            public boolean isCellEditable(int row, int col) {
                //return col!=0 ? false: true;
            	return false;
            }
            public void setValueAt(Object aValue, int row, int col) {
                
            	/*if(col==0)
            	{
            		fanDTasia.list_of_volumes.GetVolume(row).participate=aValue;
            	}*/
            	
            }
        };

        table = new JTable(dataModel);
        
        table.setRowHeight(100);
        
        TableColumn col = table.getColumn("Image");
        col.setWidth(80);
        col = table.getColumn("#");
        col.setWidth(30);
        col.setMinWidth(30);
        col.setMaxWidth(30);
        
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        JScrollPane scrollpane = new JScrollPane(table);
        table.getSelectionModel().addListSelectionListener(this);
        add(scrollpane);
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed() ){
            // Row selection changed
            int r=table.getSelectedRow();
        	
            if(selected_row!=r && r!=-1)
            {
            	selected_row=r;        		
            	setCurrentImage(images.get(r)); 
                	
            }
		}	
	}
	
	public void setCurrentImage(ImageStructure im)
	{
		app.registrationPanel.viewer.setImage(im);
		app.viewerPanel.setImage(im);
		app.reconstructionPanel.viewer.repaint();
		double l=im.getLightingOrientation();
    	if(l<=45 || l>315)
    	{
    		app.viewerPanel.lighting.setSelectedIndex(0);
    	}
    	else if(l<=135 && l>45)
    	{
    		app.viewerPanel.lighting.setSelectedIndex(1);
    	}
    	else if(l<=225 && l>135)
    	{
    		app.viewerPanel.lighting.setSelectedIndex(2); 
    	}
    	else if(l<=315 && l>225)
    	{
    		app.viewerPanel.lighting.setSelectedIndex(3); 
    	}
    	if(im.heightmap==null) app.viewerPanel.show_heightmap.setSelected(false);
    	else app.viewerPanel.show_heightmap.setSelected(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(Digital_Epigraphy_3D_Reconstruction_App.algorithmBlock()) return;
		
		if(e.getSource()==open)
		{
		
		BufferedImage img=null;
		JFileChooser chooser = new JFileChooser();
        chooser.setFileHidingEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if(Digital_Epigraphy_3D_Reconstruction_App.most_recent_path.length()>0)
			chooser.setCurrentDirectory(new File(Digital_Epigraphy_3D_Reconstruction_App.most_recent_path));
        chooser.setDialogTitle("Open an image file");
        ImagePreviewer previewer = new ImagePreviewer(chooser);
        previewer.setToolTipText("This is a quick preview of the selected image.");
        chooser.setAccessory(previewer);
        chooser.setApproveButtonText("Open"); 
        
        if (chooser.showOpenDialog(DWApp.app)== JFileChooser.APPROVE_OPTION) 
        {
        	Digital_Epigraphy_3D_Reconstruction_App.most_recent_path=chooser.getCurrentDirectory().getAbsolutePath();
			
        	if(previewer.thumbnail==null)
        	{
        		DWApp.showErrorDialog("Image Format Error", "This image file format is not supported.");
        	}
        	else
        	{
        		try {
					img=ImageIO.read(new FileInputStream(chooser.getSelectedFile()));
					addImage(img,chooser.getSelectedFile().getAbsolutePath());
				} catch (FileNotFoundException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
        	}
        }
		}
		else if(e.getSource()==clear)
		{
			while(images.size()>0)images.remove(0);
			dataModel.fireTableDataChanged();
			app.viewerPanel.setImage(null);
			app.registrationPanel.viewer.setImage(null);
			app.reconstructionPanel.viewer.repaint();
		}

	}
	
	public void addImage(BufferedImage img, String path)
	{
		if(img!=null)
		{
			double scaling_factor=1;
			
			//This block was not commented in the released version 
			/*if(images.size()==0)
			{
				int max=Math.max(img.getWidth(),img.getHeight());
				if(max>5000)
				{
					scaling_factor=5000.0/max;
				}
			}
			else
			{
				scaling_factor=images.get(0).scale_from_the_original;
			}*/
			
			if(scaling_factor!=1)
			{
				BufferedImage new_img = new BufferedImage((int)(img.getWidth()*scaling_factor), (int)(img.getHeight()*scaling_factor), BufferedImage.TYPE_INT_RGB);
				Graphics2D g_= (Graphics2D) new_img.getGraphics();
				g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g_.drawImage(img, 0,0,(int)(img.getWidth()*scaling_factor),(int)(img.getHeight()*scaling_factor),null);
				img=new_img;
			}
				
			ImageStructure image=new ImageStructure(img);
			image.path=path;
			image.scale_from_the_original=scaling_factor;
			image.setLightingOrientation(SFSAlgo.guessLightDirection(SFSAlgo.convertImage(img)[0]));
			images.add(image);
			dataModel.fireTableDataChanged();
			setCurrentImage(image);
		}
	}
	
}
