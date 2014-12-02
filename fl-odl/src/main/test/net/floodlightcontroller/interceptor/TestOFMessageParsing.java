/**
 * Copyright (c) 2014, NetIDE Consortium (Create-Net (CN), Telefonica Investigacion Y Desarrollo SA (TID), Fujitsu 
 * Technology Solutions GmbH (FTS), Thales Communications & Security SAS (THALES), Fundacion Imdea Networks (IMDEA),
 * Universitaet Paderborn (UPB), Intel Research & Innovation Ireland Ltd (IRIIL) )
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors:
 *     ...
 */
package net.floodlightcontroller.interceptor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.util.HexString;

/**
 * Describe your class here...
 *
 * @author aleckey
 *
 */
public class TestOFMessageParsing {

	private final String PACKET_IN = "[\"packet\", {\"raw\": [186, 100, 113, 119, 229, 86, 190, 234, 9, 83, 209, 248, 8, 0, 69, 0, 0, 84, 110, 53, 0, 0, 64, 1, 248, 113, 10, 0, 0, 2, 10, 0, 0, 1, 0, 0, 157, 245, 111, 210, 0, 4, 13, 76, 53, 84, 190, 144, 6, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55], \"switch\": 1, \"inport\": 2}]";
	
	private final String SWITCH_JOIN_BEGIN = "[\"switch\", \"join\", 1, \"BEGIN\"]"; 
	private final String SWITCH_JOIN_END   = "[\"switch\", \"join\", 1, \"END\"]";
	
	private final String PORT_JOIN_BEGIN1 = "[\"port\", \"join\", 1, 1, true, true, [\"OFPPF_COPPER\", \"OFPPF_10GB_FD\"]]";
	private final String PORT_JOIN_BEGIN2 = "[\"port\", \"join\", 1, 2, true, true, [\"OFPPF_COPPER\", \"OFPPF_10GB_FD\"]]";
	
	private final String SWITCH_PART = "[\"switch\", \"part\", 1]";
	private final String PORT_PART = "[\"port\", \"part\", 1, 1]";
	
	@Test
	public void testSwitchBegin() {
		MessageSwitch msgSwitch = new MessageSwitch(SWITCH_JOIN_BEGIN);
		assertEquals("Switch ID not set correctly", 1L, msgSwitch.getId());
		assertEquals("Switch action not set correctly", "join", msgSwitch.getAction());
		assertTrue(msgSwitch.isBegin());
	}

	@Test
	public void testSwitchEnd() {
		MessageSwitch msgSwitch = new MessageSwitch(SWITCH_JOIN_END);
		assertEquals("Switch ID not set correctly", 1L, msgSwitch.getId());
		assertEquals("Switch action not set correctly", "join", msgSwitch.getAction());
		assertFalse(msgSwitch.isBegin());
	}
	
	@Test
	public void testSwitchPart() {
		MessageSwitch msgSwitch = new MessageSwitch(SWITCH_PART);
		assertEquals("Switch ID not set correctly", 1L, msgSwitch.getId());
		assertEquals("Switch action not set correctly", "part", msgSwitch.getAction());
		assertFalse(msgSwitch.isBegin());
	}
	
	@Test
	public void testPortJoin() {
		MessagePort msgPort = new MessagePort(PORT_JOIN_BEGIN1);
		assertEquals("Switch ID not set correctly", 1L, msgPort.getSwitchId());
		assertEquals("Port action not set correctly", "join", msgPort.getAction());
		assertEquals("Port number not set correctly", 1, msgPort.getPortNo());
		assertEquals("Port Feature 1 not set correctly", "OFPPF_COPPER", msgPort.getPortFeatures().get(0));
		assertEquals("Port Feature 1 not set correctly", "OFPPF_10GB_FD", msgPort.getPortFeatures().get(1));
		assertEquals("OFPort no not set correctly", 1, msgPort.getOfPort().getPortNumber());
		assertEquals("OFPort Features not set correctly", 0, msgPort.getOfPort().getSupportedFeatures());
		assertEquals("OFPort Features not set correctly", 192, msgPort.getOfPort().getCurrentFeatures());
	}
	
