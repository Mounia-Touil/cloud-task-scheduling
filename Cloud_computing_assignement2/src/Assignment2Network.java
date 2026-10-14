package org.cloudbus.cloudsim.examples.network;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Assignment2Network {

    private static List<Cloudlet> cloudletList1;
    private static List<Cloudlet> cloudletList2;
    private static List<Cloudlet> cloudletList3;

    private static List<Vm> vmlist1;
    private static List<Vm> vmlist2;
    private static List<Vm> vmlist3;

    public static void main(String[] args) {

        Log.printLine("Starting Assignment2Network...");

        try {
            int numUser = 3;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUser, calendar, traceFlag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");
            Datacenter datacenter2 = createDatacenter("Datacenter_2");

            DatacenterBroker broker1 = createBroker(1);
            DatacenterBroker broker2 = createBroker(2);
            DatacenterBroker broker3 = createBroker(3);

            int brokerId1 = broker1.getId();
            int brokerId2 = broker2.getId();
            int brokerId3 = broker3.getId();

            vmlist1 = new ArrayList<Vm>();
            vmlist2 = new ArrayList<Vm>();
            vmlist3 = new ArrayList<Vm>();

            long size = 10000;
            int mips = 250;
            int ram = 512;
            long bw = 1000;
            int pesNumber = 1;
            String vmm = "Xen";

            Vm vm1 = new Vm(0, brokerId1, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm2 = new Vm(0, brokerId2, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            Vm vm3 = new Vm(0, brokerId3, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

            vmlist1.add(vm1);
            vmlist2.add(vm2);
            vmlist3.add(vm3);

            broker1.submitVmList(vmlist1);
            broker2.submitVmList(vmlist2);
            broker3.submitVmList(vmlist3);

            cloudletList1 = new ArrayList<Cloudlet>();
            cloudletList2 = new ArrayList<Cloudlet>();
            cloudletList3 = new ArrayList<Cloudlet>();

            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet cloudlet1 = new Cloudlet(0, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet1.setUserId(brokerId1);

            Cloudlet cloudlet2 = new Cloudlet(0, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet2.setUserId(brokerId2);

            Cloudlet cloudlet3 = new Cloudlet(0, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet3.setUserId(brokerId3);

            cloudletList1.add(cloudlet1);
            cloudletList2.add(cloudlet2);
            cloudletList3.add(cloudlet3);

            broker1.submitCloudletList(cloudletList1);
            broker2.submitCloudletList(cloudletList2);
            broker3.submitCloudletList(cloudletList3);

            NetworkTopology.buildNetworkTopology("examples/org/cloudbus/cloudsim/examples/network/assignment2_topology.brite");

            NetworkTopology.mapNode(datacenter0.getId(), 0);
            NetworkTopology.mapNode(datacenter1.getId(), 2);
            NetworkTopology.mapNode(datacenter2.getId(), 7);

            NetworkTopology.mapNode(broker1.getId(), 3);
            NetworkTopology.mapNode(broker2.getId(), 5);
            NetworkTopology.mapNode(broker3.getId(), 9);

            CloudSim.startSimulation();

            List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
            List<Cloudlet> newList2 = broker2.getCloudletReceivedList();
            List<Cloudlet> newList3 = broker3.getCloudletReceivedList();

            CloudSim.stopSimulation();

            Log.printLine("=============> Broker " + brokerId1);
            printCloudletList(newList1);

            Log.printLine("=============> Broker " + brokerId2);
            printCloudletList(newList2);

            Log.printLine("=============> Broker " + brokerId3);
            printCloudletList(newList3);

            Log.printLine("Assignment2Network finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<Host>();
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        int hostId = 0;
        int ram = 2048;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
                new VmSchedulerSpaceShared(peList)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, timeZone,
                cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(int id) {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker" + id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
                + indent + "Start Time" + indent + "Finish Time");

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                DecimalFormat dft = new DecimalFormat("###.##");
                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
                        + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}
