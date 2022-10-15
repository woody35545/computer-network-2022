import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
public class ARPLayer implements BaseLayer {
	private static final byte[] UNKNOWN_DESTINATION_MAC_ADDR = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00 };
	private static final byte[] OPCODE_ARP_REQUEST = new byte[] { 0x00, 0x01 };
	// Default hardware type = Ethernet(0x0001)
	private static final byte[] DEFAULT_HARDWARE_TYPE = new byte[] { 0x00, 0x01 };
	// Default protocol type = IPv4(0x0800)
	private static final byte[] DEFAULT_PROTOCOL_TYPE = new byte[] { 0x08, 0x00 };
	// Default length of hardware address = 0x06 (MAC Address)
	private static final byte DEFAULT_LENGTH_OF_HARDWARE_ADDRESS = (byte) 0x06;
	// Default length of protocol address = 0x04 (IP Address)
	private static final byte DEFAULT_LENGTH_OF_PROTOCOL_ADDRESS = (byte) 0x04;
	public int nUpperLayerCount = 0;
	public int nUnderLayerCount = 0;
	public String pLayerName = null;
	public ArrayList<BaseLayer> p_UnderLayer = new ArrayList<BaseLayer>();
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private _ARP_HEADER m_sHeader = new _ARP_HEADER();
	private _ARP_CACHE_TABLE arpCacheTable = new _ARP_CACHE_TABLE();
	private _PROXY_CACHE_TABLE proxyCacheTable = new _PROXY_CACHE_TABLE();
	public ARPLayer(String pName) {
		// ARPLayer class constructor
		pLayerName = pName;
	}
	private class _ARP_HEADER {
		private int length_of_header = 28;
		/*
		 * Data structure for representing ARP Message Format
		 * 
		 * ARP Header Structure Specification (in order) # [Hardware Type] - 2 Bytes #
		 * [Protocol Type] - 2 Bytes # [Length of hardware address] - 1 Byte # [Length
		 * of Protocol address] - 1 Byte # [Opcode] - 2 Bytes # [Sender hardware
		 * address] - 6 Bytes # [Sender protocol address] - 4 Bytes # [Target hardware
		 * address] - 6 Bytes # [Target protocol address] - 4 Bytes
		 */
		byte[] hardwareType = new byte[2];
		byte[] protocolType = new byte[2];
		byte lengthOfHardwareAddr;
		byte lengthOfProtocolAddr;
		byte[] opCode = new byte[2];
		_MAC_ADDR senderMac = new _MAC_ADDR();
		_MAC_ADDR targetMac = new _MAC_ADDR();
		_IP_ADDR senderIp = new _IP_ADDR();
		_IP_ADDR targetIp = new _IP_ADDR();
		public _ARP_HEADER() {
			// _ARP_HEADER constructor
		}
		@SuppressWarnings("unused")
	      public _ARP_HEADER(byte[] pHardwareType, byte[] pProtocolType, byte pLengthOfMacAddr,
	            byte pLengthOfProtocolAddr, byte[] pOpcode, _MAC_ADDR pSenderMacAddress, _IP_ADDR pSenderIpAddress,
	            _MAC_ADDR pTargetMacAddress,_IP_ADDR pTargetIpAddress) {
			// _ARP_HEADER constructor with Parameters
			this.hardwareType = pHardwareType;
			this.protocolType = pProtocolType;
			this.lengthOfHardwareAddr = pLengthOfMacAddr;
			this.lengthOfProtocolAddr = pLengthOfProtocolAddr;
			this.opCode = pOpcode;
			this.senderMac = pSenderMacAddress;
			this.targetMac = pTargetMacAddress;
			this.senderIp = pSenderIpAddress;
			this.targetIp = pTargetIpAddress;
		}
		public void SetHardwareType(byte[] pHardwareType) {
			this.hardwareType = pHardwareType;
		}
		public void SetProtocolType(byte[] pProtocolType) {
			this.protocolType = pProtocolType;
		}
		public void SetLengthOfHardwareAddress(byte _pLengthOfHardwareAddress) {
			this.lengthOfHardwareAddr = _pLengthOfHardwareAddress;
		}
		public void SetLengthOfProtocolAddress(byte pLengthOfProtocolAddress) {
			this.lengthOfProtocolAddr = pLengthOfProtocolAddress;
		}
		public void SetOpCode(byte[] pOpCode) {
			this.opCode = pOpCode;
		}
		public void SetSenderMacAddress(byte[] pSenderMacAddress) {
			this.senderMac.addr = pSenderMacAddress;
		}
		public void SetTargetMacAddress(byte[] pTargetMacAddress) {
			this.targetMac.addr = pTargetMacAddress;
		}
		public void SetSenderIPAddress(byte[] pSenderIPAddress) {
			this.senderIp.addr = pSenderIPAddress;
		}
		public void SetTargetIPAddress(byte[] pTargetIPAddress) {
			this.targetIp.addr = pTargetIPAddress;
		}
		public int get_length_of_header() {
			return this.length_of_header;
		}
	}
	private class _ARP_CACHE_TABLE {
		private final static int Capacity = 30;
		private int size = 0;
		private String[] ipAddr = new String[Capacity];
		private String[] macAddr = new String[Capacity];
		private String[] state = new String[Capacity];
		public _ARP_CACHE_TABLE() {
			// _ARP_CACHE_TABLE constructor
		}
		public boolean addArpCacheTableElement(String pIpAddr, String pMacAddr, String pState) {
			if (this.isExist(pIpAddr)) {
				this.updateArpCacheTableElement(pIpAddr, pMacAddr, pState);
				return true;
			}
			if (this.size < this.Capacity && !this.isExist(pIpAddr)) {
				ipAddr[size] = pIpAddr;
				macAddr[size] = pMacAddr;
				state[size] = pState;
				size++;
				return true;
			}
			return false;
		}
		public boolean addArpCacheTableElement(String pIpAddr) {
			if (this.size < this.Capacity && !this.isExist(pIpAddr)) {
				ipAddr[size] = pIpAddr;
				macAddr[size] = "??:??:??:??:??:??";
				state[size] = "incomplete";
				size++;
				return true;
			}
			return false;
		}
		private int getIndexOf(String pIpAddr) {
			if (this.isExist(pIpAddr))
				for (int i = 0; i < size; i++) {
					if (this.ipAddr[i].equals(pIpAddr))
						return i;
				}
			return -1;
		}
		public boolean updateArpCacheTableElement(String pIpAddr, String pMacAddr, String pState) {
			if (!this.isExist(pIpAddr)) {
				return false;
			}
			for (int i = 0; i < size; i++) {
				if (ipAddr[i] == pIpAddr) {
					macAddr[i] = pMacAddr;
					state[i] = pState;
					System.out.println("Update");
					return true;
				}
			}
			return false;
		}
		
		public boolean deleteArpCacheTable(String pIpAddr) {
			if (this.isExist(pIpAddr)) {
				int idx = 0;
				for (int i = 0; i < this.size; i++) {
					if (this.ipAddr[i].equals(pIpAddr)) {
						idx = i;
					}
				}
				this.ipAddr = this.removeElementFromArray(this.ipAddr, idx);
				this.macAddr = this.removeElementFromArray(this.macAddr, idx);
				this.state =this.removeElementFromArray(this.state,idx);
				this.size--;
				
				this.showArpTable();
				return true;
			}
			return false;
		}
		
		public void resetArpCacheTable() {
			this.size = 0;
			this.ipAddr = new String[Capacity];
			this.macAddr = new String[Capacity];
			this.state = new String[Capacity];
		}
		
		public boolean isExist(String pIpAddr) {
			for (int i = 0; i < this.size; i++) {
				if (this.ipAddr[i].equals(pIpAddr)) {
					return true;
				}
			}
			return false;
		}
		private String[] removeElementFromArray(String[] arr, int index)
	    {
	        if (arr == null || index < 0 || index >= arr.length) {
	            return arr;
	        }
	        String[] removed_arr = new String[arr.length - 1];
	        System.arraycopy(arr, 0, removed_arr, 0, index);
	        System.arraycopy(arr, index + 1, removed_arr, index, arr.length - index - 1);
	        return removed_arr;
	    }
		public void showArpTable() {
			System.out.println("[ ARP CACHE TABLE ] - (size: " + this.size +")");
			for (int i = 0; i < this.size; i++) {
				System.out.print(ipAddr[i] + " | ");
				System.out.print(macAddr[i] + " | ");
				System.out.println(state[i]);
			}
			System.out.println("----------------------------------------------------------------------------");
			System.out.println("");
			/*
			 * for (int i = 0; i < this.size - 1; i++) { for (int j = 0; j < 4; j++) {
			 * System.out.print(this.ipAddr[i].getAddr()[j] + "."); } }
			 */
		}
	}
	
	private class _PROXY_CACHE_TABLE {
	      private final static int Capacity = 30;
	      private int size = 0;
	      private String[] deviceName = new String[Capacity];
	      private String[] ipAddr = new String[Capacity];
	      private String[] macAddr = new String[Capacity];
	      
	      public _PROXY_CACHE_TABLE() {
	         // _PROXY_CACHE_TABLE constructor
	      }
	      public boolean addProxyCacheTableElement(String pDeviceName, String pIpAddr, String pMacAddr) {
	         if (this.isExist(pIpAddr)) {
	            this.updateProxyCacheTableElement(pIpAddr, pMacAddr);
	         }
	         if (this.size < this.Capacity && !this.isExist(pIpAddr)) {
	            deviceName[size] = pDeviceName;
	            ipAddr[size] = pIpAddr;
	            macAddr[size] = pMacAddr;
	            size++;
	            return true;
	         }
	         return false;
	      }
	      public boolean addProxyCacheTableElement(String pIpAddr) {
	         if (this.size < this.Capacity && !this.isExist(pIpAddr)) {
	            deviceName[size] = "Host ~"; //?
	            ipAddr[size] = pIpAddr;
	            macAddr[size] = "??:??:??:??:??:??";
	            size++;
	            return true;
	         }
	         return false;
	      }
	      private int getIndexOf(String pIpAddr) {
	         if (this.isExist(pIpAddr))
	            for (int i = 0; i < size; i++) {
	               if (this.ipAddr[i].equals(pIpAddr))
	                  return i;
	            }
	         return -1;
	      }
	      public boolean updateProxyCacheTableElement(String pIpAddr, String pMacAddr) {
	         if (!this.isExist(pIpAddr)) {
	            return false;
	         }
	         for (int i = 0; i < size; i++) {
	            if (ipAddr[i] == pIpAddr) {
	               macAddr[i] = pMacAddr;
	               return true;
	            }
	         }
	         return false;
	      }
	      
	      public boolean deleteProxyCacheTable(String pIpAddr) {
	         if (this.isExist(pIpAddr)) {
	            int idx = 0;
	            for (int i = 0; i < this.size; i++) {
	               if (this.ipAddr[i].equals(pIpAddr)) {
	                  idx = i;
	               }
	            }
	            this.deviceName = this.removeElementFromArray(this.deviceName,idx);
	            this.ipAddr = this.removeElementFromArray(this.ipAddr, idx);
	            this.macAddr = this.removeElementFromArray(this.macAddr, idx);
	            this.size--;
	            
	            this.showProxyTable();
	            return true;
	         }
	         return false;
	      }
	      
	      public void resetProxyCacheTable() {
	         this.size = 0;
	         this.deviceName = new String[Capacity];
	         this.ipAddr = new String[Capacity];
	         this.macAddr = new String[Capacity];
	      }
	      
	      public boolean isExist(String pIpAddr) {
	         for (int i = 0; i < this.size; i++) {
	            if (this.ipAddr[i].equals(pIpAddr)) {
	               return true;
	            }
	         }
	         return false;
	      }
	      private String[] removeElementFromArray(String[] arr, int index)
	       {
	           if (arr == null || index < 0 || index >= arr.length) {
	               return arr;
	           }
	           String[] removed_arr = new String[arr.length - 1];
	           System.arraycopy(arr, 0, removed_arr, 0, index);
	           System.arraycopy(arr, index + 1, removed_arr, index, arr.length - index - 1);
	           return removed_arr;
	       }
	      public void showProxyTable() {
	         System.out.println("[ PROXY CACHE TABLE ] - (size: " + this.size +")");
	         for (int i = 0; i < this.size; i++) {
	            System.out.print(deviceName[i] + " | ");
	            System.out.print(ipAddr[i] + " | ");
	            System.out.print(macAddr[i] + " | ");
	         }
	         System.out.println("----------------------------------------------------------------------------");
	         System.out.println("");
	         /*
	          * for (int i = 0; i < this.size - 1; i++) { for (int j = 0; j < 4; j++) {
	          * System.out.print(this.ipAddr[i].getAddr()[j] + "."); } }
	          */
	      }
	   }
	private class _IP_ADDR {
		/*
		 * Data structure for representing IP Address, Will be used for ARP Packet
		 * structure. 4 bytes are required to represent the IP Address.
		 */
		private byte[] addr = new byte[4];
		private int lengthOfAddr = 4;
		// Initialize values ​​to 0x00
		public _IP_ADDR() {
			for (int i = 0; i < this.lengthOfAddr; i++) {
				this.addr[i] = (byte) 0x00;
			}
		}
		public _IP_ADDR(byte[] pAddr) {
			for (int i = 0; i < this.lengthOfAddr; i++) {
				this.addr[i] = pAddr[i];
			}
		}
		public byte[] getAddrByte() {
			return this.addr;
		}
		public String getAddrStr() {
			String addr_str = (addr[0] & 0xFF) + "." + (addr[1] & 0xFF) + "." + (addr[2] & 0xFF) + "." + (addr[3] & 0xFF);
			return addr_str;
		}
		public int getLengthOfAddr() {
			return this.lengthOfAddr;
		}
	}
	private class _MAC_ADDR {
		/*
		 * Data structure for representing Mac Address, Will be used for ARP Packet
		 * structure. 8 bytes are required to represent the MAC Address.
		 */
		private byte[] addr = new byte[6];
		private int lengthOfAddr = 6;
		// Initialize values ​​to 0x00
		public _MAC_ADDR() {
			for (int i = 0; i < this.lengthOfAddr; i++) {
				this.addr[i] = (byte) 0x00;
			}
		}
		public _MAC_ADDR(byte[] pAddr) {
			for (int i = 0; i < this.lengthOfAddr; i++) {
				this.addr[i] = pAddr[i];
			}
		}
		public byte[] getAddrByte() {
			return this.addr;
		}
		public String getAddrStr() {
			String addr_str = (addr[0] & 0xFF) + ":" + (addr[1] & 0xFF) + ":" + (addr[2] & 0xFF) + ":" + (addr[3] & 0xFF) + ":" + (addr[4] & 0xFF) + ":" + (addr[5] & 0xFF);
			return addr_str;
		}
		public int getLengthOfAddr() {
			return this.lengthOfAddr;
		}
	}
	public byte[] Encapsulate(_ARP_HEADER pHeader) {
		// <!> Need to check
		// Encapsulate function to create byte array type ARP Packet
		int idx_ptr = 0;
		byte[] encapsulated = new byte[pHeader.get_length_of_header()];
		for (int i = 0; i < pHeader.hardwareType.length; i++) {
			encapsulated[idx_ptr++] = pHeader.hardwareType[i];
		}
		for (int i = 0; i < pHeader.protocolType.length; i++) {
			encapsulated[idx_ptr++] = pHeader.protocolType[i];
		}
		encapsulated[idx_ptr++] = pHeader.lengthOfHardwareAddr;
		encapsulated[idx_ptr++] = pHeader.lengthOfProtocolAddr;
		for (int i = 0; i < pHeader.opCode.length; i++) {
			encapsulated[idx_ptr++] = pHeader.opCode[i];
		}
		for (int i = 0; i < pHeader.senderMac.getLengthOfAddr(); i++) {
			encapsulated[idx_ptr++] = pHeader.senderMac.addr[i];
		}
		for (int i = 0; i < pHeader.senderIp.getLengthOfAddr(); i++) {
			encapsulated[idx_ptr++] = pHeader.senderIp.addr[i];
		}
		for (int i = 0; i < pHeader.targetMac.getLengthOfAddr(); i++) {
			encapsulated[idx_ptr++] = pHeader.targetMac.addr[i];
		}
		for (int i = 0; i < pHeader.targetIp.getLengthOfAddr(); i++) {
			encapsulated[idx_ptr++] = pHeader.targetIp.addr[i];
		}
		return encapsulated;
	}
	public byte[] Decapsulate(byte[] pARPPacket) {
		// <!> Need to check
		int offset = 28;
		byte[] decapsulated = new byte[pARPPacket.length - 28];
		for (int i = 0; i < decapsulated.length; i++) {
			decapsulated[i] = pARPPacket[offset + i];
		}
		return decapsulated;
	}
	public _ARP_HEADER MakeARPRequestHeader() {
		_ARP_HEADER header = new _ARP_HEADER(DEFAULT_HARDWARE_TYPE, DEFAULT_PROTOCOL_TYPE,
				DEFAULT_LENGTH_OF_HARDWARE_ADDRESS, DEFAULT_LENGTH_OF_PROTOCOL_ADDRESS, OPCODE_ARP_REQUEST,
				this.m_sHeader.senderMac, this.m_sHeader.senderIp, new _MAC_ADDR(UNKNOWN_DESTINATION_MAC_ADDR),
				this.m_sHeader.targetIp);
		return header;
	}
	public boolean Send() {
		// <!> additional implementation required later
		// if (!this.arpCacheTable.isExist(this.m_sHeader.target_ip.getByteAddr())) {
		/*
		 * If there is no Mac address for the destination IP in the ARP cache table,
		 * then Send ARP Request
		 */
		// OpCode of ARP Request = 0x0001
		_ARP_HEADER ARPRequestHeader = this.MakeARPRequestHeader();
		// System.out.println(this.arpCacheTable.addArpCacheTableElement(this.m_sHeader.target_ip));
		// this.arpCacheTable.showArpTable();
		byte[] encapsulated = this.Encapsulate(ARPRequestHeader);
		// this.arpCacheTable.addArpCacheTableElement(Utils.convertByteFormatIpToStrFormat(ARPRequestHeader.targetIp.addr));
		this.addARPCacheTableElement(Utils.convertByteFormatIpToStrFormat(ARPRequestHeader.targetIp.addr));
		this.arpCacheTable.showArpTable();
		System.out.println("ARPLayer Send: ");
		Utils.showPacket(encapsulated);
		this.GetUnderLayer(0).Send(encapsulated, encapsulated.length);
		// }
		return true;
	}
	public boolean SendGARP() {
		// called by btnNewButton_4_1 in ARPGUI for GARP
		_ARP_HEADER ARPRequestHeader = this.MakeARPRequestHeader();
		// set targetIP's address and senderIP's address equal, because this is GARP
		ARPRequestHeader.targetIp.addr = ARPRequestHeader.senderIp.addr;
		byte[] encapsulated = this.Encapsulate(ARPRequestHeader);
		System.out.println("ARPLayer Send(GARP): ");
		Utils.showPacket(encapsulated);
		this.GetUnderLayer(0).Send(encapsulated, encapsulated.length);
		
		return true;
	}
	
	public void resetARPCacheTableGUI() { 
		((ARPGUI) this.GetUpperLayer(1)).resetTable();
	}
	public void refreshARPCacheTableGUI() {
		for (int i = 0; i < this.arpCacheTable.size; i++) {
			((ARPGUI) this.GetUpperLayer(1)).initTableValue(new String[] { Integer.toString(i),
					this.arpCacheTable.ipAddr[i], this.arpCacheTable.macAddr[i], this.arpCacheTable.state[i] });
		}
	}
	public void addARPCacheTableElement(String pIpAddr, String pMacAddr, String pState) {
		this.arpCacheTable.addArpCacheTableElement(pIpAddr, pMacAddr, pState);
		this.refreshARPCacheTableGUI();
	}
	public void addARPCacheTableElement(String pIpAddr) {
		this.arpCacheTable.addArpCacheTableElement(pIpAddr);
		this.refreshARPCacheTableGUI();
	}
	public void deleteARPCacheTableElement(String pIpAddr) {
		this.resetARPCacheTableGUI();
		this.arpCacheTable.deleteArpCacheTable(pIpAddr);
		this.refreshARPCacheTableGUI();
	}
	
	public void deleteAllARPCacheTableElement() {
		this.arpCacheTable.resetArpCacheTable();
		this.resetARPCacheTableGUI();
	}
	public void setARPHeaderSrcIp(byte[] pSrcIP) {
		this.m_sHeader.senderIp.addr = pSrcIP;
	}
	public void setARPHeaderDstIp(byte[] pTargetIP) {
		this.m_sHeader.targetIp.addr = pTargetIP;
	}
	public void setARPHeaderSrcMac(byte[] pSrcMac) {
		this.m_sHeader.senderMac.addr = pSrcMac;
	}
	public void setARPHeaderDstMac(byte[] pTargetMac) {
		this.m_sHeader.targetMac.addr = pTargetMac;
	}

	public void resetPROXYCacheTableGUI() { 
		((ARPGUI) this.GetUpperLayer(1)).resetProxyTable();
	}
	public void refreshPROXYCacheTableGUI() {
		for (int i = 0; i < this.proxyCacheTable.size; i++) {
			((ARPGUI) this.GetUpperLayer(1)).initProxyTableValue(new String[] { Integer.toString(i),
					this.proxyCacheTable.deviceName[i], this.proxyCacheTable.ipAddr[i], this.proxyCacheTable.macAddr[i]});
		}
	}

	public void addPROXYCacheTableElement(String pDeviceName, String pIpAddr, String pMacAddr) {
		this.proxyCacheTable.addProxyCacheTableElement(pDeviceName, pIpAddr, pMacAddr);
		this.refreshPROXYCacheTableGUI();
	}

	public void addPROXYCacheTableElement(String pIpAddr) {
		this.proxyCacheTable.addProxyCacheTableElement(pIpAddr);
		this.refreshPROXYCacheTableGUI();
	}

	public void deletePROXYCacheTableElement(String pIpAddr) {
		this.resetPROXYCacheTableGUI();
		this.proxyCacheTable.deleteProxyCacheTable(pIpAddr);
		this.refreshPROXYCacheTableGUI();
	}

	public void deleteAllPROXYCacheTableElement() {
		this.proxyCacheTable.resetProxyCacheTable();
		this.resetPROXYCacheTableGUI();
	}

	public void setPROXYHeaderSrcIp(byte[] pSrcIP) {
		this.m_sHeader.senderIp.addr = pSrcIP;
	}

	public void setPROXYHeaderDstIp(byte[] pTargetIP) {
		this.m_sHeader.targetIp.addr = pTargetIP;
	}

	public void setPROXYHeaderSrcMac(byte[] pSrcMac) {
		this.m_sHeader.senderMac.addr = pSrcMac;
	}

	public void setPROXYHeaderDstMac(byte[] pTargetMac) {
		this.m_sHeader.targetMac.addr = pTargetMac;
	}

	
	/*
	 * 		    ARP Request
	 * Host A      ====================>     Host B
	 *  
	 *                  ARP Reply
	 * Host A      <====================     Host B
	 *                 
	 *              header : 14 ~ 17 : Host B(ARP Reply 를 보내는 측) 의 IP : target_IP
	 *              header : 06 ~ 13 : Host B(ARP Reply 를 보내는 측) 의 MAC : target_Mac
	 *              header : 24 ~ 27 : Host B(ARP Reply 를 보내는 측) 가 응답을 보내려는 타겟 Host A 의 IP 주소 : src_IP
	 * 				
	 */
	
	
	public boolean Receive(byte[] input) {
		//Object[] value = new Object[4];
		byte[] replysender_IP = Arrays.copyOfRange(input, 14, 18);		//내가 원하는 arp request 를 받는 destination  
		byte[] replysender_Mac = Arrays.copyOfRange(input, 8, 14);
		byte[] reqsender_IP = Arrays.copyOfRange(input, 24, 28);
		
		m_sHeader = new _ARP_HEADER(); 
		m_sHeader.SetTargetIPAddress(replysender_IP);
		m_sHeader.SetTargetMacAddress(replysender_Mac);
		m_sHeader.SetSenderIPAddress(reqsender_IP);
		String target_IP = m_sHeader.targetIp.getAddrStr();
		String src_IP = m_sHeader.senderIp.getAddrStr();
		String target_MAC = m_sHeader.targetMac.getAddrStr();
		System.out.println("-----------------------");
		
		if (input[6] == (byte) 0x00 && input[7] == (byte) 0x02) { // ARP-reply Receive ("Incomplete" ->
			 System.out.println("This is ARP Reply Type Packet");
			 // 만약 
	         if (target_IP.equals(src_IP)) {
	            String macAddress = String.format("%X:", replysender_Mac[0]) + String.format("%X:", replysender_Mac[1]) + String.format("%X:", replysender_Mac[2]) + String.format("%X:", replysender_Mac[3]) + String.format("%X:", replysender_Mac[4]) + String.format("%X", replysender_Mac[5]);
	            System.out.println("duplicate IP address sent from Ethernet address :" + macAddress);
	            return false;
	         }
	         if (arpCacheTable.isExist(target_IP)) {
	            System.out.println(target_IP);
	         }
	         
	         this.addARPCacheTableElement(target_IP, target_MAC, "Complete");
	      }
		// src - > dst
		else if(input[6] == (byte) 0x00 && input[7] == (byte) 0x01) {	//	ARP-Request case
			 if (target_IP.equals(src_IP)) {	// if srcIP and dstIP is equal (GARP)
				 if (arpCacheTable.isExist(target_IP)) {
			            System.out.println(target_IP);
			            this.addARPCacheTableElement(target_IP, target_MAC, "Complete");	//	add	-> updateARPCacheTableElement
			            
			     }
			 }
			 else if (proxyCacheTable.isExist(Utils.convertAddrFormat(Arrays.copyOfRange(input, 24, 28)))) {
				 	// if target IP exists in my proxy table	
				 	
				 		// make reply packet header
					 	_ARP_HEADER arpReplyHeader = new _ARP_HEADER(DEFAULT_HARDWARE_TYPE, DEFAULT_PROTOCOL_TYPE,
								DEFAULT_LENGTH_OF_HARDWARE_ADDRESS, DEFAULT_LENGTH_OF_PROTOCOL_ADDRESS, OPCODE_ARP_REQUEST,
								this.m_sHeader.senderMac, new _IP_ADDR(Arrays.copyOfRange(input, 24, 28)),new _MAC_ADDR(Arrays.copyOfRange(input, 8, 14)),
								new _IP_ADDR(Arrays.copyOfRange(input, 14, 18))); 	
				 		
					 	// make packet(byte type)
					 	byte[] replyPacket = this.Encapsulate(arpReplyHeader);
					 	
					 	// send Proxy-Reply to ethernet
						this.GetUnderLayer(0).Send(replyPacket, replyPacket.length);
				 }
				 
			 
		}
	
        return true;
	}
	
	
	
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer.add(nUnderLayerCount++, pUnderLayer);
		// nUpperLayerCount++;
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
		if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
			return null;
		return p_UnderLayer.get(nindex);
	}
}