	@Test
	public void testPortPart() {
		MessagePort msgPort = new MessagePort(PORT_PART);
		assertEquals("Switch ID not set correctly", 1L, msgPort.getSwitchId());
		assertEquals("Port action not set correctly", "part", msgPort.getAction());
		assertEquals("Port number not set correctly", 1, msgPort.getPortNo());
		assertEquals("Port Features is not empty", 0, msgPort.getPortFeatures().size());
		assertNull(msgPort.getOfPort());
	}
	
	@Test
	public void testMessagePacket() {
		MessagePacket msg = new MessagePacket(PACKET_IN);
		assertEquals("Switch ID not set correctly", 1L, msg.getSwitchId());
		assertEquals("Port number not set correctly", 2, msg.getInPort());
		//assertEquals("Port number not set correctly", 1, msgPort.getPortNo());
		//assertEquals("Port Features is not empty", 0, msgPort.getPortFeatures().size());
		//assertNull(msgPort.getOfPort());
	}
	
	@Test
	public void testMessageFlowMod() {
		OFFlowMod flowMod = createFlowMod();
		MessageSerializer msg = new MessageSerializer();
		String output = msg.serializeMessage(flowMod);
		System.out.println(output);
	}
	
	private OFFlowMod createFlowMod() {
		//["install", 0, [{"outport": 2}], 
		//  {"dstip": [49, 48, 46, 48, 46, 48, 46, 49], "srcip": [49, 48, 46, 48, 46, 48, 46, 50],
		//   "dstmac": [54, 97, 58, 50, 100, 58, 55, 102, 58, 50, 57, 58, 99, 56, 58, 54, 49], "srcmac": [49, 50, 58, 57, 51, 58, 50, 99, 58, 52, 97, 58, 52, 56, 58, 50, 52],
		//   "dstport": 0, "srcport": 0
		//   "protocol": 1,  "tos": 0, "inport": 1, "switch": 2, "ethtype": 2048} ]
		byte[] dstIp = new byte[] {49, 48, 46, 48, 46, 48, 46, 49}; //10.0.0.1
		byte[] srcIp = new byte[] {49, 48, 46, 48, 46, 48, 46, 50}; //10.0.0.2
		byte[] dstMac = new byte[] {54, 97, 58, 50, 100, 58, 55, 102, 58, 50, 57, 58, 99, 56, 58, 54, 49}; //6a:2d:7f:29:c8:61
		byte[] srcMac = new byte[] {49, 50, 58, 57, 51, 58, 50, 99, 58, 52, 97, 58, 52, 56, 58, 50, 52};   //12:93:2c:4a:48:24
		
		StringBuilder sb = new StringBuilder();
		sb.append("dl_src=").append(new String(srcMac)).append(",dl_dst=").append(new String(dstMac)).append(",");
		sb.append("nw_src=").append(new String(srcIp)).append(",nw_dst=").append(new String(dstIp)).append(",");
		sb.append("nw_proto=1,");
		sb.append("nw_tos=0,");
		sb.append("in_port=2,");
		sb.append("eth_type=2048,");
		sb.append("tp_src=1234,");
		sb.append("tp_dst=8080");
		
		OFFlowMod flowMod = new OFFlowMod();
		OFMatch match = new OFMatch();
		match.fromString(sb.toString());
		flowMod.setMatch(match);
		
		BasicFactory factory = new BasicFactory();
		flowMod.setActionFactory(factory.getActionFactory());
		List<OFAction> actions = new ArrayList<OFAction>();
		
		OFAction action = new OFAction();
		action.setType(OFActionType.SET_DL_DST);
		actions.add(action);
		
		action = new OFAction();
		action.setType(OFActionType.SET_DL_SRC);
		actions.add(action);
		
		action = new OFAction();
		action.setType(OFActionType.SET_NW_SRC);
		actions.add(action);
		
		action = new OFAction();
		action.setType(OFActionType.SET_NW_DST);
		actions.add(action);
		
		action = new OFAction();
		action.setType(OFActionType.SET_NW_TOS);
		actions.add(action);
		
		action = new OFAction();
		action.setType(OFActionType.SET_TP_DST);
		actions.add(action);
		
		action = new OFAction();
		action.setType(OFActionType.SET_TP_SRC);
		actions.add(action);
		
		flowMod.setActions(actions);
		flowMod.setOutPort((short) 2);
		flowMod.setCommand(OFFlowMod.OFPFC_ADD);
		flowMod.setBufferId(-1);
		flowMod.setFlags(OFFlowMod.OFPFF_SEND_FLOW_REM);
		
		return flowMod;
	}

}
