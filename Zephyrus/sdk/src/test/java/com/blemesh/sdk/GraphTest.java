package com.blemesh.sdk;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Date;

import com.blemesh.sdk.mesh_graph.LocalGraph;
import com.blemesh.sdk.mesh_graph.Peer;
import com.blemesh.sdk.mesh_graph.PeersEdge;
import com.blemesh.sdk.mesh_graph.PeersGraph;

import static org.junit.Assert.*;

public class GraphTest {
    //@Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void graph_merge_test() throws Exception {

        Peer mLocalNode = new Peer("local", null, 0, 0);
        LocalGraph mGraph = new LocalGraph(mLocalNode);
        Peer node2 = new Peer("node2", new Date(), 55, 1);
        Peer node3 = new Peer("node3", null, 76, 1);
        mGraph.newDirectRemote(node2);
        mGraph.insertEdge(new PeersEdge(mLocalNode.getAlias(),node2.getAlias(),15));
        mGraph.newDirectRemote(node3);
        mGraph.insertEdge(new PeersEdge(mLocalNode.getAlias(),node3.getAlias(),34));
        mGraph.displayGraph();

        Peer remoteNode = new Peer("node1", null, 0, 0);
        LocalGraph remoteGraph = new LocalGraph(remoteNode);
        Peer node4 = new Peer("node4",null,100,1);
        remoteGraph.newDirectRemote(node4);
        remoteGraph.insertEdge(new PeersEdge(remoteNode.getAlias(),node4.getAlias(),65));
        remoteGraph.displayGraph();

        mGraph.newDirectRemote(new Peer("node1",null,45,1));
        remoteGraph.newDirectRemote(new Peer("local",null,55,1));

        mGraph.mergeGarph(remoteNode,remoteGraph);
        mGraph.displayGraph();

        JSONObject vertexJSONObject = mGraph.toVertexJSONObject();
        JSONObject edgesJSONObject =mGraph.toEdgeJSONOBject();

        PeersGraph copyGraph = new PeersGraph(vertexJSONObject,edgesJSONObject);

        copyGraph.displayGraph();
    }

    @Test
    public void shortest_path_test(){
        Peer mLocalNode = new Peer("local", null, 0, 0);
        LocalGraph mGraph = new LocalGraph(mLocalNode);
        mGraph.insertVertex(new Peer("node1",new Date(),20,1));
        mGraph.insertVertex(new Peer("node2",new Date(),30,1));
        mGraph.insertVertex(new Peer("node3",new Date(),35,1));
        mGraph.insertVertex(new Peer("node4",new Date(),25,1));
        mGraph.insertVertex(new Peer("node5",new Date(),25,1));

        mGraph.addMatrixRow("node1");
        mGraph.addMatrixRow("node2");
        mGraph.addMatrixRow("node3");
        mGraph.addMatrixRow("node4");
        mGraph.addMatrixRow("node5");

        mGraph.insertEdge(new PeersEdge("local","node1",50));
        mGraph.insertEdge(new PeersEdge("node1","local",50));

        mGraph.insertEdge(new PeersEdge("local","node2",50));
        mGraph.insertEdge(new PeersEdge("node2","local",50));

        mGraph.insertEdge(new PeersEdge("node1","node3",20));
        mGraph.insertEdge(new PeersEdge("node3","node1",20));

        mGraph.insertEdge(new PeersEdge("node2","node4",10));
        mGraph.insertEdge(new PeersEdge("node4","node2",10));

        mGraph.insertEdge(new PeersEdge("node3","node5",30));
        mGraph.insertEdge(new PeersEdge("node5","node3",30));

        mGraph.insertEdge(new PeersEdge("node4","node5",40));
        mGraph.insertEdge(new PeersEdge("node5","node4",40));

        mGraph.displayGraph();
        mGraph.calCluateShortestPath();
        mGraph.displayAllShortestPath();

    }
}