package jmt.gui.jwat.workloadAnalysis.chart;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmt.engine.jwat.TimeConsumingWorker;
import jmt.engine.jwat.VariableNumber;
import jmt.engine.jwat.input.ProgressMonitorShow;
import jmt.engine.jwat.workloadAnalysis.WorkloadAnalysisSession;
import jmt.engine.jwat.workloadAnalysis.clustering.kMean.KMean;
import jmt.engine.jwat.workloadAnalysis.utils.JWatWorkloadManager;
import jmt.engine.jwat.workloadAnalysis.utils.JavaWatColor;
import jmt.engine.jwat.workloadAnalysis.utils.ModelWorkloadAnalysis;

import jmt.gui.common.CommonConstants;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsbase.util.export.ExportDialog;

//UPDATE 02/11/2006:	+clustered variable display
public class DispKMeanMatrix extends JScrollPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ModelWorkloadAnalysis model;
	private WorkloadAnalysisSession session;

	private DispersionPanel panel;

	public DispKMeanMatrix() {
		super(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.setPreferredSize(new Dimension((int)(420 * CommonConstants.widthScaling), (int)(420 * CommonConstants.heightScaling)));
		panel = new DispersionPanel();
		this.setViewportView(panel);
	}

	public DispKMeanMatrix(WorkloadAnalysisSession session, int clustering) {
		/* Richiamo il costruttore della classe JScorllPanel impostando le barre di scorrimento solo se necessarie */
		super(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.model = (ModelWorkloadAnalysis) session.getDataModel();
		this.session = session;
		/* Settaggio delle proprieta' del pannello di scroll */
		this.setPreferredSize(new Dimension((int)(420 * CommonConstants.widthScaling), (int)(420 * CommonConstants.heightScaling)));
		/* Creazione e aggiunta del Pannello di visualizzazione della matrice di dispersione */
		panel = new DispersionPanel(model, clustering);
		panel.setPreferredSize(new Dimension(DispersionPanel.WIDTH_TOT * model.getMatrix().getNumVariables(), DispersionPanel.HEIGHT_TOT
				* model.getMatrix().getNumVariables()));
		this.setViewportView(panel);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (g == null) {
			return;
		}
		VectorGraphics vg = VectorGraphics.create(g);
		super.paintComponent(vg);
	}

	public void setModel(ModelWorkloadAnalysis m, int clustering) {
		model = m;
		panel.setModel(m, clustering);
		panel.setPreferredSize(new Dimension(DispersionPanel.WIDTH_TOT * model.getMatrix().getNumVariables(), DispersionPanel.HEIGHT_TOT
				* model.getMatrix().getNumVariables()));
		this.setViewportView(panel);
	}

	public void setClustering(int clustering, int clust) {
		panel.setClustering(clustering, clust);
	}

	public void setCluter(int clust) {
		panel.setCluster(clust);
	}

	class DispersionPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int curClust;
		private int clustering = 0;

		public DispersionPanel() {

		}

		public DispersionPanel(ModelWorkloadAnalysis m, int clustering) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			model = m;
			this.clustering = clustering;
			initComponent();
		}

		public void setModel(ModelWorkloadAnalysis m, int clustering) {
			model = m;
			this.clustering = clustering;
			initComponent();
		}

		public void setClustering(int clustering, int clust) {
			this.clustering = clustering;
			this.curClust = clust;
			Redraw = true;
			repaint();
		}

		//To remove but verify
		public void setCluster(int clust) {
			curClust = clust;
			Redraw = true;
			repaint();
		}

		private void initComponent() {
			/* Create buffer for image */
			chart = new BufferedImage(WIDTH_TOT * model.getMatrix().getNumVariables() + 1, HEIGHT_TOT * model.getMatrix().getNumVariables() + 1,
					BufferedImage.TYPE_USHORT_555_RGB);
			Redraw = true;
			/* Addition of the mouse listener for displaying the corresponding scatter plot in case of pressing the left button on the panel
or display the possibility of saving the image in .png format with the right button */
			if (this.getMouseListeners().length > 0) {
				this.removeMouseListener(this.getMouseListeners()[0]);
			}
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					/* Visualizzazione dello scatter plot dettagliato */
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
						int x = model.getMatrix().getNumVariables() * WIDTH_TOT;
						int y = model.getMatrix().getNumVariables() * HEIGHT_TOT;
						if (e.getX() < x && e.getY() < y && e.getX() / WIDTH_TOT != e.getY() / HEIGHT_TOT) {
							final JFrame f = new JFrame();

							JWatWorkloadManager.addJMTWindow(f);
							f.addWindowListener(new WindowAdapter() {
								@Override
								public void windowClosing(WindowEvent e) {
									JWatWorkloadManager.exit(f);
								}

								@Override
								public void windowClosed(WindowEvent e) {
									JWatWorkloadManager.exit(f);
								}
							});
							f.setSize(640, 690);
							KMeanScatter s = new KMeanScatter(e.getX() / WIDTH_TOT, e.getY() / HEIGHT_TOT, session, f, clustering, curClust);
							f.setTitle("Scatter Plot " + model.getMatrix().getVariables()[e.getX() / WIDTH_TOT].getName() + " - "
									+ model.getMatrix().getVariables()[e.getY() / HEIGHT_TOT].getName());
							f.setContentPane(s);
							f.setVisible(true);
						}
					}
					/* Image saving option */
					if (e.getButton() == MouseEvent.BUTTON3) {
						showScreenShotDialog();
					}
				}
			});
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (Redraw) {
				Redraw = false;
				Graphics grap = chart.getGraphics();
				grap.setColor(Color.GRAY);
				grap.fillRect(0, 0, WIDTH_TOT * model.getMatrix().getNumVariables() + 1, HEIGHT_TOT * model.getMatrix().getNumVariables() + 1);
				grap.drawImage(chart, 0, 0, null);
				TimeConsumingWorker worker = new TimeConsumingWorker(new ProgressMonitorShow(this, "Constructin Dispersion Matrix...", 1)) {
					@Override
					public Object construct() {
						Graphics g = chart.getGraphics();
						VariableNumber Elenco[] = model.getMatrix().getVariables();
						try {
							// Image cleaning
							g.setColor(Color.WHITE);
							g.fillRect(0, 0, WIDTH_TOT * Elenco.length + 1, HEIGHT_TOT * Elenco.length + 1);

							initShow(10000 * (Elenco.length * Elenco.length));

							short[][] c1 = ((KMean) (session.getListOfClustering().get(clustering))).getAsseg();
							int[] v = ((KMean) (session.getListOfClustering().get(clustering))).getVarClust();
							int l = 0;
							// Graph drawing
							for (int row = 0; row < Elenco.length; row++) {
								for (int col = 0; col < Elenco.length; col++) {
									g.setColor(Color.BLACK);
									g.drawRect(col * WIDTH_TOT, row * HEIGHT_TOT, WIDTH_TOT, HEIGHT_TOT);
									if (col == row) {
										// Write variable
										Graphics2D gr = (Graphics2D) g;
										gr.setFont(new Font("Arial", Font.BOLD, 13));
										FontRenderContext context = gr.getFontRenderContext();
										Font f = new Font("Arial", Font.BOLD, 12);
										Rectangle2D bounds = f.getStringBounds(Elenco[row].getName(), context);

										g.setFont(new Font("Arial", Font.BOLD, 12));
										g.drawString(Elenco[row].getName(), col * WIDTH_TOT + (WIDTH_TOT - (int) bounds.getWidth()) / 2, row
												* HEIGHT_TOT - (int) bounds.getY() + (HEIGHT_TOT - (int) bounds.getHeight()) / 2);
										//UPDATE 02/11/2006:	+clustered variable display
										if (l < v.length && v[l] == col) {
											g.drawString("(Clusterized)", col * WIDTH_TOT + 10, row * HEIGHT_TOT - (int) bounds.getY()
													+ (HEIGHT_TOT - (int) bounds.getHeight()) / 2 + 15);
											l++;
										}
										g.setFont(new Font("Arial", Font.PLAIN, 12));
									} else {
										// Graph step calculation
										double yFoot = HEIGHT_GRAPH / Elenco[row].getUniStats().getRangeValue();
										double xFoot = WIDTH_GRAPH / Elenco[col].getUniStats().getRangeValue();
										// Graph plotting
										for (int i = 1; i <= WIDTH_GRAPH; i++) {
											boolean[] done = new boolean[101];
											int k = 1;
											for (int j = Elenco[col].getStartInt(i); j < Elenco[col].getEndInt(i); j++) {
												if ((int) ((Elenco[col].getValue(j, row) - Elenco[row].getUniStats().getMinValue()) * yFoot) >= 0) {
													if (!done[(int) ((Elenco[col].getValue(j, row) - Elenco[row].getUniStats().getMinValue()) * yFoot)]) {
														updateInfos(((row * Elenco.length) + (col)) * 10000 + i + (k++), "Plotting scatter "
																+ ((row * Elenco.length) + (col + 1)), false);
														g.setColor(JavaWatColor.getColor(c1[curClust][Elenco[col].getObsID(j) - 1]));
														g.fillOval(col * WIDTH_TOT + 5 +
																(int) (((Elenco[col].getValue(j) - Elenco[col].getUniStats().getMinValue())) * xFoot) - 1,
																(((row + 1) * HEIGHT_TOT) - 5 - (int) (((Elenco[col].getValue(j, row) - Elenco[row]
																		.getUniStats().getMinValue())) * yFoot)), 1, 1);
														done[(int) ((Elenco[col].getValue(j, row) - Elenco[row].getUniStats().getMinValue()) * yFoot)] = true;
													}
												}
											}
										}
									}
								}
							}
						} catch (Exception e) {
							g.setColor(Color.WHITE);
							g.fillRect(0, 0, WIDTH_TOT * model.getMatrix().getNumVariables() + 1, HEIGHT_TOT * model.getMatrix().getNumVariables() + 1);
							DispersionPanel.this.repaint();
						}
						updateInfos(10000 * (Elenco.length * Elenco.length) + 1, "End", false);
						return null;
					}

					@Override
					public void finished() {
						DispersionPanel.this.repaint();
					}
				};
				worker.start();
			} else {
				g.drawImage(chart, 0, 0, null);
			}
		}

		/* List of variables to visualize */
		private ModelWorkloadAnalysis model;
		/* Image buffer */
		private BufferedImage chart;
		/* Indicates whether or not the image should be redrawn */
		private boolean Redraw;
		/* COSTANTI */
		private static final int HEIGHT_GRAPH = 100;
		private static final int WIDTH_GRAPH = 100;
		public static final int HEIGHT_TOT = 110;
		public static final int WIDTH_TOT = 110;

		/**
		 * Shows a screenshot dialog used to select screenshot format
		 */
		public void showScreenShotDialog() {
			ExportDialog export = new ExportDialog("JWAT - version ???");
			export.showExportDialog(this, "Export as image...", this, "graph");
		}

	}

}
