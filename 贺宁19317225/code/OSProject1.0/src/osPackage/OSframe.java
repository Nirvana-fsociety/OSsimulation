package osPackage;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class OSframe extends JFrame {
	/**
	 * 快表显示窗口
	 */
	private TlbFrame tlbFrame = new TlbFrame();
	private PageTableFrame pgTbFrame = new PageTableFrame();
	private MainBuddyFrame buddyFrame = new MainBuddyFrame();
	/**
	 * 界面刷新线程
	 */
	private ThreadRefresh threadRefresh;
	/**
	 * 进程管理系统
	 */
	private ProcessManageSystem PMS;
	/**
	 * 作业管理系统
	 */
	private JobManageSystem JMS;

	/**
	 * 时钟线程
	 */
	private ThreadTime threadTime;
	/**
	 * DMA线程
	 */
	private ThreadDma threadDma;
	/**
	 * 请求-作业线程
	 */
	private ThreadRequests threadRequests;
	/**
	 * 作业-进程线程
	 */
	private ThreadJobs threadJobs;
	/**
	 * 进程调度线程
	 */
	private ThreadProcess threadProcess;

	private final static int UNIT_WIDTH = 68;
	private final static int UNIT_HEIGHT = 25;

	private final static int HEIGHT = 40;
	private final static int WIDTH = 28;

	private final static int TITLE_HEIGHT = 30;

	private final static int LOW_MANAGE_DOMAIN = 9;
	private final static int HIGH_MID_MANAGE_DOMAIN = 6;

	private JPanel contentPane;
	private JTextField txtPc;
	private JTextField txtIr;
	private JTextField txtPsw;
	private JTextField txtAr;
	private JTextField txtSp;
	private JTextField txtLogicadd;
	private JTextField txtTableadd;
	private JTextField txtPhysicadd;
	private JTextField txtProid;
	private JTextField txtPriority;
	private JTextField txtAccesstext;
	private JTextField txtWaitreason;
	private JTextField txtInstructionnum;
	private JTextField txtDatanum;
	private JTextField txtTextadd;
	private JTextField txtDataadd;
	private JTextField txtStackadd;
	private JTextField txtBufferadd;
	private JTextField txtProTableadd;
	private JTextField txtCputime;
	private JTextField txtSumtime;
	private JTextField txtUserbufferadd;
	private JTextField txtSystemBufferadd;
	private JTextField txtDataregister;
	private JTextField txtDeviceadd;
	private JTextField txtDmamessage;
	private JTextField txtClockmessage;
	private JTextField txtSysruntime;
	private JTextField txtCpumessage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OSframe frame = new OSframe();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public OSframe() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(3, 3, WIDTH * UNIT_WIDTH, HEIGHT * UNIT_HEIGHT + TITLE_HEIGHT);
		this.setTitle("贺宁的操作系统课程设计");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		// 初始化两个系统
		this.PMS = new ProcessManageSystem();
		this.JMS = new JobManageSystem(this.PMS.getClock(), this.PMS.getMMS().getSubMemory());

		JPanel dowPanel = new JPanel();
		contentPane.add(dowPanel, BorderLayout.SOUTH);
		dowPanel.setPreferredSize(
				new Dimension(WIDTH * UNIT_WIDTH, (HIGH_MID_MANAGE_DOMAIN + LOW_MANAGE_DOMAIN) * UNIT_HEIGHT));
		dowPanel.setLayout(new BorderLayout(0, 0));

		JPanel LowManagePanel = new JPanel();
		dowPanel.add(LowManagePanel);
		LowManagePanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel readyQueuePanel = new JPanel();
		readyQueuePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"\u5C31\u7EEA\u961F\u5217", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		LowManagePanel.add(readyQueuePanel);
		readyQueuePanel.setLayout(new BorderLayout(0, 0));

		JPanel readyQleftPanel = new JPanel();
		readyQueuePanel.add(readyQleftPanel, BorderLayout.WEST);
		readyQleftPanel.setLayout(new GridLayout(1, 6, 0, 0));
		readyQleftPanel.setPreferredSize(new Dimension(UNIT_WIDTH * 6, UNIT_HEIGHT * LOW_MANAGE_DOMAIN));

		JPanel readyQ0Panel = new JPanel();
		readyQ0Panel
				.setBorder(new TitledBorder(null, "\u8FDB\u7A0BID", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		readyQleftPanel.add(readyQ0Panel);
		readyQ0Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> readyList0 = new JList<Integer>();
		readyQ0Panel.add(readyList0, BorderLayout.CENTER);

		JPanel readyQ1Panel = new JPanel();
		readyQ1Panel.setBorder(
				new TitledBorder(null, "\u4F18\u5148\u7EA7", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		readyQleftPanel.add(readyQ1Panel);
		readyQ1Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> readyList1 = new JList<Integer>();
		readyQ1Panel.add(readyList1, BorderLayout.CENTER);

		JPanel readyQ2Panel = new JPanel();
		readyQleftPanel.add(readyQ2Panel);
		readyQ2Panel.setBorder(
				new TitledBorder(null, "\u8BBF\u95EE\u5B57\u6BB5", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		readyQ2Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> readyList2 = new JList<Integer>();
		readyQ2Panel.add(readyList2, BorderLayout.CENTER);

		JPanel readyQ3Panel = new JPanel();
		readyQleftPanel.add(readyQ3Panel);
		readyQ3Panel.setBorder(
				new TitledBorder(null, "\u5DF2\u5360\u65F6\u957F", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		readyQ3Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> readyList3 = new JList<Integer>();
		readyQ3Panel.add(readyList3, BorderLayout.CENTER);

		JPanel readyQ4Panel = new JPanel();
		readyQleftPanel.add(readyQ4Panel);
		readyQ4Panel.setBorder(
				new TitledBorder(null, "\u6307\u4EE4\u6570", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		readyQ4Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> readyList4 = new JList<Integer>();
		readyList4.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		readyQ4Panel.add(readyList4, BorderLayout.CENTER);

		JPanel readyQ5Panel = new JPanel();
		readyQleftPanel.add(readyQ5Panel);
		readyQ5Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6570\u636E\u91CF",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		readyQ5Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> readyList5 = new JList<Integer>();
		readyQ5Panel.add(readyList5, BorderLayout.CENTER);

		JPanel readyQrightPanel = new JPanel();
		readyQueuePanel.add(readyQrightPanel);
		readyQrightPanel.setLayout(new GridLayout(1, 4, 0, 0));

		JPanel readyQ6Panel = new JPanel();
		readyQ6Panel.setBorder(new TitledBorder(null, "\u6B63\u6587\u6BB5\u5730\u5740", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		readyQrightPanel.add(readyQ6Panel);
		readyQ6Panel.setLayout(new BorderLayout(0, 0));

		JList<String> readyList6 = new JList<String>();// 从此处开始，就绪队列的列表是字符串型
		readyQ6Panel.add(readyList6, BorderLayout.CENTER);

		JPanel readyQ7Panel = new JPanel();
		readyQ7Panel.setBorder(new TitledBorder(null, "\u6570\u636E\u6BB5\u5730\u5740", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		readyQrightPanel.add(readyQ7Panel);
		readyQ7Panel.setLayout(new BorderLayout(0, 0));

		JList<String> readyList7 = new JList<String>();
		readyQ7Panel.add(readyList7, BorderLayout.CENTER);

		JPanel readyQ8Panel = new JPanel();
		readyQ8Panel.setBorder(new TitledBorder(null, "\u6838\u5FC3\u6808\u5730\u5740", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		readyQrightPanel.add(readyQ8Panel);
		readyQ8Panel.setLayout(new BorderLayout(0, 0));

		JList<String> readyList8 = new JList<String>();
		readyQ8Panel.add(readyList8, BorderLayout.CENTER);

		JPanel readyQ9Panel = new JPanel();
		readyQ9Panel.setBorder(new TitledBorder(null, "\u6700\u8FD1\u963B\u585E\u539F\u56E0", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		readyQrightPanel.add(readyQ9Panel);
		readyQ9Panel.setLayout(new BorderLayout(0, 0));

		JList<String> readyList9 = new JList<String>();
		readyQ9Panel.add(readyList9, BorderLayout.CENTER);

		JPanel blockQueuePanel = new JPanel();
		blockQueuePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"\u963B\u585E\u961F\u5217", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		LowManagePanel.add(blockQueuePanel);
		blockQueuePanel.setPreferredSize(new Dimension(UNIT_WIDTH * 11, UNIT_HEIGHT * LOW_MANAGE_DOMAIN));
		blockQueuePanel.setLayout(new BorderLayout(0, 0));

		JPanel blockQleftPanel = new JPanel();
		blockQueuePanel.add(blockQleftPanel, BorderLayout.WEST);
		blockQleftPanel.setLayout(new GridLayout(1, 2, 0, 0));
		blockQleftPanel.setPreferredSize(new Dimension(UNIT_WIDTH * 6, UNIT_HEIGHT * LOW_MANAGE_DOMAIN));

		JPanel blockQ0Panel = new JPanel();
		blockQ0Panel
				.setBorder(new TitledBorder(null, "\u8FDB\u7A0BID", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		blockQleftPanel.add(blockQ0Panel);
		blockQ0Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> blockList0 = new JList<Integer>();
		blockQ0Panel.add(blockList0, BorderLayout.CENTER);

		JPanel blockQ1Panel = new JPanel();
		blockQ1Panel.setBorder(
				new TitledBorder(null, "\u4F18\u5148\u7EA7", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		blockQleftPanel.add(blockQ1Panel);
		blockQ1Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> blockList1 = new JList<Integer>();
		blockQ1Panel.add(blockList1, BorderLayout.CENTER);

		JPanel blockQ2panel = new JPanel();
		blockQleftPanel.add(blockQ2panel);
		blockQ2panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u8BBF\u95EE\u5B57\u6BB5",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		blockQ2panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> blockList2 = new JList<Integer>();
		blockQ2panel.add(blockList2, BorderLayout.CENTER);

		JPanel blockQ3Panel = new JPanel();
		blockQleftPanel.add(blockQ3Panel);
		blockQ3Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u5DF2\u5360\u65F6\u957F",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		blockQ3Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> blockList3 = new JList<Integer>();
		blockQ3Panel.add(blockList3, BorderLayout.CENTER);

		JPanel blockQ4Panel = new JPanel();
		blockQleftPanel.add(blockQ4Panel);
		blockQ4Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6307\u4EE4\u6570",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		blockQ4Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> blockList4 = new JList<Integer>();
		blockQ4Panel.add(blockList4, BorderLayout.CENTER);

		JPanel blockQ5Panel = new JPanel();
		blockQleftPanel.add(blockQ5Panel);
		blockQ5Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6570\u636E\u91CF",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		blockQ5Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> blockList5 = new JList<Integer>();
		blockQ5Panel.add(blockList5, BorderLayout.CENTER);

		JPanel blockQrightPanel = new JPanel();
		blockQueuePanel.add(blockQrightPanel);
		blockQrightPanel.setLayout(new GridLayout(1, 4, 0, 0));

		JPanel blockQ6Panel = new JPanel();
		blockQ6Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"\u6B63\u6587\u6BB5\u5730\u5740", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		blockQrightPanel.add(blockQ6Panel);
		blockQ6Panel.setLayout(new BorderLayout(0, 0));

		JList<String> blockList6 = new JList<String>();// 从此处开始，就绪队列的列表是字符串型
		blockQ6Panel.add(blockList6);

		JPanel blockQ7Panel = new JPanel();
		blockQ7Panel.setBorder(new TitledBorder(null, "\u6570\u636E\u6BB5\u5730\u5740", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		blockQrightPanel.add(blockQ7Panel);
		blockQ7Panel.setLayout(new BorderLayout(0, 0));

		JList<String> blockList7 = new JList<String>();
		blockQ7Panel.add(blockList7, BorderLayout.CENTER);

		JPanel blockQ8Panel = new JPanel();
		blockQ8Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"\u6838\u5FC3\u6808\u5730\u5740", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		blockQrightPanel.add(blockQ8Panel);
		blockQ8Panel.setLayout(new BorderLayout(0, 0));

		JList<String> blockList8 = new JList<String>();
		blockQ8Panel.add(blockList8, BorderLayout.CENTER);

		JPanel blockQ9Panel = new JPanel();
		blockQ9Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u963B\u585E\u539F\u56E0",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		blockQrightPanel.add(blockQ9Panel);
		blockQ9Panel.setLayout(new BorderLayout(0, 0));

		JList<String> blockList9 = new JList<String>();
		blockQ9Panel.add(blockList9, BorderLayout.CENTER);

		JPanel HighMidManagePanel = new JPanel();
		dowPanel.add(HighMidManagePanel, BorderLayout.SOUTH);
		HighMidManagePanel.setLayout(new BorderLayout(0, 0));
		HighMidManagePanel.setPreferredSize(new Dimension(UNIT_WIDTH * 6, UNIT_HEIGHT * HIGH_MID_MANAGE_DOMAIN));

		JPanel MidManagePanel = new JPanel();
		HighMidManagePanel.add(MidManagePanel, BorderLayout.CENTER);
		MidManagePanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel pendReadyQueuePanel = new JPanel();
		pendReadyQueuePanel.setBorder(new TitledBorder(null, "\u6302\u8D77\u5C31\u7EEA\u961F\u5217",
				TitledBorder.CENTER, TitledBorder.TOP, null, null));
		MidManagePanel.add(pendReadyQueuePanel);
		pendReadyQueuePanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel pendRQleftPanel = new JPanel();
		pendReadyQueuePanel.add(pendRQleftPanel);
		pendRQleftPanel.setLayout(new GridLayout(1, 4, 0, 0));

		JPanel pendRQ0panel = new JPanel();
		pendRQ0panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u8FDB\u7A0BID",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pendRQleftPanel.add(pendRQ0panel);
		pendRQ0panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendRQlist0 = new JList<Integer>();
		pendRQ0panel.add(pendRQlist0, BorderLayout.CENTER);

		JPanel pendRQ1panel = new JPanel();
		pendRQ1panel.setBorder(
				new TitledBorder(null, "\u4F18\u5148\u7EA7", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		pendRQleftPanel.add(pendRQ1panel);
		pendRQ1panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendRQlist1 = new JList<Integer>();
		pendRQ1panel.add(pendRQlist1, BorderLayout.CENTER);

		JPanel pendRQ2panel = new JPanel();
		pendRQ2panel.setBorder(
				new TitledBorder(null, "\u6307\u4EE4\u6570", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		pendRQleftPanel.add(pendRQ2panel);
		pendRQ2panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendRQlist2 = new JList<Integer>();
		pendRQ2panel.add(pendRQlist2, BorderLayout.CENTER);

		JPanel pendRQ3panel = new JPanel();
		pendRQ3panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6570\u636E\u91CF",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pendRQleftPanel.add(pendRQ3panel);
		pendRQ3panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendRQlist3 = new JList<Integer>();
		pendRQ3panel.add(pendRQlist3, BorderLayout.CENTER);

		JPanel pendRQrightPanel = new JPanel();
		pendReadyQueuePanel.add(pendRQrightPanel);
		pendRQrightPanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel pendRQ4panel = new JPanel();
		pendRQ4panel.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6700\u8FD1\u963B\u585E\u539F\u56E0",
						TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pendRQrightPanel.add(pendRQ4panel);
		pendRQ4panel.setLayout(new BorderLayout(0, 0));

		JList<String> pendRQlist4 = new JList<String>();
		pendRQ4panel.add(pendRQlist4, BorderLayout.CENTER);

		JPanel pendRQ5panel = new JPanel();
		pendRQ5panel.setBorder(new TitledBorder(null, "\u4EA4\u6362\u533A\u5730\u5740", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		pendRQrightPanel.add(pendRQ5panel);
		pendRQ5panel.setLayout(new BorderLayout(0, 0));

		JList<String> pendRQlist5 = new JList<String>();
		pendRQ5panel.add(pendRQlist5, BorderLayout.CENTER);

		JPanel pendBlockQueuePanel = new JPanel();
		pendBlockQueuePanel.setBorder(new TitledBorder(null, "\u6302\u8D77\u963B\u585E\u961F\u5217",
				TitledBorder.CENTER, TitledBorder.TOP, null, null));
		MidManagePanel.add(pendBlockQueuePanel);
		pendBlockQueuePanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel pendBQleftPanel = new JPanel();
		pendBlockQueuePanel.add(pendBQleftPanel);
		pendBQleftPanel.setLayout(new GridLayout(1, 4, 0, 0));

		JPanel pendBQ0panel = new JPanel();
		pendBQ0panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u8FDB\u7A0BID",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pendBQleftPanel.add(pendBQ0panel);
		pendBQ0panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendBQList0 = new JList<Integer>();
		pendBQ0panel.add(pendBQList0, BorderLayout.CENTER);

		JPanel pendBQ1panel = new JPanel();
		pendBQ1panel.setBorder(
				new TitledBorder(null, "\u4F18\u5148\u7EA7", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		pendBQleftPanel.add(pendBQ1panel);
		pendBQ1panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendBQList1 = new JList<Integer>();
		pendBQ1panel.add(pendBQList1, BorderLayout.CENTER);

		JPanel pendBQ2panel = new JPanel();
		pendBQ2panel.setBorder(
				new TitledBorder(null, "\u6307\u4EE4\u6570", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		pendBQleftPanel.add(pendBQ2panel);
		pendBQ2panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendBQList2 = new JList<Integer>();
		pendBQ2panel.add(pendBQList2, BorderLayout.CENTER);

		JPanel pendBQ3panel = new JPanel();
		pendBQ3panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6570\u636E\u91CF",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pendBQleftPanel.add(pendBQ3panel);
		pendBQ3panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> pendBQList3 = new JList<Integer>();
		pendBQ3panel.add(pendBQList3, BorderLayout.CENTER);

		JPanel pendBQrightPanel = new JPanel();
		pendBlockQueuePanel.add(pendBQrightPanel);
		pendBQrightPanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel pendBQ4panel = new JPanel();
		pendBQ4panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u963B\u585E\u539F\u56E0",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pendBQrightPanel.add(pendBQ4panel);
		pendBQ4panel.setLayout(new BorderLayout(0, 0));

		JList<String> pendBQList4 = new JList<String>();
		pendBQ4panel.add(pendBQList4, BorderLayout.CENTER);

		JPanel pendBQ5panel = new JPanel();
		pendBQ5panel.setBorder(new TitledBorder(null, "\u4EA4\u6362\u533A\u5730\u5740", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		pendBQrightPanel.add(pendBQ5panel);
		pendBQ5panel.setLayout(new BorderLayout(0, 0));

		JList<String> pendBQList5 = new JList<String>();
		pendBQ5panel.add(pendBQList5, BorderLayout.CENTER);

		JPanel HighManagePanel = new JPanel();
		HighMidManagePanel.add(HighManagePanel, BorderLayout.WEST);
		HighManagePanel.setPreferredSize(new Dimension(UNIT_WIDTH * 12, UNIT_HEIGHT * HIGH_MID_MANAGE_DOMAIN));
		HighManagePanel.setLayout(new BorderLayout(0, 0));

		JPanel futureQueuePanel = new JPanel();
		HighManagePanel.add(futureQueuePanel, BorderLayout.CENTER);
		futureQueuePanel.setBorder(new TitledBorder(null, "\u672A\u6765\u8BF7\u6C42\u5E8F\u5217", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		futureQueuePanel.setLayout(new GridLayout(1, 5, 0, 0));

		JPanel futureQ0Panel = new JPanel();
		futureQ0Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u4F18\u5148\u7EA7",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		futureQueuePanel.add(futureQ0Panel);
		futureQ0Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> futureList0 = new JList<Integer>();
		futureQ0Panel.add(futureList0, BorderLayout.CENTER);

		JPanel futureQ1Panel = new JPanel();
		futureQ1Panel.setBorder(
				new TitledBorder(null, "\u6307\u4EE4\u6570", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		futureQueuePanel.add(futureQ1Panel);
		futureQ1Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> futureList1 = new JList<Integer>();
		futureQ1Panel.add(futureList1, BorderLayout.CENTER);

		JPanel futureQ2Panel = new JPanel();
		futureQ2Panel.setBorder(
				new TitledBorder(null, "\u6570\u636E\u91CF", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		futureQueuePanel.add(futureQ2Panel);
		futureQ2Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> futureList2 = new JList<Integer>();
		futureQ2Panel.add(futureList2);

		JPanel futureQ3Panel = new JPanel();
		futureQ3Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "inTime",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		futureQueuePanel.add(futureQ3Panel);
		futureQ3Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> futureList3 = new JList<Integer>();
		futureQ3Panel.add(futureList3);

		JPanel jobQueuePanel = new JPanel();
		HighManagePanel.add(jobQueuePanel, BorderLayout.EAST);
		jobQueuePanel.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u4F5C\u4E1A\u540E\u5907\u961F\u5217",
						TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		jobQueuePanel.setPreferredSize(new Dimension(UNIT_WIDTH * 8, UNIT_HEIGHT * HIGH_MID_MANAGE_DOMAIN));
		jobQueuePanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel jobQleftPanel = new JPanel();
		jobQueuePanel.add(jobQleftPanel);
		jobQleftPanel.setLayout(new GridLayout(1, 4, 0, 0));

		JPanel jobQ0Panel = new JPanel();
		jobQleftPanel.add(jobQ0Panel);
		jobQ0Panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u4F5C\u4E1AID",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		jobQ0Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> jobList0 = new JList<Integer>();
		jobQ0Panel.add(jobList0, BorderLayout.CENTER);

		JPanel jobQ1Panel = new JPanel();
		jobQleftPanel.add(jobQ1Panel);
		jobQ1Panel.setBorder(
				new TitledBorder(null, "\u4F18\u5148\u7EA7", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		jobQ1Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> jobList1 = new JList<Integer>();
		jobQ1Panel.add(jobList1, BorderLayout.CENTER);

		JPanel jobQ2Panel = new JPanel();
		jobQleftPanel.add(jobQ2Panel);
		jobQ2Panel.setBorder(
				new TitledBorder(null, "\u6307\u4EE4\u6570", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		jobQ2Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> jobList2 = new JList<Integer>();
		jobQ2Panel.add(jobList2, BorderLayout.CENTER);

		JPanel jobQ3Panel = new JPanel();
		jobQleftPanel.add(jobQ3Panel);
		jobQ3Panel.setBorder(
				new TitledBorder(null, "\u6570\u636E\u91CF", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		jobQ3Panel.setLayout(new BorderLayout(0, 0));

		JList<Integer> jobList3 = new JList<Integer>();
		jobQ3Panel.add(jobList3, BorderLayout.CENTER);

		JPanel jobQrightPanel = new JPanel();
		jobQueuePanel.add(jobQrightPanel);
		jobQrightPanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel jobQ4Panel = new JPanel();
		jobQrightPanel.add(jobQ4Panel);
		jobQ4Panel.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u7A0B\u5E8F\u5916\u5B58\u5730\u5740",
						TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		jobQ4Panel.setLayout(new BorderLayout(0, 0));

		JList<String> jobList4 = new JList<String>();
		jobQ4Panel.add(jobList4, BorderLayout.CENTER);

		JPanel jobQ5Panel = new JPanel();
		jobQrightPanel.add(jobQ5Panel);
		jobQ5Panel.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u6570\u636E\u5916\u5B58\u5730\u5740",
						TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		jobQ5Panel.setLayout(new BorderLayout(0, 0));

		JList<String> jobList5 = new JList<String>();
		jobQ5Panel.add(jobList5, BorderLayout.CENTER);

		JPanel upPanel = new JPanel();
		contentPane.add(upPanel, BorderLayout.CENTER);
		upPanel.setLayout(new BorderLayout(0, 0));

		JPanel runPanel = new JPanel();
		upPanel.add(runPanel, BorderLayout.WEST);
		runPanel.setLayout(new GridLayout(1, 2, 0, 0));
		runPanel.setPreferredSize(new Dimension(UNIT_WIDTH * 8, UNIT_HEIGHT * 25));

		JPanel hardwarePanel = new JPanel();
		runPanel.add(hardwarePanel);
		hardwarePanel.setLayout(new GridLayout(4, 1, 0, 0));

		JPanel clockPanel = new JPanel();
		hardwarePanel.add(clockPanel);
		clockPanel.setBorder(new TitledBorder(null, "\u65F6\u949F", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		clockPanel.setLayout(new GridLayout(0, 2, 0, 0));

		JLabel labelClockMess = new JLabel("\u65F6\u949F\u4E2D\u65AD\u4FE1\u53F7");
		labelClockMess.setHorizontalAlignment(SwingConstants.CENTER);
		labelClockMess.setFont(new Font("宋体", Font.PLAIN, 15));
		clockPanel.add(labelClockMess);

		txtClockmessage = new JTextField();
		txtClockmessage.setBackground(Color.ORANGE);
		txtClockmessage.setHorizontalAlignment(SwingConstants.CENTER);
		txtClockmessage.setText("ClockMessage");
		clockPanel.add(txtClockmessage);
		txtClockmessage.setColumns(10);

		JLabel labelSysRunTime = new JLabel("\u7CFB\u7EDF\u8FD0\u884C\u65F6\u957F");
		labelSysRunTime.setHorizontalAlignment(SwingConstants.CENTER);
		labelSysRunTime.setFont(new Font("SimSun", Font.PLAIN, 15));
		clockPanel.add(labelSysRunTime);

		txtSysruntime = new JTextField();
		txtSysruntime.setHorizontalAlignment(SwingConstants.CENTER);
		txtSysruntime.setFont(new Font("宋体", Font.PLAIN, 36));
		txtSysruntime.setText(Integer.toString(this.PMS.getClock().getCountSecondNum()));
		clockPanel.add(txtSysruntime);
		txtSysruntime.setColumns(10);

		JPanel hardwareUpPanel = new JPanel();
		hardwareUpPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		hardwarePanel.add(hardwareUpPanel);
		hardwareUpPanel.setLayout(new BorderLayout(0, 0));

		JPanel cpuPanel = new JPanel();
		cpuPanel.setBorder(new TitledBorder(null, "CPU ", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		hardwareUpPanel.add(cpuPanel, BorderLayout.CENTER);
		cpuPanel.setLayout(new GridLayout(5, 2, 0, 0));

		JLabel lblPc = new JLabel("PC");
		lblPc.setHorizontalAlignment(SwingConstants.CENTER);
		cpuPanel.add(lblPc);

		txtPc = new JTextField();
		txtPc.setText("PC");
		cpuPanel.add(txtPc);
		txtPc.setColumns(10);

		JLabel lblIr = new JLabel("IR");
		lblIr.setHorizontalAlignment(SwingConstants.CENTER);
		cpuPanel.add(lblIr);

		txtIr = new JTextField();
		txtIr.setText("IR");
		cpuPanel.add(txtIr);
		txtIr.setColumns(10);

		JLabel lblPsw = new JLabel("PSW");
		lblPsw.setHorizontalAlignment(SwingConstants.CENTER);
		cpuPanel.add(lblPsw);

		txtPsw = new JTextField();
		txtPsw.setText("PSW");
		cpuPanel.add(txtPsw);
		txtPsw.setColumns(10);

		JLabel lblAr = new JLabel("AR");
		lblAr.setHorizontalAlignment(SwingConstants.CENTER);
		cpuPanel.add(lblAr);

		txtAr = new JTextField();
		txtAr.setText("AR");
		cpuPanel.add(txtAr);
		txtAr.setColumns(10);

		JLabel lblSp = new JLabel("SP");
		lblSp.setHorizontalAlignment(SwingConstants.CENTER);
		cpuPanel.add(lblSp);

		txtSp = new JTextField();
		txtSp.setText("SP");
		cpuPanel.add(txtSp);
		txtSp.setColumns(10);

		JPanel hardwareMidPanel = new JPanel();
		hardwareMidPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		hardwarePanel.add(hardwareMidPanel);
		hardwareMidPanel.setLayout(new BorderLayout(0, 0));

		JPanel mmuPanel = new JPanel();
		mmuPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "MMU", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		hardwareMidPanel.add(mmuPanel, BorderLayout.CENTER);
		mmuPanel.setLayout(new GridLayout(5, 2, 0, 0));

		JLabel labelLogAdd = new JLabel("\u903B\u8F91\u5730\u5740");
		labelLogAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelLogAdd.setHorizontalAlignment(SwingConstants.CENTER);
		mmuPanel.add(labelLogAdd);

		txtLogicadd = new JTextField();
		txtLogicadd.setText("LogicAdd");
		mmuPanel.add(txtLogicadd);
		txtLogicadd.setColumns(10);

		JLabel labelTableadd = new JLabel("\u8FDB\u7A0B\u9875\u8868\u57FA\u5740");
		labelTableadd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelTableadd.setHorizontalAlignment(SwingConstants.CENTER);
		mmuPanel.add(labelTableadd);

		txtTableadd = new JTextField();
		txtTableadd.setText("TableAdd");
		mmuPanel.add(txtTableadd);
		txtTableadd.setColumns(10);

		JLabel labelPhysAdd = new JLabel("\u7269\u7406\u5730\u5740");
		labelPhysAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelPhysAdd.setHorizontalAlignment(SwingConstants.CENTER);
		mmuPanel.add(labelPhysAdd);

		txtPhysicadd = new JTextField();
		txtPhysicadd.setText("PhysicAdd");
		mmuPanel.add(txtPhysicadd);
		txtPhysicadd.setColumns(10);

		JButton buttonTlb = new JButton("\u5FEB\u8868");
		buttonTlb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(buttonTlb)) {
					tlbFrame.setVisible(true);
				}
			}
		});
		buttonTlb.setFont(new Font("宋体", Font.PLAIN, 16));
		mmuPanel.add(buttonTlb);

		JButton buttonPageTable = new JButton("\u9875\u8868");
		buttonPageTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(buttonPageTable)) {
					pgTbFrame.setVisible(true);
				}
			}
		});
		buttonPageTable.setFont(new Font("宋体", Font.PLAIN, 16));
		mmuPanel.add(buttonPageTable);

		JButton buttonMainMem = new JButton("\u4E3B\u5B58");
		buttonMainMem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(buttonMainMem)) {
					buddyFrame.setVisible(true);
				}
			}
		});
		buttonMainMem.setFont(new Font("宋体", Font.PLAIN, 16));
		mmuPanel.add(buttonMainMem);

		JButton buttonSubMem = new JButton("\u8F85\u5B58");
		buttonSubMem.setFont(new Font("宋体", Font.PLAIN, 16));
		mmuPanel.add(buttonSubMem);

		JPanel hardwareDownPanel = new JPanel();
		hardwarePanel.add(hardwareDownPanel);
		hardwareDownPanel.setLayout(new GridLayout(1, 2, 0, 0));

		JPanel dmaPanel = new JPanel();
		dmaPanel.setBorder(new TitledBorder(null, "DMA", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		hardwareDownPanel.add(dmaPanel);
		dmaPanel.setLayout(new GridLayout(6, 2, 0, 0));

		JLabel lblCpuMess = new JLabel("CPU\u89E6\u53D1\u4FE1\u53F7");
		lblCpuMess.setHorizontalAlignment(SwingConstants.CENTER);
		dmaPanel.add(lblCpuMess);

		JLabel lblDmaMess = new JLabel("DMA\u4E2D\u65AD\u4FE1\u53F7");
		lblDmaMess.setHorizontalAlignment(SwingConstants.CENTER);
		lblDmaMess.setFont(new Font("宋体", Font.PLAIN, 15));
		dmaPanel.add(lblDmaMess);

		txtCpumessage = new JTextField();
		txtCpumessage.setBackground(Color.ORANGE);
		txtCpumessage.setText("CpuMessage");
		txtCpumessage.setHorizontalAlignment(SwingConstants.CENTER);
		dmaPanel.add(txtCpumessage);
		txtCpumessage.setColumns(10);

		txtDmamessage = new JTextField();
		txtDmamessage.setBackground(Color.ORANGE);
		txtDmamessage.setForeground(Color.BLACK);
		txtDmamessage.setHorizontalAlignment(SwingConstants.CENTER);
		txtDmamessage.setText("DmaMessage");
		dmaPanel.add(txtDmamessage);
		txtDmamessage.setColumns(10);

		JLabel labelUserBufferAdd = new JLabel("\u8FDB\u7A0B\u7F13\u51B2\u533A\u5730\u5740");
		labelUserBufferAdd.setHorizontalAlignment(SwingConstants.CENTER);
		labelUserBufferAdd.setFont(new Font("宋体", Font.PLAIN, 14));
		dmaPanel.add(labelUserBufferAdd);

		txtUserbufferadd = new JTextField();
		txtUserbufferadd.setText("userBufferAdd");
		dmaPanel.add(txtUserbufferadd);
		txtUserbufferadd.setColumns(10);

		JLabel label = new JLabel("\u7F13\u51B2\u533A\u5730\u5740");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("宋体", Font.PLAIN, 15));
		dmaPanel.add(label);

		txtSystemBufferadd = new JTextField();
		txtSystemBufferadd.setText("BufferAdd");
		dmaPanel.add(txtSystemBufferadd);
		txtSystemBufferadd.setColumns(10);

		JLabel labelDataRegister = new JLabel("\u6570\u636E\u5BC4\u5B58\u5668");
		labelDataRegister.setHorizontalAlignment(SwingConstants.CENTER);
		labelDataRegister.setFont(new Font("宋体", Font.PLAIN, 15));
		dmaPanel.add(labelDataRegister);

		txtDataregister = new JTextField();
		txtDataregister.setText("DataRegister");
		dmaPanel.add(txtDataregister);
		txtDataregister.setColumns(10);

		JLabel labelDeviceAdd = new JLabel("\u5916\u8BBE\u5730\u5740");
		labelDeviceAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelDeviceAdd.setHorizontalAlignment(SwingConstants.CENTER);
		dmaPanel.add(labelDeviceAdd);

		txtDeviceadd = new JTextField();
		txtDeviceadd.setText("deviceAdd");
		dmaPanel.add(txtDeviceadd);
		txtDeviceadd.setColumns(10);

		JPanel runProPanel = new JPanel();
		runProPanel.setBorder(new TitledBorder(null, "\u5F53\u524D\u8FD0\u884C\u8FDB\u7A0B", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		runPanel.add(runProPanel);
		runProPanel.setLayout(new GridLayout(15, 2, 0, 0));

		JLabel lblProid = new JLabel("\u8FDB\u7A0BID");
		lblProid.setFont(new Font("宋体", Font.PLAIN, 15));
		lblProid.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(lblProid);

		txtProid = new JTextField();
		txtProid.setText("proID");
		runProPanel.add(txtProid);
		txtProid.setColumns(10);

		JLabel labelPriority = new JLabel("\u4F18\u5148\u7EA7");
		labelPriority.setFont(new Font("宋体", Font.PLAIN, 15));
		labelPriority.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelPriority);

		txtPriority = new JTextField();
		txtPriority.setText("Priority");
		runProPanel.add(txtPriority);
		txtPriority.setColumns(10);

		JLabel lblAccessText = new JLabel("\u8FDB\u7A0B\u8BBF\u95EE\u5B57\u6BB5");
		lblAccessText.setFont(new Font("宋体", Font.PLAIN, 15));
		lblAccessText.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(lblAccessText);

		txtAccesstext = new JTextField();
		txtAccesstext.setText("AccessText");
		runProPanel.add(txtAccesstext);
		txtAccesstext.setColumns(10);

		JLabel labelWaitReason = new JLabel("\u6700\u8FD1\u963B\u585E\u539F\u56E0");
		labelWaitReason.setFont(new Font("宋体", Font.PLAIN, 15));
		labelWaitReason.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelWaitReason);

		txtWaitreason = new JTextField();
		txtWaitreason.setText("WaitReason");
		runProPanel.add(txtWaitreason);
		txtWaitreason.setColumns(10);

		JLabel lblInstructionNum = new JLabel("\u62E5\u6709\u6307\u4EE4\u6570");
		lblInstructionNum.setFont(new Font("宋体", Font.PLAIN, 15));
		lblInstructionNum.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(lblInstructionNum);

		txtInstructionnum = new JTextField();
		txtInstructionnum.setText("InstructionNum");
		runProPanel.add(txtInstructionnum);
		txtInstructionnum.setColumns(10);

		JLabel labelDataNum = new JLabel("\u62E5\u6709\u6570\u636E\u91CF");
		labelDataNum.setFont(new Font("宋体", Font.PLAIN, 15));
		labelDataNum.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelDataNum);

		txtDatanum = new JTextField();
		txtDatanum.setText("DataNum");
		runProPanel.add(txtDatanum);
		txtDatanum.setColumns(10);

		JLabel labelTextAdd = new JLabel("\u7A0B\u5E8F\u6BB5\u9996\u5730\u5740");
		labelTextAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelTextAdd.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelTextAdd);

		txtTextadd = new JTextField();
		txtTextadd.setText("TextAdd");
		runProPanel.add(txtTextadd);
		txtTextadd.setColumns(10);

		JLabel labelDataAdd = new JLabel("\u6570\u636E\u6BB5\u9996\u5730\u5740");
		labelDataAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelDataAdd.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelDataAdd);

		txtDataadd = new JTextField();
		txtDataadd.setText("DataAdd");
		runProPanel.add(txtDataadd);
		txtDataadd.setColumns(10);

		JLabel labelStackAdd = new JLabel("\u6838\u5FC3\u6808\u9996\u5730\u5740");
		labelStackAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelStackAdd.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelStackAdd);

		txtStackadd = new JTextField();
		txtStackadd.setText("StackAdd");
		runProPanel.add(txtStackadd);
		txtStackadd.setColumns(10);

		JLabel labelBufferAdd = new JLabel("\u8FDB\u7A0B\u7F13\u51B2\u533A\u9996\u5730\u5740");
		labelBufferAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelBufferAdd.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelBufferAdd);

		txtBufferadd = new JTextField();
		txtBufferadd.setText("BufferAdd");
		runProPanel.add(txtBufferadd);
		txtBufferadd.setColumns(10);

		JLabel labelTableAdd = new JLabel("\u8FDB\u7A0B\u9875\u8868\u9996\u5730\u5740");
		labelTableAdd.setFont(new Font("宋体", Font.PLAIN, 15));
		labelTableAdd.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelTableAdd);

		txtProTableadd = new JTextField();
		txtProTableadd.setText("ProTableAdd");
		runProPanel.add(txtProTableadd);
		txtProTableadd.setColumns(10);

		JLabel lblcpuTime = new JLabel("\u672C\u6B21\u5360\u7528CPU\u65F6\u957F");
		lblcpuTime.setFont(new Font("宋体", Font.PLAIN, 15));
		lblcpuTime.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(lblcpuTime);

		txtCputime = new JTextField();
		txtCputime.setText("CPUTime");
		runProPanel.add(txtCputime);
		txtCputime.setColumns(10);

		JLabel labelSumTime = new JLabel("\u8FD0\u884C\u603B\u65F6\u957F");
		labelSumTime.setFont(new Font("宋体", Font.PLAIN, 15));
		labelSumTime.setHorizontalAlignment(SwingConstants.CENTER);
		runProPanel.add(labelSumTime);

		txtSumtime = new JTextField();
		txtSumtime.setText("SumTime");
		runProPanel.add(txtSumtime);
		txtSumtime.setColumns(10);

		JButton buttonCreateRequest = new JButton("\u521B\u5EFA\u8BF7\u6C42");
		buttonCreateRequest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (JMS) {
					JMS.addOneFutureRequest(PMS.getClock().getSecondNum());
				}
			}
		});
		buttonCreateRequest.setPreferredSize(new Dimension(68, 175));
		buttonCreateRequest.setFont(new Font("宋体", Font.PLAIN, 15));
		runProPanel.add(buttonCreateRequest);

		JButton btnStart = new JButton("\u7CFB\u7EDF\u542F\u52A8");
		btnStart.setFont(new Font("宋体", Font.PLAIN, 15));
		btnStart.addActionListener(new ActionListener() {
			/**
			 * 五线程启动：<br>
			 * 1.时钟线程启动<br>
			 * 2.请求线程启动<br>
			 */
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(btnStart)) {
					threadTime = new ThreadTime(PMS.getClock());
					threadRequests = new ThreadRequests(JMS, PMS);
					threadJobs = new ThreadJobs(PMS, JMS);
					threadProcess = new ThreadProcess(PMS);
					threadDma = new ThreadDma(PMS.getDMA());

					Thread thread0 = new Thread(threadTime);
					Thread thread1 = new Thread(threadRequests);
					Thread thread2 = new Thread(threadJobs);
					Thread thread3 = new Thread(threadProcess);
					Thread thread5 = new Thread(threadDma);

					thread0.start();
					thread1.start();
					thread2.start();
					thread3.start();
					thread5.start();
				}

			}
		});
		runProPanel.add(btnStart);

		JPanel LogPanel = new JPanel();
		LogPanel.setBorder(new TitledBorder(null, "\u7CFB\u7EDF\u8FD0\u884C\u8FC7\u7A0B", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		upPanel.add(LogPanel, BorderLayout.CENTER);
		LogPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		LogPanel.add(scrollPane, BorderLayout.CENTER);

		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("宋体", Font.PLAIN, 20));
		scrollPane.setViewportView(textArea);

		/******************************* 创建刷新界面线程。 *******************************/
		threadRefresh = new ThreadRefresh(PMS, JMS, txtSysruntime, txtClockmessage, txtCpumessage, txtDmamessage,
				txtUserbufferadd, txtSystemBufferadd, txtDataregister, txtDeviceadd, futureList0, futureList1,
				futureList2, futureList3, jobList0, jobList1, jobList2, jobList3, jobList4, jobList5, readyList0,
				readyList1, readyList2, readyList3, readyList4, readyList5, readyList6, readyList7, readyList8,
				readyList9, blockList0, blockList1, blockList2, blockList3, blockList4, blockList5, blockList6,
				blockList7, blockList8, blockList9, pendRQlist0, pendRQlist1, pendRQlist2, pendRQlist3, pendRQlist4,
				pendRQlist5, pendBQList0, pendBQList1, pendBQList2, pendBQList3, pendBQList4, pendBQList5, txtProid,
				txtPriority, txtAccesstext, txtWaitreason, txtInstructionnum, txtDatanum, txtTextadd, txtDataadd,
				txtStackadd, txtBufferadd, txtProTableadd, txtCputime, txtSumtime, txtPc, txtIr, txtPsw, txtAr, txtSp,
				txtLogicadd, txtTableadd, txtPhysicadd, tlbFrame.getTable(), pgTbFrame.getTable(), textArea,
				buddyFrame.getLevelsArrayList());
		
		JButton button = new JButton("\u6B7B\u9501\u6848\u4F8B");
		button.setFont(new Font("宋体", Font.PLAIN, 15));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//凭空创建5个作业而非请求，这5个作业的优先级相同，第二条指令均为计算指令，且每个进程占有的源寄存器都是其后进程所需要的目的寄存器。
				JMS.createDeath();
			}
		});
		runProPanel.add(button);
		
		
		JButton button_1 = new JButton("\u9009\u7528\u5916\u5B58\u6587\u4EF6");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMS.setCreate(false);
				JMS.readOld();
				JMS.readFutureRequest();
			}
		});
		runProPanel.add(button_1);
		Thread thread4 = new Thread(threadRefresh);
		thread4.start();
	}
}
