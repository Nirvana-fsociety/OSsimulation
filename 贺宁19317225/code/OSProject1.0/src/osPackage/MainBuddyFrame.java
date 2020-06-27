package osPackage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class MainBuddyFrame extends JFrame {

	private final static int UNIT_LEN = 30;// 一个单元
	private final static int ROW_NUM = 6;// 行数

	private final static int COLUMN_NUM = 64;// 列数
	private final static int HEAD_WIDTH = 70;// 列头宽度
	private final static int HEAD_HEIGHT = 30;// 行头高度

	private JPanel contentPane;
	private ArrayList<ArrayList<JTextField>> levelsArrayList;

	/**
	 * Create the frame.
	 */
	public MainBuddyFrame() {
		super("BuddyAlgorithm - Main Memory");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(0, 100, COLUMN_NUM * UNIT_LEN + HEAD_WIDTH, ROW_NUM * UNIT_LEN + HEAD_HEIGHT);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel levelPanel = new JPanel();
		levelPanel.setPreferredSize(new Dimension(HEAD_WIDTH, ROW_NUM * UNIT_LEN));
		contentPane.add(levelPanel, BorderLayout.WEST);
		levelPanel.setLayout(new GridLayout(6, 0, 0, 0));
		
		JLabel lblLevel1 = new JLabel("Level-1");
		lblLevel1.setHorizontalAlignment(SwingConstants.CENTER);
		levelPanel.add(lblLevel1);
		
		JLabel lblLevel2 = new JLabel("Level-2");
		lblLevel2.setHorizontalAlignment(SwingConstants.CENTER);
		levelPanel.add(lblLevel2);
		
		JLabel lblLevel4 = new JLabel("Level-4");
		lblLevel4.setHorizontalAlignment(SwingConstants.CENTER);
		levelPanel.add(lblLevel4);
		
		JLabel lblLevel8 = new JLabel("Level-8");
		lblLevel8.setHorizontalAlignment(SwingConstants.CENTER);
		levelPanel.add(lblLevel8);
		
		JLabel lblLevel16 = new JLabel("Level-16");
		lblLevel16.setHorizontalAlignment(SwingConstants.CENTER);
		levelPanel.add(lblLevel16);
		
		JLabel lblLevel32 = new JLabel("Level-32");
		lblLevel32.setHorizontalAlignment(SwingConstants.CENTER);
		levelPanel.add(lblLevel32);

		JPanel mapPanel = new JPanel();
		contentPane.add(mapPanel, BorderLayout.CENTER);
		mapPanel.setLayout(new GridLayout(ROW_NUM, COLUMN_NUM, 0, 0));

		this.levelsArrayList = new ArrayList<ArrayList<JTextField>>();
		for (int i = 0; i < ROW_NUM; i++) {
			this.levelsArrayList.add(new ArrayList<JTextField>());
			for (int j = 0; j < COLUMN_NUM; j++) {
				this.levelsArrayList.get(i).add(new JTextField("" + j));
				mapPanel.add(this.levelsArrayList.get(i).get(j));
			}
		}
	}

	public static int getRowNum() {
		return ROW_NUM;
	}

	public static int getColumnNum() {
		return COLUMN_NUM;
	}

	public ArrayList<ArrayList<JTextField>> getLevelsArrayList() {
		return levelsArrayList;
	}

}
