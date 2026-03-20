package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

public class ImagePreviewer extends JComponent implements PropertyChangeListener {
    	public ImageIcon thumbnail = null;

    	private JFileChooser fc;
    	
    	public ImagePreviewer(JFileChooser fc) {
    	    setPreferredSize(new Dimension(300, 300));
    	    this.fc=fc;
    	    this.fc.addPropertyChangeListener(this);
    	}
    	
    	
    	public void propertyChange(PropertyChangeEvent e) {
    	    String prop = e.getPropertyName();
    	    if ((prop == JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)||(prop == JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
    		if(isShowing()) {
    			try
    			{
    				ImageIcon i=new ImageIcon(ImageIO.read(new FileInputStream(fc.getSelectedFile())));
    				int h=i.getIconHeight();
    				int w=i.getIconWidth();
    				if(h>w){w=(w*getHeight())/h;h=getHeight();}
    				else{h=(h*getWidth())/w;w=getWidth();}
    				thumbnail=new ImageIcon(i.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    			}
    			catch(Exception ex){thumbnail=null;
    			
    			}
    		    repaint();
    		}
    	    }
    	}

    	public void paint(Graphics g) {
    		
    	    if(thumbnail != null) {
    	    	int x = getWidth()/2 - thumbnail.getIconWidth()/2;
    	    	int y = getHeight()/2 - thumbnail.getIconHeight()/2;
    	    	if(y < 0) {
    	    		//y = 0;
    	    	}

    	    	if(x < 5) {
    	    		//x = 5;
    	    	}
    		
    	    	g.setColor(new Color(0,0,0));
    	    	g.fillRect(0, 0, getWidth(), getHeight());
    	    	
    	    	thumbnail.paintIcon(this, g, x, y);
    	   }
    	   else
    	   {g.setColor(new Color(0,0,0));
    		g.fillRect(0, 0, getWidth(), getHeight());
    	   }
    	    	
    	}
    	
    	
    }