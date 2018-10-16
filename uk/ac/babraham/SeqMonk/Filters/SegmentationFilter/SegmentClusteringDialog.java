package uk.ac.babraham.SeqMonk.Filters.SegmentationFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import uk.ac.babraham.SeqMonk.SeqMonkApplication;
import uk.ac.babraham.SeqMonk.Utilities.AxisScale;

/**
 * This class takes in an array of segments and plots out a graph
 * of the mean values.  It then allows you to place dividers on 
 * the graph to divide the segments into groups which can then
 * be used to split all of the segments into subgroups afterwards
 * 
 * @author andrewss
 *
 */

public class SegmentClusteringDialog extends JDialog {
	
	private ClusteredSegment [] segments;
	private HashSet<DrawnBoundary> boundaries = new HashSet<DrawnBoundary>();
	
	private BoundaryGraph boundaryGraph = new BoundaryGraph();
	
	private float minValue;
	private float maxValue;
	

	public SegmentClusteringDialog (ClusteredSegment [] segments) {
		super(SeqMonkApplication.getInstance(),"Cluster Segments");
		this.segments = segments;
		
		for (int s=0;s<segments.length;s++) {
			if (s==0) {
				minValue = segments[s].mean;
				maxValue = segments[s].mean;
			}
			if (segments[s].mean > maxValue) maxValue = segments[s].mean;
			if (segments[s].mean < minValue) minValue = segments[s].mean;
		}
		
		boundaries.add(new DrawnBoundary(minValue + ((maxValue-minValue)/2)));
		
		getContentPane().setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		
		JButton boundaryButton = new JButton("Add Boundary");
		boundaryButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boundaries.add(new DrawnBoundary(minValue + ((maxValue-minValue)/2)));
				repaint();
			}
		});
		
		JButton clusterButton = new JButton("Cluster");
		clusterButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		
		getContentPane().add(boundaryGraph, BorderLayout.CENTER);
		
		
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setSize(700,500);
		setLocationRelativeTo(SeqMonkApplication.getInstance());
		setVisible(true);
		
	}
	
	public ClusteredSegment [][] getClusteredSegments () {
		return null;
	}
	

	private class DrawnBoundary {
		
		private float value;
		private int lastY = 0;
		
		public DrawnBoundary (float value) {
			this.value = value;
		}
		
	}
	
	
	private class BoundaryGraph extends JPanel implements MouseListener, MouseMotionListener{
		
		private DrawnBoundary selectedBoundary = null;
		
		public void paint (Graphics g) {
			super.paint(g);
			
			addMouseListener(this);
			addMouseMotionListener(this);
			
			g.setColor(Color.WHITE);
			g.fillRect(0,0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			
			AxisScale as = new AxisScale(minValue, maxValue);
			
			int xOffset = as.getXSpaceNeeded()+5;
			
			g.drawLine(xOffset, 0, xOffset, getHeight());
			
			// Draw the axis on the left
			for (double value=as.getStartingValue();value <= as.getMax(); value+=as.getInterval()) {
				String text = as.format(value);
				g.drawLine(xOffset-2, getY(value), xOffset, getY(value));
				
				g.drawString(text, xOffset-(3+g.getFontMetrics().stringWidth(text)),getY(value));
			}
			
			// Draw the mean values
			
			for (int s=0;s<segments.length;s++) {
				
				int x = xOffset + (int)((getWidth()-(5+xOffset))*(s/(double)(segments.length-1)));
				
				g.drawOval(x,getY(segments[s].mean)-2, 4, 4);
			}
			
			Iterator<DrawnBoundary> b = boundaries.iterator();
			while (b.hasNext()) {
				
				DrawnBoundary boundary = b.next();
				
				if (selectedBoundary != null && selectedBoundary == boundary) continue;
				
				int y = getY(boundary.value);
				boundary.lastY = y;
				
				g.drawLine(xOffset, y, getWidth(), y);
			}
			
			if (selectedBoundary != null) {
				g.setColor(Color.RED);
				int y = getY(selectedBoundary.value);
				selectedBoundary.lastY = y;
				g.drawLine(xOffset,y,getWidth(),y);
			}
			
			
		}
		
		
		public int getY (double value) {
			double proportion = (value-minValue) / (maxValue-minValue);
			
			return getHeight()-(int) (getHeight()*proportion);
		}


		@Override
		public void mouseDragged(MouseEvent me) {
			if (selectedBoundary != null) {
				int height = getHeight()-me.getY();
				
				float proportion = height/(float)getHeight();
				
				float value = minValue + ((maxValue-minValue)*proportion);
				
				selectedBoundary.value = value;
				selectedBoundary.lastY = me.getY();
				repaint();
			}

		}


		@Override
		public void mouseMoved(MouseEvent arg0) {}


		@Override
		public void mouseClicked(MouseEvent e) {}


		@Override
		public void mouseEntered(MouseEvent e) {}


		@Override
		public void mouseExited(MouseEvent e) {}


		@Override
		public void mousePressed(MouseEvent me) {
			// Find the closest bounary
			DrawnBoundary closestBoundary = null;
			int distance = getHeight();
			
			Iterator<DrawnBoundary> it = boundaries.iterator();
			
			while (it.hasNext()) {
				DrawnBoundary b = it.next();
				int thisDistance = Math.abs(me.getY()-b.lastY);
				if (thisDistance < distance) {
					closestBoundary = b;
					distance = thisDistance;
				}
				
				if (distance <= 3) {
					selectedBoundary = closestBoundary;
					repaint();
				}
				
			}
			
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			selectedBoundary = null;
			repaint();
		}
		
	}
	
	
}
