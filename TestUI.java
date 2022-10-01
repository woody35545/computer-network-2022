import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

import javax.swing.JComboBox;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class TestUI extends JFrame implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	String path;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextArea textarea_srcMacAddr;
	private JTextArea textarea_dstMacAddr;
	int selected_index;
	JComboBox comboBox_NIC;
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// Initialize layers
		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
		m_LayerMgr.AddLayer(new ARPLayer("ARP"));
		m_LayerMgr.AddLayer(new IPLayer("IP"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new TestUI("TestUI"));

		// Connect all currently existing layers
		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ( *Chat ( *TestUI ) )");
	}

	public TestUI(String pName) {
		setTitle("Packet Debugger");
		pLayerName = pName;
		getContentPane().setLayout(null);
		setBounds(250, 250, 386, 416);

		JPanel panel = new JPanel();
		panel.setBounds(12, 10, 354, 231);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel label_NIC = new JLabel("NIC List");
		label_NIC.setBounds(0, 36, 57, 15);
		panel.add(label_NIC);
		
		JLabel lblSourceMac = new JLabel("Src MAC");
		lblSourceMac.setBounds(0, 67, 86, 15);
		panel.add(lblSourceMac);
		
		JLabel lblDestinationMac = new JLabel("Dst MAC");
		lblDestinationMac.setBounds(0, 97, 86, 15);
		panel.add(lblDestinationMac);
		
		textarea_dstMacAddr = new JTextArea();
		textarea_dstMacAddr.setText("0-50-56-C0-0-1");
		textarea_dstMacAddr.setBounds(57, 93, 285, 21);
			
		panel.add(textarea_dstMacAddr);
		textarea_dstMacAddr.setColumns(10);
		
		textarea_srcMacAddr = new JTextArea();
		textarea_srcMacAddr.setBounds(57, 62, 285, 21);
		textarea_srcMacAddr.setText("0-50-56-C0-0-8");
		panel.add(textarea_srcMacAddr);
		textarea_srcMacAddr.setColumns(10);
		
		comboBox_NIC = new JComboBox();
		comboBox_NIC.setBounds(57, 30, 197, 23);
		this.SetCombobox();
		panel.add(comboBox_NIC);
		
		JButton NIC_select_button = new JButton("Select");
		NIC_select_button.setBounds(266, 31, 76, 25);
		panel.add(NIC_select_button);
		
		JButton btn_set = new JButton("Set");
		btn_set.setBounds(57, 190, 285, 31);
		panel.add(btn_set);
		
		JTextArea textarea_srcIpAddr = new JTextArea();
		textarea_srcIpAddr.setColumns(10);
		textarea_srcIpAddr.setText("192.168.0.1");
		textarea_srcIpAddr.setBounds(57, 122, 285, 21);
		panel.add(textarea_srcIpAddr);
		
		JLabel lblDstIp = new JLabel("Dst IP");
		lblDstIp.setBounds(0, 157, 86, 15);
		panel.add(lblDstIp);
		
		JTextArea textarea_dstIpAddr = new JTextArea();
		textarea_dstIpAddr.setColumns(10);
		textarea_dstIpAddr.setText("192.168.0.2");
		textarea_dstIpAddr.setBounds(57, 153, 285, 21);
		panel.add(textarea_dstIpAddr);
		
		JLabel lblSrcip = new JLabel("Src IP");
		lblSrcip.setBounds(0, 127, 86, 15);
		panel.add(lblSrcip);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(12, 251, 354, 118);
		getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JButton btnNewButton = new JButton("Receive ARP Request");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton.setBounds(180, 51, 170, 23);
		panel_1.add(btnNewButton);
		
		JButton btnSendArpPacket = new JButton("Send ARP Request");
		btnSendArpPacket.setBounds(0, 51, 170, 23);
		panel_1.add(btnSendArpPacket);
		
		JButton btnSendArpReply = new JButton("Send ARP Reply");
		btnSendArpReply.setBounds(0, 85, 170, 23);
		panel_1.add(btnSendArpReply);
		
		JButton btnReceiveArpReply = new JButton("Receive ARP Reply");
		btnReceiveArpReply.setBounds(180, 84, 170, 23);
		panel_1.add(btnReceiveArpReply);
		
		JButton btn_send = new JButton("Send");
		btn_send.setBounds(0, 10, 350, 31);
		panel_1.add(btn_send);
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btn_set.getText() == "Reset") {

					byte[] bytes = new byte[]{(byte)0x99};
					((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).test_send();	
					// p_UnderLayer.Send(bytes, bytes.length);
				} else {
					JOptionPane.showMessageDialog(null, "�ּ� ���� ����");
				}
			}
		});
		btn_set.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btn_set.getText() == "Reset") {
					textarea_srcMacAddr.setText("");
					textarea_dstMacAddr.setText("");
					textarea_srcIpAddr.setText("");
					textarea_dstIpAddr.setText("");
					btn_set.setText("set");
					textarea_dstMacAddr.setEditable(true);
				} else {
					byte[] srcMacAddress = new byte[6];
					byte[] dstMacAddress = new byte[6];
					byte[] srcIPAddress = new byte[4];
					byte[] dstIPAddress = new byte[4];
					String srcMac = textarea_srcMacAddr.getText();
					String dstMac = textarea_dstMacAddr.getText();
					String srcIP  = textarea_srcIpAddr.getText();
					String dstIP = textarea_dstIpAddr.getText();
					

					String[] byte_srcMac = srcMac.split("-");
					String[] byte_dstMac = dstMac.split("-");

					for (int i = 0; i < 6; i++) {
						srcMacAddress[i] = (byte) Integer.parseInt(byte_srcMac[i], 16);
					}

					for (int i = 0; i < 6; i++) {
						dstMacAddress[i] = (byte) Integer.parseInt(byte_dstMac[i], 16);
					}
					
					((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).setEthernetHeaderType(new byte[]{0x08, 0x00});
					((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).setEthernetHeaderSrcMacAddr(srcMacAddress);
					((EthernetLayer)m_LayerMgr.GetLayer("Ethernet")).setEthernetHeaderDstMacAddr(dstMacAddress);
					((IPLayer)m_LayerMgr.GetLayer("IP")).setIpHeaderSrcIPAddr(Utils.convertStrIpToByte(srcIP));
					((IPLayer)m_LayerMgr.GetLayer("IP")).setIpHeaderDstIPAddr(Utils.convertStrIpToByte(dstIP));
					((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(selected_index);

					btn_set.setText("Reset");
					textarea_dstMacAddr.setEditable(false);
				}

			}
		});
		NIC_select_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected = comboBox_NIC.getSelectedItem().toString();
				selected_index = comboBox_NIC.getSelectedIndex();
				textarea_srcMacAddr.setText("");
				try {
					byte[] MacAddress = ((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(selected_index)
							.getHardwareAddress();
					String hexNumber;
					for (int i = 0; i < 6; i++) {
						hexNumber = Integer.toHexString(0xff & MacAddress[i]);
						textarea_srcMacAddr.append(hexNumber.toUpperCase());
						if (i != 5)
							textarea_srcMacAddr.append("-");
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}
		});
		setVisible(true);
		}
	
	private void SetCombobox() {
		List<PcapIf> m_pAdapterList = new ArrayList<PcapIf>();
		StringBuilder errbuf = new StringBuilder();

		int r = Pcap.findAllDevs(m_pAdapterList, errbuf);
		if (r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
			System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
			return;
		}
		for (int i = 0; i < m_pAdapterList.size(); i++)
			this.comboBox_NIC.addItem(m_pAdapterList.get(i).getDescription());
	}


	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

	@Override
	public BaseLayer GetUnderLayer(int nindex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseLayer GetUpperLayer() {
		// TODO Auto-generated method stub
		return null;
	}
}
